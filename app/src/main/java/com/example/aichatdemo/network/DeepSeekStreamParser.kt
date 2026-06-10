package com.example.aichatdemo.network

import org.json.JSONObject

object DeepSeekStreamParser {
  data class StreamDelta(
    val reasoningContent: String? = null,
    val content: String? = null,
  )

  fun parseDelta(data: String): StreamDelta? {
    val json = JSONObject(data)
    val choices = json.optJSONArray("choices") ?: return null
    val delta = choices.optJSONObject(0)?.optJSONObject("delta") ?: return null
    val reasoningContent = readNonNullString(delta, "reasoning_content")
    val content = readNonNullString(delta, "content")
    if (reasoningContent == null && content == null) return null
    return StreamDelta(reasoningContent = reasoningContent, content = content)
  }

  fun parseContentToken(data: String): String? = parseDelta(data)?.content

  private fun readNonNullString(json: JSONObject, key: String): String? {
    if (!json.has(key)) return null
    return when (val value = json.opt(key)) {
      null, JSONObject.NULL -> null
      is String -> value.takeIf { it.isNotEmpty() }
      else -> value.toString().takeIf { it.isNotEmpty() }
    }
  }
}
