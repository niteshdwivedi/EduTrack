package com.example.edutrack.ui.screens.exams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.Exam
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _exams = MutableStateFlow<List<Exam>>(emptyList())
    val exams: StateFlow<List<Exam>> = _exams

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadExams()
    }

    fun loadExams() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = "dummy_user"
            val data = repository.getExams(userId)
            if (data.isEmpty()) {
                _exams.value = listOf(
                    Exam("1", "Mathematics", "2023-12-10", "09:00 AM", "Hall A"),
                    Exam("2", "Physics", "2023-12-12", "02:00 PM", "Lab 2"),
                    Exam("3", "Computer Science", "2023-12-15", "10:00 AM", "Room 204")
                )
            } else {
                _exams.value = data
            }
            _isLoading.value = false
        }
    }
}
