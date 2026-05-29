package com.example.edutrack.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.model.User
import com.example.edutrack.data.repository.AuthRepository
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val firestore: FirebaseFirestore,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user

    init {
        loadUserProfile()
    }

    private fun loadUserProfile() {
        viewModelScope.launch {
            val regNum = settingsDataStore.registrationNumber.first() ?: "12317648"
            
            try {
                val studentsRef = firestore.collection("students")
                var snapshot = studentsRef.whereEqualTo("registrationNumber", regNum).get().await()
                
                if (snapshot.isEmpty) {
                    val regNumLong = regNum.toLongOrNull()
                    if (regNumLong != null) {
                        snapshot = studentsRef.whereEqualTo("registrationNumber", regNumLong).get().await()
                    }
                }

                val doc = snapshot.documents.firstOrNull()
                if (doc != null) {
                    val nameFromDoc = doc.getString("name") ?: doc.get("Name") as? String ?: "Nitesh Dwivedi"
                    val emailFromDoc = doc.getString("email") ?: doc.get("Email") as? String ?: "nitesh@edutrack.com"
                    val phoneFromDoc = doc.get("phone")?.toString() ?: ""
                    val courseFromDoc = doc.getString("course") ?: "B.Tech CSE"
                    val rollFromDoc = doc.get("rollNumber")?.toString() ?: regNum

                    _user.value = User(
                        id = doc.id,
                        name = nameFromDoc,
                        email = emailFromDoc,
                        rollNumber = rollFromDoc,
                        course = courseFromDoc,
                        phone = phoneFromDoc,
                        semester = (doc.get("semester") as? Number)?.toInt() ?: 6
                    )
                } else {
                    // Fallback mock for Nitesh
                    _user.value = User(
                        name = "Nitesh Dwivedi",
                        email = "nitesh@edutrack.com",
                        rollNumber = "12317648",
                        course = "B.Tech CSE",
                        phone = "9876543210",
                        semester = 6
                    )
                }
            } catch (e: Exception) {
                _user.value = User(name = "Error Loading Profile")
            }
        }
    }

    fun logout() {
        authRepository.logout()
        viewModelScope.launch {
            settingsDataStore.setRegistrationNumber("")
        }
    }
}
