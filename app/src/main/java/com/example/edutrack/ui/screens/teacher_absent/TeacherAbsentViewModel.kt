package com.example.edutrack.ui.screens.teacher_absent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.TeacherAbsentEntry
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherAbsentViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _absences = MutableStateFlow<List<TeacherAbsentEntry>>(emptyList())
    val absences: StateFlow<List<TeacherAbsentEntry>> = _absences

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAbsences()
    }

    fun loadAbsences() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getTeacherAbsences()
            if (data.isEmpty()) {
                // Initial dummy data if Firestore is empty
                _absences.value = listOf(
                    TeacherAbsentEntry("1", "Dr. Smith", "Mathematics", "2023-11-15", "Period 2"),
                    TeacherAbsentEntry("2", "Prof. Johnson", "Physics", "2023-11-16", "Period 4"),
                    TeacherAbsentEntry("3", "Ms. Davis", "Computer Science", "2023-11-16", "Period 1")
                )
            } else {
                _absences.value = data
            }
            _isLoading.value = false
        }
    }
}
