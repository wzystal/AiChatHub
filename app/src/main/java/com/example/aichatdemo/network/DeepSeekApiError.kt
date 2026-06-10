package com.example.aichatdemo.network

import org.json.JSONObject

object DeepSeekApiError {
  fun format(httpCode: Int, body: String?): String {
    val apiMessage = parseMessage(body)
    return when (httpCode) {
      401 -> "API Key 无效或已过期，请检查 local.properties 中的 deepseek.api.key"
      402 -> "DeepSeek 账户余额不足，请前往 platform.deepseek.com 充值后重试"
      429 -> "请求过于频繁，请稍后再试"
      in 500..599 -> "DeepSeek 服务暂时不可用（HTTP $httpCode），请稍后重试"
      else ->
        apiMessage?.let { translateKnownMessage(it) }
          ?: "DeepSeek 请求失败（HTTP $httpCode）"
    }
  }

  private fun parseMessage(body: String?): String? {
    if (body.isNullOrBlank()) return null
    return runCatching {
      JSONObject(body).optJSONObject("error")?.optString("message")?.takeIf { it.isNotBlank() }
    }.getOrNull()
  }

  private fun translateKnownMessage(message: String): String =
    when (message.lowercase()) {
      "insufficient balance" -> "DeepSeek 账户余额不足，请登录控制台充值后重试"
      "invalid api key" -> "API Key 无效，请检查 deepseek.api.key 配置"
      else -> message
    }
}
