package com.example.aichatdemo.ui.chat

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aichatdemo.R
import com.example.aichatdemo.data.model.ChatMessage
import com.example.aichatdemo.data.model.Role
import com.example.aichatdemo.ui.chat.component.MarkdownText
import com.example.aichatdemo.ui.chat.theme.DeepSeekColors

@Composable
fun ChatScreen(
  uiState: ChatUiState,
  onSend: (String) -> Unit,
  onStop: () -> Unit,
  onNewChat: () -> Unit,
  onSuggestionClick: (String) -> Unit,
) {
  var input by remember { mutableStateOf("") }
  val listState = rememberLazyListState()
  var stickToBottom by remember { mutableStateOf(true) }

  val listNestedScrollConnection =
    remember {
      object : NestedScrollConnection {
        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
          if (source == NestedScrollSource.UserInput && available.y < 0f) {
            stickToBottom = false
          }
          return Offset.Zero
        }
      }
    }

  LaunchedEffect(uiState.messages.size) {
    stickToBottom = true
  }

  LaunchedEffect(listState) {
    snapshotFlow {
      val info = listState.layoutInfo
      val total = info.totalItemsCount
      if (total == 0) {
        true
      } else {
        val lastVisible = info.visibleItemsInfo.lastOrNull()?.index ?: -1
        lastVisible >= total - 1
      }
    }.collect { atBottom ->
      if (atBottom) {
        stickToBottom = true
      }
    }
  }

  LaunchedEffect(
    uiState.messages.size,
    uiState.messages.lastOrNull()?.content,
    uiState.messages.lastOrNull()?.reasoningContent,
    stickToBottom,
  ) {
    if (stickToBottom && uiState.messages.isNotEmpty()) {
      val lastIndex = uiState.messages.lastIndex
      listState.scrollToItem(lastIndex)
    }
  }

  Column(
    modifier =
      Modifier
        .fillMaxSize()
        .background(DeepSeekColors.Background)
        .statusBarsPadding(),
  ) {
    ChatTopBar(onNewChat = onNewChat)

    if (uiState.errorMessage != null) {
      ErrorBanner(message = uiState.errorMessage)
    }

    if (uiState.messages.isEmpty()) {
      EmptyChatState(
        modifier = Modifier.weight(1f),
        onSuggestionClick = onSuggestionClick,
      )
    } else {
      LazyColumn(
        modifier = Modifier.weight(1f).nestedScroll(listNestedScrollConnection),
        state = listState,
        contentPadding =
          androidx.compose.foundation.layout.PaddingValues(
            horizontal = 16.dp,
            vertical = 12.dp,
          ),
        verticalArrangement = Arrangement.spacedBy(20.dp),
      ) {
        items(uiState.messages, key = { it.id }) { message ->
          ChatMessageItem(message = message)
        }
      }
    }

    ChatInputBar(
      value = input,
      onValueChange = { input = it },
      isGenerating = uiState.isGenerating,
      onSend = {
        onSend(input)
        input = ""
      },
      onStop = onStop,
      modifier = Modifier.navigationBarsPadding().imePadding(),
    )
  }
}

@Composable
private fun ChatTopBar(onNewChat: () -> Unit) {
  Surface(color = DeepSeekColors.Surface, shadowElevation = 0.5.dp) {
    Column {
      Row(
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        IconButton(onClick = { /* 侧边栏占位 */ }) {
          Icon(
            painter = painterResource(R.drawable.ic_menu),
            contentDescription = stringResource(R.string.menu),
            tint = DeepSeekColors.TextPrimary,
            modifier = Modifier.size(22.dp),
          )
        }
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = stringResource(R.string.chat_title),
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = DeepSeekColors.TextPrimary,
          )
          Text(
            text = stringResource(R.string.chat_subtitle),
            fontSize = 12.sp,
            color = DeepSeekColors.TextSecondary,
          )
        }
        IconButton(onClick = onNewChat) {
          Icon(
            painter = painterResource(R.drawable.ic_add),
            contentDescription = stringResource(R.string.new_chat),
            tint = DeepSeekColors.TextPrimary,
            modifier = Modifier.size(22.dp),
          )
        }
      }
      HorizontalDivider(color = DeepSeekColors.Border, thickness = 0.5.dp)
    }
  }
}

