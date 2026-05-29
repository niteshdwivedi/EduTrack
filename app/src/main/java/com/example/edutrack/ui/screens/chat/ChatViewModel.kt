package com.example.edutrack.ui.screens.chat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.ChatRoom
import com.example.edutrack.data.model.ChatMessage
import com.example.edutrack.data.model.User
import com.example.edutrack.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _chatRooms = MutableStateFlow<List<ChatRoom>>(emptyList())
    val chatRooms: StateFlow<List<ChatRoom>> = _chatRooms

    private val _searchResults = MutableStateFlow<List<User>>(emptyList())
    val searchResults: StateFlow<List<User>> = _searchResults

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    init {
        loadChatRooms()
    }

    private fun loadChatRooms() {
        // Mocking chat rooms for Students, Teachers, and Section
        _chatRooms.value = listOf(
            ChatRoom("1", "Section A - CSE 2023", "Hello everyone!", System.currentTimeMillis(), true, emptyList(), "Section"),
            ChatRoom("2", "Prof. Jane Doe (OS)", "Please submit the assignment.", System.currentTimeMillis() - 100000, false, emptyList(), "Teacher"),
            ChatRoom("3", "Rahul Sharma", "Are you coming to the lab?", System.currentTimeMillis() - 500000, false, emptyList(), "Student")
        )
    }

    fun searchStudents(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        // Mock search results
        _searchResults.value = listOf(
            User("101", "Rahul Sharma", "rahul@uni.com", "2023001", "REG2023001", "B.Tech CSE", 6, university = "EduTrack University"),
            User("102", "Priya Singh", "priya@uni.com", "2023045", "REG2023045", "B.Tech CSE (AI/ML)", 6, university = "EduTrack University")
        ).filter { it.name.contains(query, ignoreCase = true) || it.rollNumber.contains(query, ignoreCase = true) }
    }

    fun loadMessages(roomId: String) {
        // Mock messages
        _messages.value = listOf(
            ChatMessage("1", "101", "Rahul Sharma", "Hey, did you finish the assignment?", System.currentTimeMillis() - 100000),
            ChatMessage("2", auth.currentUser?.uid ?: "current_user", "Me", "Working on it right now!", System.currentTimeMillis() - 50000)
        )
    }

    fun sendMessage(roomId: String, text: String, attachmentUrl: String = "", attachmentName: String = "") {
        val newMessage = ChatMessage(
            id = System.currentTimeMillis().toString(),
            senderId = auth.currentUser?.uid ?: "current_user",
            senderName = "Me",
            text = text,
            timestamp = System.currentTimeMillis(),
            attachmentUrl = attachmentUrl,
            attachmentName = attachmentName
        )
        _messages.value = _messages.value + newMessage
    }
}
