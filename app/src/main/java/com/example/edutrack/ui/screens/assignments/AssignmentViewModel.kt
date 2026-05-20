package com.example.edutrack.ui.screens.assignments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.Assignment
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AssignmentViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _assignments = MutableStateFlow<List<Assignment>>(emptyList())
    val assignments: StateFlow<List<Assignment>> = _assignments

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAssignments()
    }

    fun loadAssignments() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = "dummy_user" // In a real app, get from Auth
            val data = repository.getAssignments(userId)
            if (data.isEmpty()) {
                _assignments.value = listOf(
                    Assignment("1", "Calculus Homework", "Mathematics", "2023-11-20", "Pending"),
                    Assignment("2", "Lab Report", "Physics", "2023-11-22", "Completed")
                )
            } else {
                _assignments.value = data
            }
            _isLoading.value = false
        }
    }

    fun addAssignment(title: String, subject: String, dueDate: String) {
        viewModelScope.launch {
            val newAssignment = Assignment(
                id = System.currentTimeMillis().toString(),
                title = title,
                subject = subject,
                dueDate = dueDate,
                status = "Pending"
            )
            // For now, we update local state and repository (if implemented)
            _assignments.value = _assignments.value + newAssignment
            // repository.saveAssignment("dummy_user", newAssignment)
        }
    }

    fun markAsCompleted(id: String) {
        _assignments.value = _assignments.value.map {
            if (it.id == id) it.copy(status = "Completed") else it
        }
    }
}
