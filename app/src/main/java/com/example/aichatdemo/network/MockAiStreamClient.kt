package com.example.aichatdemo.network

import com.example.aichatdemo.data.model.ChatMessage
import com.example.aichatdemo.data.model.Role
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class MockAiStreamClient(
  private val prefix: String = "",
) : AiStreamClient {
  override fun stream(messages: List<ChatMessage>): Flow<ChatStreamEvent> = flow {
    val question = messages.lastOrNull { it.role == Role.USER }?.content.orEmpty()
    val reasoning =
      buildString {
        append("1. 用户问的是「")
        append(question.ifBlank { "你好" })
        append("」\n")
        append("2. 先确认身份与能力范围\n")
        append("3. 组织简洁、友好的中文回复")
      }
    val answer =
      buildString {
        append(prefix)
        append("你好！我是 **DeepSeek** 助手。\n\n")
        append("收到你的问题：**")
        append(question.ifBlank { "你好" })
        append("**\n\n")
        append("- 支持 **Markdown** 渲染\n")
        append("- 支持展示思考过程\n")
        append("> 当前为本地模拟数据")
      }

    reasoning.chunked(3).forEach { token ->
      delay(30)
      emit(ChatStreamEvent.ReasoningToken(token))
    }
    answer.chunked(2).forEach { token ->
      delay(35)
      emit(ChatStreamEvent.ContentToken(token))
    }
    emit(ChatStreamEvent.Done)
  }
}
