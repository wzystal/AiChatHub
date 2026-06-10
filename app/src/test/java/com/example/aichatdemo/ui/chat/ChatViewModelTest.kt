package com.example.aichatdemo.ui.chat

import com.example.aichatdemo.data.ChatRepository
import com.example.aichatdemo.data.model.ChatMessage
import com.example.aichatdemo.network.AiStreamClient
import com.example.aichatdemo.network.ChatStreamEvent
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.Test
import org.junit.Rule
import org.junit.runner.Description

@OptIn(ExperimentalCoroutinesApi::class)
class ChatViewModelTest {
  @get:Rule val mainDispatcherRule: TestWatcher = MainDispatcherRule()

  @Test
  fun sendMessage_streamsAssistantReplyIntoState() = runTest {
    val viewModel =
      ChatViewModel(
        repository = ChatRepository(FakeStreamClient(contentTokens = listOf("你", "好"))),
        throttleMs = 0L,
      )
    val events = mutableListOf<ChatUiEvent>()
    val collectJob = launch(UnconfinedTestDispatcher(testScheduler)) {
      viewModel.events.collect { events += it }
    }

    viewModel.sendMessage("hi")
    testScheduler.advanceUntilIdle()

    assertEquals(2, viewModel.uiState.value.messages.size)
    assertEquals("你好", viewModel.uiState.value.messages.last().content)
    assertFalse(viewModel.uiState.value.messages.last().isStreaming)
    assertFalse(viewModel.uiState.value.isGenerating)
    assertEquals(1, events.filterIsInstance<ChatUiEvent.AppendMessages>().size)
    assertEquals(1, events.filterIsInstance<ChatUiEvent.FinishAssistantMessage>().size)

    collectJob.cancel()
  }

  @Test
  fun sendMessage_streamsReasoningBeforeContent() = runTest {
    val viewModel =
      ChatViewModel(
        repository =
          ChatRepository(
            FakeStreamClient(
              reasoningTokens = listOf("思", "考"),
              contentTokens = listOf("回", "答"),
            )
          ),
        throttleMs = 0L,
      )

    viewModel.sendMessage("hi")
    testScheduler.advanceUntilIdle()

    val assistant = viewModel.uiState.value.messages.last()
    assertEquals("思考", assistant.reasoningContent)
    assertEquals("回答", assistant.content)
    assertFalse(assistant.isReasoningStreaming)
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
private class MainDispatcherRule(
  private val dispatcher: TestDispatcher = UnconfinedTestDispatcher(),
) : TestWatcher() {
  override fun starting(description: Description) {
    Dispatchers.setMain(dispatcher)
  }

  override fun finished(description: Description) {
    Dispatchers.resetMain()
  }
}

private class FakeStreamClient(
  private val reasoningTokens: List<String> = emptyList(),
  private val contentTokens: List<String>,
) : AiStreamClient {
  override fun stream(messages: List<ChatMessage>): Flow<ChatStreamEvent> = flow {
    reasoningTokens.forEach { emit(ChatStreamEvent.ReasoningToken(it)) }
    contentTokens.forEach { emit(ChatStreamEvent.ContentToken(it)) }
    emit(ChatStreamEvent.Done)
  }
}
