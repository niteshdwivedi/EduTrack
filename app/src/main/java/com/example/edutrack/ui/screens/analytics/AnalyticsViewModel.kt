package com.example.edutrack.ui.screens.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.repository.FirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PerformanceMetric(
    val label: String,
    val value: String,
    val trend: String = ""
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _metrics = MutableStateFlow<List<PerformanceMetric>>(emptyList())
    val metrics: StateFlow<List<PerformanceMetric>> = _metrics

    init {
        loadMetrics()
    }

    private fun loadMetrics() {
        val userId = auth.currentUser?.uid ?: "dummy_user"
        viewModelScope.launch {
            val attendanceList = repository.getAttendance(userId)
            val assignments = repository.getAssignments(userId)
            
            val avgAttendance = if (attendanceList.isNotEmpty()) {
                val totalAttended = attendanceList.sumOf { it.attended }
                val totalClasses = attendanceList.sumOf { it.total }
                if (totalClasses > 0) (totalAttended.toFloat() / totalClasses * 100).toInt() else 0
            } else 0

            val completedAssignments = assignments.count { it.status == "Completed" }
            val totalAssignments = assignments.size

            _metrics.value = listOf(
                PerformanceMetric("Average Attendance", "$avgAttendance%", if (avgAttendance >= 75) "Good" else "Low"),
                PerformanceMetric("Assignments Done", "$completedAssignments/$totalAssignments", ""),
                PerformanceMetric("Subjects Tracked", "${attendanceList.size}", ""),
                PerformanceMetric("Current Semester", "6", "")
            )
        }
    }
}
