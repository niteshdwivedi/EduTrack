package com.example.edutrack.ui.screens.search

import androidx.lifecycle.ViewModel
import com.example.edutrack.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SearchItem(
    val name: String,
    val route: String
)

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val allSearchItems = listOf(
        SearchItem("Attendance", Screen.Attendance.route),
        SearchItem("Timetable", Screen.Timetable.route),
        SearchItem("Teacher Absent", Screen.TeacherAbsent.route),
        SearchItem("Makeup Class", Screen.MakeupClass.route),
        SearchItem("Reminders", Screen.Reminders.route),
        SearchItem("Notes", Screen.Notes.route),
        SearchItem("Assignments", Screen.Assignments.route),
        SearchItem("Exams", Screen.Exams.route),
        SearchItem("GPA Calculator", Screen.GPACalculator.route),
        SearchItem("Study Timer", Screen.StudyTimer.route),
        SearchItem("Job Portal", Screen.JobPortal.route),
        SearchItem("Resources", Screen.Resources.route),
        SearchItem("Analytics", Screen.Analytics.route),
        SearchItem("Profile", Screen.Profile.route),
        SearchItem("Settings", Screen.Settings.route)
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(allSearchItems)
    val searchResults: StateFlow<List<SearchItem>> = _searchResults.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        if (query.isEmpty()) {
            _searchResults.value = allSearchItems
        } else {
            // Fuzzy search logic
            _searchResults.value = allSearchItems.sortedByDescending { 
                calculateSimilarity(it.name.lowercase(), query.lowercase())
            }.filter { 
                calculateSimilarity(it.name.lowercase(), query.lowercase()) > 0.3
            }
        }
    }

    private fun calculateSimilarity(s1: String, s2: String): Double {
        if (s1.contains(s2)) return 1.0
        
        // Very simple fuzzy logic: count matching characters in order
        var matches = 0
        var s1Idx = 0
        var s2Idx = 0
        while (s1Idx < s1.length && s2Idx < s2.length) {
            if (s1[s1Idx] == s2[s2Idx]) {
                matches++
                s2Idx++
            }
            s1Idx++
        }
        
        return matches.toDouble() / s2.length
    }
}
