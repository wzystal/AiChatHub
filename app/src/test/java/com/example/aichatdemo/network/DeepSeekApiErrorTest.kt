package com.example.aichatdemo.network

import junit.framework.TestCase.assertEquals
import org.junit.Test

class DeepSeekApiErrorTest {
  @Test
  fun format_returnsFriendlyMessageForInsufficientBalance() {
    val body =
      """{"error":{"message":"Insufficient Balance","type":"unknown_error","param":null,"code":"invalid_request_error"}}"""

    assertEquals(
      "DeepSeek 账户余额不足，请前往 platform.deepseek.com 充值后重试",
      DeepSeekApiError.format(402, body),
    )
  }

  @Test
  fun format_returnsFriendlyMessageForInvalidApiKey() {
    assertEquals(
      "API Key 无效或已过期，请检查 local.properties 中的 deepseek.api.key",
      DeepSeekApiError.format(401, null),
    )
  }
}
