package com.example.aichatdemo.ui.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.aichatdemo.data.ChatRepository
import com.example.aichatdemo.data.model.ChatMessage
import com.example.aichatdemo.data.model.Role
import com.example.aichatdemo.network.ChatStreamEvent
import java.util.UUID
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ChatViewModel(
  private val repository: ChatRepository,
  private val throttleMs: Long = 75L,
) : ViewModel() {
  private val _uiState = MutableStateFlow(ChatUiState())
  val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

  private val _events = MutableSharedFlow<ChatUiEvent>(extraBufferCapacity = 32)
  val events: SharedFlow<ChatUiEvent> = _events.asSharedFlow()

  private var streamJob: Job? = null

  fun sendMessage(input: String) {
    val trimmed = input.trim()
    if (trimmed.isEmpty() || _uiState.value.isGenerating) return

    val userMessage =
      ChatMessage(id = UUID.randomUUID().toString(), role = Role.USER, content = trimmed)
    val assistantMessage =
      ChatMessage(
        id = UUID.randomUUID().toString(),
        role = Role.ASSISTANT,
        content = "",
        isStreaming = true,
        isReasoningStreaming = true,
      )
    val appendedMessages = listOf(userMessage, assistantMessage)
    val requestMessages = _uiState.value.messages + userMessage

    _uiState.update {
      it.copy(messages = it.messages + appendedMessages, isGenerating = true, errorMessage = null)
    }
    _events.tryEmit(ChatUiEvent.AppendMessages(appendedMessages))

    streamJob?.cancel()
    streamJob =
      viewModelScope.launch {
        var reasoningContent = ""
        var content = ""
        var pendingReasoning = StringBuilder()
        var pendingContent = StringBuilder()
        var isReasoningStreaming = true
        var lastUiUpdateAt = 0L

        fun flush(force: Boolean = false) {
          val now = System.currentTimeMillis()
          if (!force && now - lastUiUpdateAt < throttleMs) return
          if (pendingReasoning.isNotEmpty()) {
            reasoningContent += pendingReasoning.toString()
            pendingReasoning = StringBuilder()
          }
          if (pendingContent.isNotEmpty()) {
            content += pendingContent.toString()
            pendingContent = StringBuilder()
          }
          lastUiUpdateAt = now
          updateAssistant(
            messageId = assistantMessage.id,
            reasoningContent = reasoningContent,
            content = content,
            isReasoningStreaming = isReasoningStreaming,
          )
        }

        runCatching {
            repository.sendMessageStream(requestMessages).collect { event ->
              when (event) {
                is ChatStreamEvent.ReasoningToken -> {
                  pendingReasoning.append(event.value)
                  flush()
                }
                is ChatStreamEvent.ContentToken -> {
                  if (isReasoningStreaming) {
                    isReasoningStreaming = false
                    flush(force = true)
                  }
                  pendingContent.append(event.value)
                  flush()
                }
                ChatStreamEvent.Done -> Unit
              }
            }
          }
          .onSuccess {
            flush(force = true)
            finishAssistant(
              messageId = assistantMessage.id,
              reasoningContent = reasoningContent,
              content = content,
            )
          }
          .onFailure { throwable ->
            flush(force = true)
            val message = throwable.message ?: "未知错误"
            val bubbleText = content.ifBlank { message }
            finishAssistant(
              messageId = assistantMessage.id,
              reasoningContent = reasoningContent,
              content = bubbleText,
              errorMessage = message,
            )
          }
      }
  }

  fun stopGenerating() {
    val currentAssistant =
      _uiState.value.messages.lastOrNull {
        it.role == Role.ASSISTANT && it.isStreaming
      } ?: return
    streamJob?.cancel()
    finishAssistant(
      messageId = currentAssistant.id,
      reasoningContent = currentAssistant.reasoningContent,
      content = currentAssistant.content,
    )
  }

  fun clearChat() {
    streamJob?.cancel()
    _uiState.value = ChatUiState()
  }

  private fun updateAssistant(
    messageId: String,
    reasoningContent: String,
    content: String,
    isReasoningStreaming: Boolean,
  ) {
    _uiState.update { state ->
      state.copy(
        messages =
          state.messages.map { message ->
            if (message.id == messageId) {
              message.copy(
                reasoningContent = reasoningContent,
                content = content,
                isReasoningStreaming = isReasoningStreaming,
              )
            } else {
              message
            }
          }
      )
    }
    _events.tryEmit(
      ChatUiEvent.UpdateAssistantStream(
        messageId = messageId,
        reasoningContent = reasoningContent,
        content = content,
        isReasoningStreaming = isReasoningStreaming,
      )
    )
  }

  private fun finishAssistant(
    messageId: String,
    reasoningContent: String,
    content: String,
    errorMessage: String? = null,
  ) {
    _uiState.update { state ->
      state.copy(
        messages =
          state.messages.map { message ->
            if (message.id == messageId) {
              message.copy(
                reasoningContent = reasoningContent,
                content = content,
                isStreaming = false,
                isReasoningStreaming = false,
              )
            } else {
              message
            }
          },
        isGenerating = false,
        errorMessage = errorMessage,
      )
    }
    _events.tryEmit(
      ChatUiEvent.FinishAssistantMessage(
        messageId = messageId,
        reasoningContent = reasoningContent,
        content = content,
      )
    )
  }

  @Suppress("UNCHECKED_CAST")
  class Factory(
    private val repository: ChatRepository = ChatRepository(),
  ) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
      ChatViewModel(repository) as T
  }
}
