package com.example.edutrack.data.model

import androidx.compose.ui.graphics.Color

// Note: User model is defined in User.kt to avoid redeclaration

data class SubjectAttendance(
    val id: String = "",
    val name: String = "",
    val courseCode: String = "",
    val teacherName: String = "",
    val attended: Int = 0,
    val total: Int = 0,
    val history: List<AttendanceRecord> = emptyList()
)

data class AttendanceRecord(
    val id: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "Present", // Present, Absent, Late
    val room: String = "",
    val teacher: String = ""
)


data class Assignment(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val dueDate: String = "",
    val status: String = "Pending"
)

data class Exam(
    val id: String = "",
    val subject: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = ""
)

data class CourseGrade(
    val id: String = "",
    var name: String = "",
    var credits: String = "",
    var grade: String = ""
)

data class Job(
    val id: String = "",
    val title: String = "",
    val company: String = "",
    val location: String = "",
    val type: String = ""
)

data class Resource(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val size: String = ""
)

data class TeacherAbsentEntry(
    val id: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val date: String = "",
    val period: String = ""
)

data class MakeupClass(
    val id: String = "",
    val subject: String = "",
    val teacher: String = "",
    val date: String = "",
    val time: String = "",
    val venue: String = ""
)

data class Reminder(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val time: String = "",
    val isEnabled: Boolean = true
)

data class Note(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val subject: String = "",
    val date: String = "",
    val color: Long = 0xFFFFFFFF
)


data class TimetableEntry(
    val id: String = "",
    val subject: String = "",
    val courseCode: String = "",
    val teacherName: String = "",
    val roomNumber: String = "",
    val cabinNumber: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val dayOfWeek: Int = 1, // 1 for Monday, etc.
    val isTeacherAbsent: Boolean = false,
    val isMakeupClass: Boolean = false
)

