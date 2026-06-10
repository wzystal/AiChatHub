package com.example.aichatdemo.network

import com.example.aichatdemo.BuildConfig

object AiClientProvider {
  fun create(): AiStreamClient {
    val apiKey = BuildConfig.DEEPSEEK_API_KEY.trim()
    if (apiKey.isBlank()) {
      return MockAiStreamClient(
        prefix = "未配置 DeepSeek API Key。请在 local.properties 中添加 deepseek.api.key=你的密钥，或在环境变量中设置 DEEPSEEK_API_KEY。\n\n"
      )
    }

    val baseUrl = BuildConfig.DEEPSEEK_BASE_URL.trim().trimEnd('/')
    return OkHttpSseClient(
      endpoint = "$baseUrl/chat/completions",
      apiKey = apiKey,
      model = BuildConfig.DEEPSEEK_MODEL,
    )
  }
}
