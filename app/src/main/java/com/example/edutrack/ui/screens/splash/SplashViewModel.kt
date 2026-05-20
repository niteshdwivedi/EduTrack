package com.example.edutrack.ui.screens.splash

import androidx.lifecycle.ViewModel
import com.example.edutrack.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    val isUserLoggedIn: Boolean
        get() = authRepository.currentUser != null
}
