package com.example.aichatdemo.network

/** 去除 API Key 等凭证中的控制字符，避免 OkHttp Authorization 头校验失败。 */
internal fun String.sanitizeAsHttpCredential(): String =
  filter { !it.isISOControl() }.trim()
