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
            val data = repository.getNotes()
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
            Note(noteId = "1", title = "Java Basics", content = "Introduction to OOP concepts, classes, and objects.", subject = "Advanced Java", date = "Nov 10, 2023", color = 0xFFFFE0B2),
            Note(noteId = "2", title = "OS Scheduling", content = "Process scheduling algorithms: FCFS, SJF, Round Robin.", subject = "Operating Systems", date = "Nov 12, 2023", color = 0xFFC8E6C9),
            Note(noteId = "3", title = "SQL Queries", content = "Complex joins, subqueries and indexing strategies.", subject = "Database Systems", date = "Nov 14, 2023", color = 0xFFBBDEFB),
            Note(noteId = "4", title = "Kotlin Coroutines", content = "Structured concurrency and suspend functions.", subject = "Mobile Dev", date = "Nov 15, 2023", color = 0xFFF8BBD0),
            Note(noteId = "5", title = "Networking Layers", content = "OSI Model and TCP/IP stack explanation.", subject = "Computer Networks", date = "Nov 15, 2023", color = 0xFFD1C4E9)
        )
    }

    fun addNote(title: String, content: String, subject: String) {
        viewModelScope.launch {
            val note = Note(
                noteId = System.currentTimeMillis().toString(),
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
