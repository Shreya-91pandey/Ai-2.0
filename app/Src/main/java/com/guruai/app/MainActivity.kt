package com.example.guruai.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.guruai.data.local.GuruDatabase
import com.example.guruai.data.remote.GeminiApiService
import com.example.guruai.data.repository.GuruRepository
import com.example.guruai.ui.screens.GuruChatScreen
import com.example.guruai.ui.theme.GuruAITheme
import com.example.guruai.util.DisplayRefreshHelper

class MainActivity : ComponentActivity() {

    private val viewModel: GuruViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val db = GuruDatabase.getInstance(applicationContext)
                val api = GeminiApiService.create()
                val repo = GuruRepository(applicationContext, db.guruDao(), api)
                @Suppress("UNCHECKED_CAST")
                return GuruViewModel(repo) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Optimize display refresh rate for 120Hz/90Hz smooth animations (Claude classes.dex)
        DisplayRefreshHelper.optimizeHighRefreshRate(window)

        handleIntent(intent)

        setContent {
            GuruAITheme {
                val state = viewModel.uiState.collectAsState().value
                GuruChatScreen(
                    state = state,
                    onSendMessage = { prompt -> viewModel.sendPrompt(prompt) },
                    onDismissShare = { viewModel.clearPendingShare() }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    private fun handleIntent(intent: Intent?) {
        if (intent == null) return
        val sharedText = intent.getStringExtra("EXTRA_SHARED_TEXT")
        val sharedUri = intent.getStringExtra("EXTRA_SHARED_URI")
        val mimeType = intent.getStringExtra("EXTRA_MIME_TYPE")

        if (sharedText != null || sharedUri != null) {
            viewModel.onIncomingSharedContent(sharedText, sharedUri, mimeType)
        }
    }
}
