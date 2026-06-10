package com.example.aichatdemo.network

sealed interface ChatStreamEvent {
  data class ReasoningToken(val value: String) : ChatStreamEvent

  data class ContentToken(val value: String) : ChatStreamEvent

  data object Done : ChatStreamEvent
}
