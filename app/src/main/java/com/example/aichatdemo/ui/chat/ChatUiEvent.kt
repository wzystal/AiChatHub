package com.example.aichatdemo.ui.chat

import com.example.aichatdemo.data.model.ChatMessage

sealed interface ChatUiEvent {
  data class AppendMessages(val messages: List<ChatMessage>) : ChatUiEvent

  data class UpdateAssistantStream(
    val messageId: String,
    val reasoningContent: String,
    val content: String,
    val isReasoningStreaming: Boolean,
  ) : ChatUiEvent

  data class FinishAssistantMessage(
    val messageId: String,
    val reasoningContent: String,
    val content: String,
  ) : ChatUiEvent
}
