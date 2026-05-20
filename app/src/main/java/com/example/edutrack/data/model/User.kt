package com.example.edutrack.data.model

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val rollNumber: String = "",
    val course: String = "",
    val semester: Int = 1,
    val phone: String = "",
    val university: String = "",
    val profilePictureUrl: String = ""
)
