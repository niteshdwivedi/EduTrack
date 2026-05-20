package com.example.edutrack.ui.screens.notes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.Note
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _notes = MutableStateFlow<List<Note>>(emptyList())
    val notes: StateFlow<List<Note>> = _notes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadNotes()
    }

    fun loadNotes() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = "dummy_user"
            val data = repository.getNotes(userId)
            if (data.isEmpty()) {
                _notes.value = getMockNotes()
            } else {
                _notes.value = data
            }
            _isLoading.value = false
        }
    }

    private fun getMockNotes(): List<Note> {
        return listOf(
            Note("1", "Java Basics", "Introduction to OOP concepts, classes, and objects.", "Advanced Java", "Nov 10, 2023", 0xFFFFE0B2),
            Note("2", "OS Scheduling", "Process scheduling algorithms: FCFS, SJF, Round Robin.", "Operating Systems", "Nov 12, 2023", 0xFFC8E6C9),
            Note("3", "SQL Queries", "Complex joins, subqueries and indexing strategies.", "Database Systems", "Nov 14, 2023", 0xFFBBDEFB),
            Note("4", "Kotlin Coroutines", "Structured concurrency and suspend functions.", "Mobile Dev", "Nov 15, 2023", 0xFFF8BBD0),
            Note("5", "Networking Layers", "OSI Model and TCP/IP stack explanation.", "Computer Networks", "Nov 15, 2023", 0xFFD1C4E9)
        )
    }

    fun addNote(title: String, content: String, subject: String) {
        viewModelScope.launch {
            val note = Note(
                id = System.currentTimeMillis().toString(),
                title = title,
                content = content,
                subject = subject,
                date = "Nov 15, 2023",
                color = 0xFFFFFFFF
            )
            repository.addNote("dummy_user", note)
            loadNotes()
        }
    }
}
