package com.example.aichatdemo.network

import com.example.aichatdemo.data.model.ChatMessage
import com.example.aichatdemo.data.model.Role
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

class OkHttpSseClient(
  private val endpoint: String,
  private val apiKey: String,
  private val model: String = "gpt-4.1-mini",
  private val client: OkHttpClient = OkHttpClient(),
) : AiStreamClient {
  override fun stream(messages: List<ChatMessage>): Flow<ChatStreamEvent> = flow {
    val request =
      Request.Builder()
        .url(endpoint)
        .addHeader("Authorization", "Bearer $apiKey")
        .addHeader("Accept", "text/event-stream")
        .post(buildRequestBody(messages).toRequestBody(JSON))
        .build()

    client.newCall(request).execute().use { response ->
      if (!response.isSuccessful) {
        val errorBody = response.body.string()
        error(DeepSeekApiError.format(response.code, errorBody))
      }

      val source = response.body.source()
      while (!source.exhausted()) {
        val line = source.readUtf8Line() ?: continue
        if (!line.startsWith("data:")) continue

        val data = line.removePrefix("data:").trim()
        if (data == "[DONE]") {
          emit(ChatStreamEvent.Done)
          return@use
        }

        val delta = DeepSeekStreamParser.parseDelta(data) ?: continue
        delta.reasoningContent?.let { emit(ChatStreamEvent.ReasoningToken(it)) }
        delta.content?.let { emit(ChatStreamEvent.ContentToken(it)) }
      }
      emit(ChatStreamEvent.Done)
    }
  }.flowOn(Dispatchers.IO)

  private fun buildRequestBody(messages: List<ChatMessage>): String {
    val payloadMessages =
      messages.joinToString(separator = ",") { message ->
        val role = if (message.role == Role.USER) "user" else "assistant"
        """{"role":"$role","content":"${message.content.jsonEscaped()}"}"""
      }
    return """{"model":"$model","stream":true,"messages":[$payloadMessages]}"""
  }

  private fun String.jsonEscaped(): String =
    buildString {
      for (char in this@jsonEscaped) {
        when (char) {
          '\\' -> append("\\\\")
          '"' -> append("\\\"")
          '\n' -> append("\\n")
          '\r' -> append("\\r")
          '\t' -> append("\\t")
          else -> append(char)
        }
      }
    }

  private companion object {
    val JSON = "application/json; charset=utf-8".toMediaType()
  }
}
