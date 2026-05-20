package com.example.edutrack.ui.screens.search

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor() : ViewModel() {

    private val allItems = listOf(
        "Attendance", "Timetable", "Teacher Absent", "Makeup Class", 
        "Reminders", "Notes", "Assignments", "Exams", 
        "GPA Calculator", "Study Timer", "Job Portal", 
        "Resources", "Analytics", "Profile", "Settings"
    )

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _searchResults = MutableStateFlow(allItems)
    val searchResults: StateFlow<List<String>> = _searchResults.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
        _searchResults.value = if (query.isEmpty()) {
            allItems
        } else {
            allItems.filter { it.contains(query, ignoreCase = true) }
        }
    }
}
