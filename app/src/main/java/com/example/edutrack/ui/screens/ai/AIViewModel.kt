package com.example.edutrack.ui.screens.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.repository.AIRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ChatMessage(
    val text: String,
    val isUser: Boolean
)

@HiltViewModel
class AIViewModel @Inject constructor(
    private val aiRepository: AIRepository
) : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(
        listOf(ChatMessage("Hello! I'm your EduTrack AI assistant. How can I help you with your studies today?", false))
    )
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun sendMessage(text: String) {
        if (text.isBlank()) return

        viewModelScope.launch {
            _messages.value = _messages.value + ChatMessage(text, true)
            _isLoading.value = true
            
            val response = aiRepository.getAIResponse(text)
            
            _messages.value = _messages.value + ChatMessage(response ?: "Sorry, I couldn't process that.", false)
            _isLoading.value = false
        }
    }
}
