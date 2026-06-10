package com.example.aichatdemo.ui.chat.component

import android.widget.TextView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.viewinterop.AndroidView
import com.example.aichatdemo.ui.chat.theme.DeepSeekColors
import io.noties.markwon.Markwon
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun MarkdownText(
  markdown: String,
  modifier: Modifier = Modifier,
  textColor: Int = DeepSeekColors.TextPrimary.toArgb(),
  textSizeSp: Float = 16f,
) {
  val markwon = rememberMarkwon()
  var renderedMarkdown by remember { mutableStateOf<CharSequence?>(null) }

  LaunchedEffect(markdown, markwon) {
    if (markdown.isEmpty()) {
      renderedMarkdown = null
      return@LaunchedEffect
    }
    val spanned =
      withContext(Dispatchers.Default) {
        markwon.render(markwon.parse(markdown))
      }
    renderedMarkdown = spanned
  }

  AndroidView(
    modifier = modifier,
    factory = { context ->
      TextView(context).apply {
        setTextColor(textColor)
        textSize = textSizeSp
        setLineSpacing(0f, 1.12f)
        includeFontPadding = false
      }
    },
    update = { textView ->
      textView.setTextColor(textColor)
      textView.textSize = textSizeSp
      textView.text = renderedMarkdown ?: ""
    },
  )
}

@Composable
private fun rememberMarkwon(): Markwon {
  val context = androidx.compose.ui.platform.LocalContext.current
  return remember(context) { Markwon.create(context) }
}
