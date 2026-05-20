package com.example.edutrack.ui.screens.makeup_class

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.MakeupClass
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MakeupClassViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {

    private val _makeupClasses = MutableStateFlow<List<MakeupClass>>(emptyList())
    val makeupClasses: StateFlow<List<MakeupClass>> = _makeupClasses

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadMakeupClasses()
    }

    fun loadMakeupClasses() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getMakeupClasses()
            if (data.isEmpty()) {
                // Fallback dummy data
                _makeupClasses.value = listOf(
                    MakeupClass("1", "Digital Logic Design", "Prof. Alan", "2023-11-25", "10:00 AM", "Room 302"),
                    MakeupClass("2", "Data Structures", "Ms. Sarah", "2023-11-26", "02:00 PM", "Lab 1"),
                    MakeupClass("3", "Operating Systems", "Dr. Mike", "2023-11-27", "09:00 AM", "Room 105")
                )
            } else {
                _makeupClasses.value = data
            }
            _isLoading.value = false
        }
    }
}
