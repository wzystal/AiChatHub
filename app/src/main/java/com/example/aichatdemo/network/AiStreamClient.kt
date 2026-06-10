package com.example.aichatdemo.network

import com.example.aichatdemo.data.model.ChatMessage
import kotlinx.coroutines.flow.Flow

interface AiStreamClient {
  fun stream(messages: List<ChatMessage>): Flow<ChatStreamEvent>
}