@Composable
private fun ErrorBanner(message: String) {
  Text(
    text = message,
    modifier =
      Modifier
        .fillMaxWidth()
        .background(DeepSeekColors.ErrorBg)
        .padding(horizontal = 16.dp, vertical = 10.dp),
    color = DeepSeekColors.ErrorText,
    fontSize = 13.sp,
    lineHeight = 18.sp,
  )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun EmptyChatState(
  modifier: Modifier = Modifier,
  onSuggestionClick: (String) -> Unit,
) {
  val allSuggestions = stringArrayResource(R.array.suggestion_pool).toList()
  val displayedSuggestions = remember { allSuggestions.shuffled().take(4) }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .padding(horizontal = 24.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.Center,
  ) {
    Icon(
      painter = painterResource(R.drawable.ic_deepseek_logo),
      contentDescription = null,
      tint = androidx.compose.ui.graphics.Color.Unspecified,
      modifier = Modifier.size(56.dp),
    )
    Spacer(modifier = Modifier.height(20.dp))
    Text(
      text = stringResource(R.string.empty_greeting),
      fontSize = 22.sp,
      fontWeight = FontWeight.SemiBold,
      color = DeepSeekColors.TextPrimary,
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
      text = stringResource(R.string.empty_subtitle),
      fontSize = 14.sp,
      color = DeepSeekColors.TextSecondary,
    )
    Spacer(modifier = Modifier.height(28.dp))
    FlowRow(
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
      displayedSuggestions.forEach { text ->
        SuggestionChip(text = text, onClick = { onSuggestionClick(text) })
      }
    }
  }
}

@Composable
private fun SuggestionChip(text: String, onClick: () -> Unit) {
  Surface(
    modifier = Modifier.clickable(onClick = onClick),
    shape = RoundedCornerShape(20.dp),
    color = DeepSeekColors.Surface,
    border = androidx.compose.foundation.BorderStroke(1.dp, DeepSeekColors.Border),
  ) {
    Text(
      text = text,
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
      fontSize = 14.sp,
      color = DeepSeekColors.TextPrimary,
    )
  }
}

@Composable
private fun ChatMessageItem(message: ChatMessage) {
  when (message.role) {
    Role.USER -> UserMessageBubble(content = message.content)
    Role.ASSISTANT -> AssistantMessageContent(message = message)
  }
}

@Composable
private fun UserMessageBubble(content: String) {
  Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
    Surface(
      shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp, bottomEnd = 4.dp, bottomStart = 20.dp),
      color = DeepSeekColors.Blue,
    ) {
      Text(
        text = content,
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp).widthIn(max = 280.dp),
        color = androidx.compose.ui.graphics.Color.White,
        fontSize = 16.sp,
        lineHeight = 24.sp,
      )
    }
  }
}

@Composable
private fun AssistantMessageContent(message: ChatMessage) {
  var reasoningExpanded by remember(message.id) { mutableStateOf(message.isReasoningStreaming) }

  LaunchedEffect(message.isReasoningStreaming, message.content) {
    if (!message.isReasoningStreaming && message.content.isNotEmpty()) {
      reasoningExpanded = false
    }
    if (message.isReasoningStreaming) {
      reasoningExpanded = true
    }
  }

  Row(modifier = Modifier.fillMaxWidth()) {
    Box(
      modifier =
        Modifier
          .size(32.dp)
          .clip(CircleShape)
          .background(DeepSeekColors.Blue),
      contentAlignment = Alignment.Center,
    ) {
      Icon(
        painter = painterResource(R.drawable.ic_deepseek_logo),
        contentDescription = null,
        tint = androidx.compose.ui.graphics.Color.Unspecified,
        modifier = Modifier.size(22.dp),
      )
    }
    Spacer(modifier = Modifier.width(10.dp))
    Column(modifier = Modifier.weight(1f)) {
      if (message.isReasoningStreaming || message.reasoningContent.isNotEmpty()) {
        ReasoningCard(
          content = message.reasoningContent,
          isStreaming = message.isReasoningStreaming,
          expanded = reasoningExpanded,
          onToggle = {
            if (!message.isReasoningStreaming) {
              reasoningExpanded = !reasoningExpanded
            }
          },
        )
        Spacer(modifier = Modifier.height(10.dp))
      }

      when {
        message.content.isNotEmpty() -> {
          MarkdownText(
            markdown = message.content,
            modifier = Modifier.fillMaxWidth(),
          )
          if (message.isStreaming) {
            Spacer(modifier = Modifier.height(4.dp))
            StreamingCursor()
          }
        }
        message.isStreaming && !message.isReasoningStreaming -> {
          ThinkingDots(label = stringResource(R.string.reasoning_thinking))
        }
        message.isStreaming && message.reasoningContent.isEmpty() -> {
          ThinkingDots(label = stringResource(R.string.reasoning_thinking))
        }
      }
    }
  }
}

