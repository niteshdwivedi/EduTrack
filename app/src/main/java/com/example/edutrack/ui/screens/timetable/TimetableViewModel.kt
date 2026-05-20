package com.example.edutrack.ui.screens.timetable

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.TimetableEntry
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTimetable()
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _isLoading.value = true
            val userId = "dummy_user"
            val data = repository.getTimetable(userId)
            if (data.isEmpty()) {
                _timetable.value = getMockTimetable()
            } else {
                _timetable.value = data
            }
            _isLoading.value = false
        }
    }

    private fun getMockTimetable(): List<TimetableEntry> {
        return listOf(
            TimetableEntry("1", "Advanced Java", "CS301", "Dr. Robert Smith", "Room 402", "C-12", "09:00 AM", "10:30 AM", 1),
            TimetableEntry("2", "Operating Systems", "CS302", "Prof. Jane Doe", "Room 101", "C-05", "11:00 AM", "12:30 PM", 1, isTeacherAbsent = true),
            TimetableEntry("3", "Database Systems", "CS303", "Dr. Alan Turing", "Lab 01", "L-01", "02:00 PM", "03:30 PM", 2),
            TimetableEntry("4", "Mobile Dev", "CS304", "Mr. Steve Jobs", "Room 205", "C-08", "09:00 AM", "10:30 AM", 2, isMakeupClass = true),
            TimetableEntry("5", "Computer Networks", "CS305", "Dr. Vint Cerf", "Room 303", "C-10", "11:00 AM", "12:30 PM", 3),
            TimetableEntry("6", "Software Engineering", "CS306", "Prof. Margaret Hamilton", "Room 501", "C-15", "09:00 AM", "10:30 AM", 4),
            TimetableEntry("7", "Artificial Intelligence", "CS307", "Dr. Andrew Ng", "Lab 02", "L-02", "02:00 PM", "03:30 PM", 5)
        )
    }
}
