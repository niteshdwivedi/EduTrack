package com.example.edutrack.ui.screens.analytics

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

data class PerformanceMetric(
    val label: String,
    val value: String,
    val trend: String = ""
)

@HiltViewModel
class AnalyticsViewModel @Inject constructor() : ViewModel() {
    private val _metrics = MutableStateFlow(
        listOf(
            PerformanceMetric("Average GPA", "3.8", "↑ 0.2"),
            PerformanceMetric("Attendance Rate", "85%", "↓ 2%"),
            PerformanceMetric("Assignments Done", "12/15", "↑ 3"),
            PerformanceMetric("Study Hours/Week", "24h", "↑ 4h")
        )
    )
    val metrics: StateFlow<List<PerformanceMetric>> = _metrics
}