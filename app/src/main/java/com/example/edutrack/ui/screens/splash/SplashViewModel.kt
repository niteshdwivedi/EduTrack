package com.example.edutrack.ui.screens.splash

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    
    var isUserLoggedIn by mutableStateOf(false)
        private set
    
    var isLoading by mutableStateOf(true)
        private set

    var userRole by mutableStateOf("STUDENT")
        private set

    init {
        checkLoginStatus()
    }

    private fun checkLoginStatus() {
        viewModelScope.launch {
            val regNum = settingsDataStore.registrationNumber.first()
            val role = settingsDataStore.userRole.first()
            
            isUserLoggedIn = !regNum.isNullOrEmpty()
            userRole = role ?: "STUDENT"
            isLoading = false
        }
    }
}
