package com.example.edutrack.ui.screens.jobs

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.Job
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class JobPortalViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _jobs = MutableStateFlow<List<Job>>(emptyList())
    val jobs: StateFlow<List<Job>> = _jobs

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadJobs()
    }

    fun loadJobs() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getJobs()
            if (data.isEmpty()) {
                // Initial dummy data if Firestore is empty
                _jobs.value = listOf(
                    Job("1", "Junior Android Developer", "TechCorp", "Remote", "Full-time"),
                    Job("2", "Software Engineer Intern", "InnovateID", "New York", "Internship"),
                    Job("3", "Kotlin Backend Developer", "CloudScale", "London", "Full-time")
                )
            } else {
                _jobs.value = data
            }
            _isLoading.value = false
        }
    }
}
