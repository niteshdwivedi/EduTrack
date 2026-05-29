package com.example.edutrack.ui.screens.makeup_class

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.model.MakeupClass
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class MakeupClassViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _makeupClasses = MutableStateFlow<List<MakeupClass>>(emptyList())
    val makeupClasses: StateFlow<List<MakeupClass>> = _makeupClasses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _userRole = MutableStateFlow("Student")
    val userRole: StateFlow<String> = _userRole

    init {
        loadMakeupClasses()
        viewModelScope.launch {
            settingsDataStore.userRole.collect { _userRole.value = it ?: "Student" }
        }
    }

    fun loadMakeupClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getMakeupClasses()
            if (data.isEmpty()) {
                val today = java.text.SimpleDateFormat("dd-MM-yyyy", java.util.Locale.getDefault()).format(java.util.Date())
                _makeupClasses.value = listOf(
                    MakeupClass("1", "DBMS", "Dr. Mike", today, "02:00 PM - 03:30 PM", "105"),
                    MakeupClass("2", "Digital Logic", "Prof. Alan", today, "10:00 AM - 11:30 AM", "302"),
                    MakeupClass("auto_1", "Mobile Dev", "Prof. Rajeev Sharma", today, "11:00 AM - 12:30 PM", "Lab 4"),
                    MakeupClass("friday_1", "Artificial Intelligence", "Dr. Amit Verma", today, "09:00 AM - 10:30 AM", "Room 401")
                )
            } else {
                _makeupClasses.value = data
            }
            _isLoading.value = false
        }
    }

    fun addMakeupClass(makeupClass: MakeupClass) {
        viewModelScope.launch {
            repository.addMakeupClass(makeupClass)
            loadMakeupClasses()
        }
    }

    fun deleteMakeupClass(id: String) {
        viewModelScope.launch {
            repository.deleteMakeupClass(id)
            loadMakeupClasses()
        }
    }
}
