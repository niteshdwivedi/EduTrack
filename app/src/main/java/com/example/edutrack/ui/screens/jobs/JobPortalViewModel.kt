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
                _jobs.value = getMockJobs()
            } else {
                _jobs.value = data
            }
            _isLoading.value = false
        }
    }

    private fun getMockJobs(): List<Job> {
        return listOf(
            Job(
                jobId = "JOB001",
                role = "Cloud Engineer",
                company = "Cognizant",
                location = "Remote",
                packageVal = "32 LPA",
                skills = "AWS, Docker",
                lastDate = "15-06-2026",
                applyLink = "https://careers.infosys.com",
                campusType = "On Campus"
            ),
            Job(
                jobId = "JOB002",
                role = "Android Developer",
                company = "Capgemini",
                location = "Remote",
                packageVal = "18 LPA",
                skills = "Python, ML",
                lastDate = "25-07-2026",
                applyLink = "https://careers.infosys.com",
                campusType = "Off Campus"
            ),
            Job(
                jobId = "JOB003",
                role = "Android Developer",
                company = "IBM",
                location = "Bangalore",
                packageVal = "19 LPA",
                skills = "React, Node",
                lastDate = "16-07-2026",
                applyLink = "https://careers.infosys.com",
                campusType = "On Campus"
            )
        )
    }
}
