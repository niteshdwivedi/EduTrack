package com.example.edutrack.ui.screens.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _loginSuccess = MutableStateFlow(false)
    val loginSuccess: StateFlow<Boolean> = _loginSuccess

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _dobVerified = MutableStateFlow(false)
    val dobVerified: StateFlow<Boolean> = _dobVerified

    private val _passwordResetSuccess = MutableStateFlow(false)
    val passwordResetSuccess: StateFlow<Boolean> = _passwordResetSuccess

    fun login(regNum: String, pass: String, role: String = "STUDENT") {
        if (regNum.isBlank() || pass.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            // For initial setup/testing, if RegNum matches a special pattern, allow login
            // Actual implementation will use the CSV data we receive
            val result = repository.login(regNum, pass)
            _isLoading.value = false
            if (result.isSuccess) {
                // Ensure registration number is saved exactly as entered (or as a string)
                settingsDataStore.setUserSession(regNum.trim(), role)
                _loginSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Login failed. Use RegNum as ID."
            }
        }
    }

    fun verifyDob(regNum: String, dob: String) {
        if (regNum.isBlank() || dob.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            // Verification logic: match regNum and dob from our data records
            val isMatch = repository.verifyDob(regNum, dob)
            _isLoading.value = false
            if (isMatch) {
                _dobVerified.value = true
            } else {
                _error.value = "Verification failed. Please check your details."
            }
        }
    }

    fun resetPassword(regNum: String, newPass: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val success = repository.updatePassword(regNum, newPass)
            _isLoading.value = false
            if (success) {
                _passwordResetSuccess.value = true
            } else {
                _error.value = "Failed to update password."
            }
        }
    }

    fun clearFlags() {
        _dobVerified.value = false
        _passwordResetSuccess.value = false
        _error.value = null
    }

    fun register(email: String, pass: String, name: String) {
        if (email.isBlank() || pass.isBlank() || name.isBlank()) {
            _error.value = "Please fill in all fields"
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            val result = repository.register(email, pass, name)
            _isLoading.value = false
            if (result.isSuccess) {
                _loginSuccess.value = true
            } else {
                _error.value = result.exceptionOrNull()?.message ?: "Registration failed"
            }
        }
    }

    fun clearError() {
        _error.value = null
    }
}