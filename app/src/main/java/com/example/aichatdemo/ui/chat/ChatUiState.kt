package com.example.aichatdemo.ui.chat

import com.example.aichatdemo.data.model.ChatMessage

data class ChatUiState(
  val messages: List<ChatMessage> = emptyList(),
  val isGenerating: Boolean = false,
  val errorMessage: String? = null,
)
