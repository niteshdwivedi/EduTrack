package com.example.edutrack.ui.screens.teacher_absent

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.TeacherAbsentEntry
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
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
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                _absences.value = listOf(
                    TeacherAbsentEntry("1", "Dr. Arhaan Roy", "Operating Systems", today, "10:00", "11:30", "Period 1"),
                    TeacherAbsentEntry("2", "Prof. Sharma", "Python Class", today, "02:00", "03:30", "Period 3"),
                    TeacherAbsentEntry("3", "Dr. Amit Verma", "Mathematics", today, "09:00", "10:30", "Period 1")
                )
            } else {
                _absences.value = data
            }
            _isLoading.value = false
        }
    }
}
