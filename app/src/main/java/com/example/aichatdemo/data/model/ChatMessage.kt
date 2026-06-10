package com.example.aichatdemo.data.model

data class ChatMessage(
  val id: String,
  val role: Role,
  val content: String,
  val reasoningContent: String = "",
  val isStreaming: Boolean = false,
  val isReasoningStreaming: Boolean = false,
)
