package com.example.aichatdemo.ui.chat.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

object DeepSeekColors {
  val Blue = Color(0xFF4D6BFE)
  val BlueDark = Color(0xFF4166D5)
  val BlueLight = Color(0xFFEEF2FF)
  val Background = Color(0xFFF9FBFF)
  val Surface = Color(0xFFFFFFFF)
  val TextPrimary = Color(0xFF1F2329)
  val TextSecondary = Color(0xFF6B7280)
  val TextTertiary = Color(0xFF9CA3AF)
  val Border = Color(0xFFE5E7EB)
  val ReasoningBg = Color(0xFFF3F4F6)
  val ErrorBg = Color(0xFFFEF2F2)
  val ErrorText = Color(0xFFDC2626)
}

private val LightColorScheme =
  lightColorScheme(
    primary = DeepSeekColors.Blue,
    onPrimary = Color.White,
    background = DeepSeekColors.Background,
    surface = DeepSeekColors.Surface,
    onBackground = DeepSeekColors.TextPrimary,
    onSurface = DeepSeekColors.TextPrimary,
    outline = DeepSeekColors.Border,
  )

@Composable
fun DeepSeekTheme(content: @Composable () -> Unit) {
  MaterialTheme(
    colorScheme = LightColorScheme,
    content = content,
  )
}
