package com.example.aichatdemo.ui.chat

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.aichatdemo.ui.chat.theme.DeepSeekTheme

class ChatActivity : ComponentActivity() {
  private val viewModel: ChatViewModel by viewModels { ChatViewModel.Factory() }

  override fun onCreate(savedInstanceState: Bundle?) {
    enableEdgeToEdge()
    super.onCreate(savedInstanceState)
    setContent {
      val uiState by viewModel.uiState.collectAsStateWithLifecycle()
      DeepSeekTheme {
        ChatScreen(
          uiState = uiState,
          onSend = viewModel::sendMessage,
          onStop = viewModel::stopGenerating,
          onNewChat = viewModel::clearChat,
          onSuggestionClick = viewModel::sendMessage,
        )
      }
    }
  }
}
