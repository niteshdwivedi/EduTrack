package com.example.edutrack.data.model

import androidx.compose.ui.graphics.Color
import com.google.firebase.firestore.PropertyName

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
    val attendanceId: String = "",
    val studentReg: String = "",
    val subject: String = "",
    val faculty: String = "",
    val room: String = "",
    val type: String = "Lecture", // Lecture, Practical
    val status: String = "Present", // Present, Absent, Late
    val date: String = "",
    val time: String = ""
)


data class Assignment(
    val id: String = "",
    val title: String = "",
    val subject: String = "",
    val dueDate: String = "",
    val status: String = "Pending"
)

data class Exam(
    @get:PropertyName("examId") @set:PropertyName("examId") var examId: String = "",
    val subjectCode: String = "",
    val subject: String = "",
    val date: String = "", // DD-MM-YYYY
    val time: String = "", // HH:mm AM/PM
    val venue: String = "",
    val section: String = "",
    val completed: Boolean = false,
    val attendance: String = "Present", // Present, Absent
    val resultStatus: String = "Pending", // Published, Pending, Not Available
    val marks: Any? = "",
    val grade: String = "",
    val alarmActive: Boolean = false,
    val alarmDays: Int = 0,
    val alarmHours: Int = 0,
    val alarmMinutes: Int = 0
) {
    val id: String get() = examId
}

data class CourseGrade(
    val id: String = "",
    var name: String = "",
    var credits: Any? = "",
    var grade: String = ""
)

data class Job(
    @get:PropertyName("jobId") @set:PropertyName("jobId") var jobId: String = "",
    val company: String = "",
    val role: String = "",
    @get:PropertyName("package") @set:PropertyName("package") var packageVal: Any? = "",
    val skills: String = "",
    val lastDate: String = "",
    val applyLink: String = "",
    val location: String = "",
    val campusType: String = "",
    val eligibility: String = ""
) {
    val id: String get() = jobId
    val title: String get() = role
    val packageOffered: String get() = packageVal?.toString() ?: ""
}

data class Resource(
    val id: String = "",
    val title: String = "",
    val type: String = "",
    val size: String = ""
)

data class Teacher(
    val teacherId: String = "",
    val name: String = "",
    val dob: String = "",
    val phone: String = "",
    val email: String = "",
    val department: String = "",
    val specialization: String = "", // Added to match CSV
    val cabin: String = "",          // Added to match CSV
    val subjects: Any? = null,
    val sections: Any? = null,
    val status: String = "Active",
    val password: Any? = "123456"
)

data class TeacherAbsentEntry(
    val id: String = "",
    val teacherName: String = "",
    val subject: String = "",
    val date: String = "", // DD-MM-YYYY
    val startTime: String = "",
    val endTime: String = "",
    val period: String = "",
    val status: String = "Absent"
)

data class MakeupClass(
    val id: String = "",
    val subject: String = "",
    val teacher: String = "",
    val date: String = "", // YYYY-MM-DD
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
    @get:PropertyName("noteId") @set:PropertyName("noteId") var noteId: String = "",
    val title: String = "",
    val subject: String = "",
    val type: String = "",
    val fileSize: String = "",
    val fileUrl: String = "",
    val content: String = "",
    val date: String = "",
    val color: Long = 0xFFFFFFFF
) {
    val id: String get() = noteId
}


data class TimetableEntry(
    @get:PropertyName("classId") @set:PropertyName("classId") var classId: String = "",
    val subjectCode: String = "",
    val subjectName: String = "",
    val faculty: String = "",
    val teacherId: String = "", // Added teacherId
    val room: String = "",
    val day: String = "",
    val startTime: String = "",
    val endTime: String = "",
    val section: String = "",
    val isTeacherAbsent: Boolean = false,
    val isMakeupClass: Boolean = false,
    val alarmEnabled: Boolean = false
) {
    val id: String get() = classId
    val subject: String get() = subjectName
    val teacherName: String get() = faculty
    val roomNumber: String get() = if (room.contains("-")) room.split("-").last().trim().removePrefix("Room").trim() else room.removePrefix("Room").trim()
    val cabinNumber: String get() = if (room.contains("-")) room.split("-").first().trim().removePrefix("Bldg").trim() else ""
    val dayOfWeek: Int get() = when(day.trim()) {
        "Monday" -> 1
        "Tuesday" -> 2
        "Wednesday" -> 3
        "Thursday" -> 4
        "Friday" -> 5
        "Saturday" -> 6
        "Sunday" -> 7
        else -> 1
    }
}

data class Section(
    val sectionId: String = "",
    val specialization: String = "",
    val semester: Int = 1,
    val strength: Int = 0,
    val mentorTeacherId: String = ""
)

data class ChatMessage(
    val id: String = "",
    val senderId: String = "",
    val senderName: String = "",
    val text: String = "",
    val timestamp: Long = 0,
    val attachmentUrl: String = "",
    val attachmentName: String = ""
)

data class ChatRoom(
    val id: String = "",
    val name: String = "",
    val lastMessage: String = "",
    val lastMessageTimestamp: Long = 0,
    val isGroup: Boolean = false,
    val participants: List<String> = emptyList(),
    val roomType: String = "Student" // Student, Teacher, Section
)
