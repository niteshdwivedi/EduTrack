package com.example.edutrack.ui.screens.profile

import androidx.lifecycle.ViewModel
import com.example.edutrack.data.model.User
import com.example.edutrack.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _user = MutableStateFlow(User(
        name = "John Doe",
        email = "john.doe@university.edu",
        rollNumber = "20230001",
        university = "Global Tech University"
    ))
    val user: StateFlow<User> = _user

    fun logout() {
        authRepository.logout()
    }
}
