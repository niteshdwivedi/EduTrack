package com.example.edutrack.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val rollNumber: String = "",
    val registrationNumber: Any? = "",
    val course: String = "",
    val semester: Int = 1,
    val section: String = "",
    val phone: Any? = "",
    val university: String = "",
    val profilePictureUrl: String = "",
    val password: Any? = ""
)
