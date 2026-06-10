package com.example.aichatdemo.data

import com.example.aichatdemo.data.model.ChatMessage
import com.example.aichatdemo.network.AiStreamClient
import com.example.aichatdemo.network.ChatStreamEvent
import com.example.aichatdemo.network.AiClientProvider
import kotlinx.coroutines.flow.Flow

class ChatRepository(
  private val streamClient: AiStreamClient = AiClientProvider.create(),
) {
  fun sendMessageStream(messages: List<ChatMessage>): Flow<ChatStreamEvent> =
    streamClient.stream(messages)
}
