package com.example.edutrack.ui.screens.attendance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.AttendanceRecord
import com.example.edutrack.data.model.SubjectAttendance
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AttendanceViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _subjects = MutableStateFlow<List<SubjectAttendance>>(emptyList())
    val subjects: StateFlow<List<SubjectAttendance>> = _subjects

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadAttendance()
    }

    fun loadAttendance() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = "dummy_user"
            val data = repository.getAttendance(userId)
            if (data.isEmpty()) {
                _subjects.value = getMockAttendance()
            } else {
                _subjects.value = data
            }
            _isLoading.value = false
        }
    }

    private fun getMockAttendance(): List<SubjectAttendance> {
        val history = listOf(
            AttendanceRecord("1", "2023-10-01", "09:00 AM", "Present", "Room 402", "Dr. Robert Smith"),
            AttendanceRecord("2", "2023-10-03", "09:00 AM", "Present", "Room 402", "Dr. Robert Smith"),
            AttendanceRecord("3", "2023-10-05", "09:00 AM", "Absent", "Room 402", "Dr. Robert Smith")
        )
        return listOf(
            SubjectAttendance("1", "Advanced Java", "CS301", "Dr. Robert Smith", 18, 20, history),
            SubjectAttendance("2", "Operating Systems", "CS302", "Prof. Jane Doe", 14, 18, history),
            SubjectAttendance("3", "Database Systems", "CS303", "Dr. Alan Turing", 19, 20, history),
            SubjectAttendance("4", "Mobile Dev", "CS304", "Mr. Steve Jobs", 15, 15, history)
        )
    }
}
