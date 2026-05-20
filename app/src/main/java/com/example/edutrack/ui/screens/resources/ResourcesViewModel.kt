package com.example.edutrack.ui.screens.resources

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.model.Resource
import com.example.edutrack.data.repository.FirestoreRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResourcesViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    private val _resources = MutableStateFlow<List<Resource>>(emptyList())
    val resources: StateFlow<List<Resource>> = _resources

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadResources()
    }

    fun loadResources() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getResources()
            if (data.isEmpty()) {
                // Fallback to dummy data
                _resources.value = listOf(
                    Resource("1", "Calculus Notes.pdf", "PDF", "2.5 MB"),
                    Resource("2", "Physics Lecture 1.mp4", "Video", "45 MB"),
                    Resource("3", "Assignment Template.docx", "Document", "120 KB"),
                    Resource("4", "Chemistry Lab Guide.pdf", "PDF", "1.8 MB")
                )
            } else {
                _resources.value = data
            }
            _isLoading.value = false
        }
    }
}