@Composable
private fun ReasoningCard(
  content: String,
  isStreaming: Boolean,
  expanded: Boolean,
  onToggle: () -> Unit,
) {
  val displayContent =
    when {
      content.isNotEmpty() -> content
      isStreaming -> "正在组织思路..."
      else -> ""
    }

  Surface(
    modifier = Modifier.fillMaxWidth(),
    shape = RoundedCornerShape(12.dp),
    color = DeepSeekColors.ReasoningBg,
    border = androidx.compose.foundation.BorderStroke(1.dp, DeepSeekColors.Border),
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggle),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        Icon(
          painter = painterResource(R.drawable.ic_sparkle),
          contentDescription = null,
          tint = DeepSeekColors.Blue,
          modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
          text =
            if (isStreaming) {
              stringResource(R.string.reasoning_thinking)
            } else if (expanded) {
              stringResource(R.string.reasoning_expanded)
            } else {
              stringResource(R.string.reasoning_collapsed)
            },
          fontSize = 13.sp,
          fontWeight = FontWeight.Medium,
          color = DeepSeekColors.TextSecondary,
          modifier = Modifier.weight(1f),
        )
        if (isStreaming) {
          ThinkingDotsCompact()
        } else {
          Icon(
            painter =
              painterResource(
                if (expanded) {
                  R.drawable.ic_chevron_up
                } else {
                  R.drawable.ic_chevron_down
                }
              ),
            contentDescription = null,
            tint = DeepSeekColors.TextSecondary,
            modifier = Modifier.size(16.dp),
          )
        }
      }
      if ((expanded || isStreaming) && displayContent.isNotEmpty()) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
          text = displayContent,
          fontSize = 14.sp,
          lineHeight = 21.sp,
          color = DeepSeekColors.TextSecondary,
        )
      }
    }
  }
}

@Composable
private fun ThinkingDots(label: String) {
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = label, fontSize = 14.sp, color = DeepSeekColors.TextSecondary)
    Spacer(modifier = Modifier.width(4.dp))
    ThinkingDotsCompact()
  }
}

@Composable
private fun ThinkingDotsCompact() {
  val transition = rememberInfiniteTransition(label = "thinking")
  val alpha1 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, easing = LinearEasing), RepeatMode.Reverse),
    label = "dot1",
  )
  val alpha2 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, delayMillis = 200, easing = LinearEasing), RepeatMode.Reverse),
    label = "dot2",
  )
  val alpha3 by transition.animateFloat(
    initialValue = 0.3f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(600, delayMillis = 400, easing = LinearEasing), RepeatMode.Reverse),
    label = "dot3",
  )
  Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
    Box(
      Modifier.size(5.dp).alpha(alpha1).clip(CircleShape).background(DeepSeekColors.Blue),
    )
    Box(
      Modifier.size(5.dp).alpha(alpha2).clip(CircleShape).background(DeepSeekColors.Blue),
    )
    Box(
      Modifier.size(5.dp).alpha(alpha3).clip(CircleShape).background(DeepSeekColors.Blue),
    )
  }
}

@Composable
private fun StreamingCursor() {
  val transition = rememberInfiniteTransition(label = "cursor")
  val alpha by transition.animateFloat(
    initialValue = 1f,
    targetValue = 0.2f,
    animationSpec = infiniteRepeatable(tween(500), RepeatMode.Reverse),
    label = "cursorAlpha",
  )
  Box(
    modifier =
      Modifier
        .size(width = 8.dp, height = 16.dp)
        .alpha(alpha)
        .clip(RoundedCornerShape(1.dp))
        .background(DeepSeekColors.Blue),
  )
}

@Composable
private fun ChatInputBar(
  value: String,
  onValueChange: (String) -> Unit,
  isGenerating: Boolean,
  onSend: () -> Unit,
  onStop: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val canSend = value.isNotBlank() && !isGenerating

  Surface(
    modifier = modifier.fillMaxWidth(),
    color = DeepSeekColors.Surface,
    shadowElevation = 8.dp,
  ) {
    Column {
      HorizontalDivider(color = DeepSeekColors.Border, thickness = 0.5.dp)
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.Bottom,
      ) {
        Surface(
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(24.dp),
          color = DeepSeekColors.Background,
          border = androidx.compose.foundation.BorderStroke(1.dp, DeepSeekColors.Border),
        ) {
          BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier =
              Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            textStyle =
              TextStyle(
                fontSize = 16.sp,
                color = DeepSeekColors.TextPrimary,
                lineHeight = 22.sp,
              ),
            cursorBrush = SolidColor(DeepSeekColors.Blue),
            maxLines = 4,
            decorationBox = { inner ->
              Box {
                if (value.isEmpty()) {
                  Text(
                    text = stringResource(R.string.input_hint),
                    color = DeepSeekColors.TextTertiary,
                    fontSize = 16.sp,
                  )
                }
                inner()
              }
            },
          )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Surface(
          modifier =
            Modifier
              .size(48.dp)
              .clip(CircleShape)
              .clickable(enabled = isGenerating || canSend) {
                if (isGenerating) onStop() else onSend()
              },
          color =
            when {
              isGenerating -> DeepSeekColors.Blue
              canSend -> DeepSeekColors.Blue
              else -> DeepSeekColors.Border
            },
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              painter =
                painterResource(
                  if (isGenerating) {
                    R.drawable.ic_stop
                  } else {
                    R.drawable.ic_send
                  }
                ),
              contentDescription =
                stringResource(
                  if (isGenerating) {
                    R.string.stop
                  } else {
                    R.string.send
                  }
                ),
              tint =
                if (isGenerating || canSend) {
                  androidx.compose.ui.graphics.Color.White
                } else {
                  DeepSeekColors.TextTertiary
                },
              modifier = Modifier.size(if (isGenerating) 24.dp else 32.dp),
            )
          }
        }
      }
    }
  }
}
