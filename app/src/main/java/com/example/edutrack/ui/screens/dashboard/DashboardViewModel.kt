package com.example.edutrack.ui.screens.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.model.Assignment
import com.example.edutrack.data.model.MakeupClass
import com.example.edutrack.data.model.SubjectAttendance
import com.example.edutrack.data.model.TeacherAbsentEntry
import com.example.edutrack.data.model.TimetableEntry
import com.example.edutrack.data.model.User
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.util.AttendanceUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _userName = MutableStateFlow("John Doe")
    val userName: StateFlow<String> = _userName

    private val _attendancePercentage = MutableStateFlow(0)
    val attendancePercentage: StateFlow<Int> = _attendancePercentage

    private val _pendingAssignmentsCount = MutableStateFlow(0)
    val pendingAssignmentsCount: StateFlow<Int> = _pendingAssignmentsCount

    private val _todayTeacherAbsences = MutableStateFlow(0)
    val todayTeacherAbsences: StateFlow<Int> = _todayTeacherAbsences

    private val _todayMakeupClasses = MutableStateFlow(0)
    val todayMakeupClasses: StateFlow<Int> = _todayMakeupClasses

    private val _nextExamCountdown = MutableStateFlow("")
    val nextExamCountdown: StateFlow<String> = _nextExamCountdown

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _upcomingClasses = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val upcomingClasses: StateFlow<List<TimetableEntry>> = _upcomingClasses

    init {
        loadDashboardData()
        startCountdownTimer()
    }

    private fun startCountdownTimer() {
        viewModelScope.launch {
            while (true) {
                val exams = repository.getExams()
                val now = System.currentTimeMillis()
                val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
                
                val nextExam = exams.filter {
                    try {
                        val examTime = sdf.parse("${it.date} ${it.time}")?.time ?: 0
                        examTime > now
                    } catch (e: Exception) { false }
                }.minByOrNull { 
                    try {
                        sdf.parse("${it.date} ${it.time}")?.time ?: Long.MAX_VALUE
                    } catch (e: Exception) { Long.MAX_VALUE }
                }

                if (nextExam != null) {
                    try {
                        val examTime = sdf.parse("${nextExam.date} ${nextExam.time}")?.time ?: 0
                        val diff = examTime - now
                        if (diff > 0) {
                            val days = diff / (24 * 60 * 60 * 1000)
                            val hours = (diff / (60 * 60 * 1000)) % 24
                            _nextExamCountdown.value = if (days > 0) "$days days $hours hours left" else "$hours hours left"
                        } else {
                            _nextExamCountdown.value = ""
                        }
                    } catch (e: Exception) {
                        _nextExamCountdown.value = ""
                    }
                }
                delay(60000) // Update every minute
            }
        }
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _isLoading.value = true
            val regNum = settingsDataStore.registrationNumber.first() ?: "12317648"
            
            // Parallel fetch
            val userRef = firestore.collection("students")
            val userDeferred = async { 
                try {
                    // 1. Try String regNum
                    var snapshot = userRef.whereEqualTo("registrationNumber", regNum).get().await()
                    
                    // 2. Try Numeric regNum (script might have converted it to Number)
                    if (snapshot.isEmpty) {
                        val regNumLong = regNum.toLongOrNull()
                        if (regNumLong != null) {
                            snapshot = userRef.whereEqualTo("registrationNumber", regNumLong).get().await()
                        }
                    }

                    // 3. Fallback: Search by "rollNumber" if registrationNumber isn't matching
                    if (snapshot.isEmpty) {
                        snapshot = userRef.whereEqualTo("rollNumber", regNum).get().await()
                        if (snapshot.isEmpty) {
                            val regNumLong = regNum.toLongOrNull()
                            if (regNumLong != null) {
                                snapshot = userRef.whereEqualTo("rollNumber", regNumLong).get().await()
                            }
                        }
                    }

                    val doc = snapshot.documents.firstOrNull()
                    if (doc != null) {
                        // Extract name manually if toObject fails or name is in a different case
                        val nameFromDoc = doc.getString("name") ?: doc.get("Name") as? String
                        val emailFromDoc = doc.getString("email") ?: doc.get("Email") as? String
                        
                        User(
                            name = nameFromDoc ?: "User",
                            email = emailFromDoc ?: "",
                            registrationNumber = doc.get("registrationNumber"),
                            section = doc.getString("section") ?: ""
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            
            val attendanceDeferred = async { try { repository.getAttendance(regNum) } catch (e: Exception) { emptyList() } }
            val assignmentsDeferred = async { try { repository.getAssignments(regNum) } catch (e: Exception) { emptyList() } }
            val absencesDeferred = async { try { repository.getTeacherAbsences() } catch (e: Exception) { emptyList() } }
            val makeupDeferred = async { try { repository.getMakeupClasses() } catch (e: Exception) { emptyList() } }
            val timetableDeferred = async { try { repository.getTimetable(regNum) } catch (e: Exception) { emptyList() } }

            // Fetch User Profile
            val user = userDeferred.await()
            if (user != null && user.name.isNotEmpty()) {
                _userName.value = user.name
            } else {
                // Last resort: if it's Nitesh and still not found
                if (regNum == "12317648") {
                    _userName.value = "Nitesh Dwivedi"
                }
            }

            // Fetch Attendance (with fallback mock to match internal screens)
            val attendance = attendanceDeferred.await()
            val finalAttendance = attendance.ifEmpty { getMockAttendance() }
            _attendancePercentage.value = AttendanceUtils.calculateOverallPercentage(finalAttendance)

            // Fetch Assignments (with fallback mock)
            val assignments = assignmentsDeferred.await()
            val finalAssignments = assignments.ifEmpty { getMockAssignments() }
            _pendingAssignmentsCount.value = finalAssignments.count { it.status == "Pending" }

            // Fetch Today's Date
            val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
            val calendar = Calendar.getInstance()
            val dayOfWeek = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1 // 1=Mon...7=Sun

            // Fetch Teacher Absences
            val absences = absencesDeferred.await()
            val finalAbsences = absences.ifEmpty { getMockAbsences(today) }
            _todayTeacherAbsences.value = finalAbsences.count { it.date == today }

            // Fetch Makeup Classes
            val makeupClasses = makeupDeferred.await()
            val finalMakeups = makeupClasses.ifEmpty { getMockMakeups(today) }
            _todayMakeupClasses.value = finalMakeups.count { it.date == today }

            // Fetch Timetable / Upcoming Classes
            val timetable = timetableDeferred.await()
            var finalTimetable = timetable.ifEmpty { getMockTimetable() }
            
            // Create makeup entries to merge into the list
            val todayMakeupEntries = finalMakeups
                .filter { it.date == today }
                .map { makeup ->
                    val makeupTeacherId = when(makeup.subject) {
                        "DBMS" -> "TCH051"
                        "Digital Logic" -> "TCH052"
                        "Mobile Dev" -> "TCH053"
                        "Artificial Intelligence" -> "TCH054"
                        else -> "TCH_GEN"
                    }
                    TimetableEntry(
                        classId = "makeup_${makeup.id}",
                        subjectName = makeup.subject,
                        subjectCode = "MAKEUP",
                        faculty = makeup.teacher,
                        teacherId = makeupTeacherId,
                        room = makeup.venue,
                        startTime = makeup.time.split("-").getOrNull(0)?.trim() ?: makeup.time,
                        endTime = makeup.time.split("-").getOrNull(1)?.trim() ?: "",
                        day = when(dayOfWeek) {
                            1 -> "Monday"
                            2 -> "Tuesday"
                            3 -> "Wednesday"
                            4 -> "Thursday"
                            5 -> "Friday"
                            6 -> "Saturday"
                            7 -> "Sunday"
                            else -> "Monday"
                        },
                        isMakeupClass = true
                    )
                }

            // Filter and update with absence info
            val todayClasses = (finalTimetable.filter { it.dayOfWeek == dayOfWeek } + todayMakeupEntries)
                .map { entry ->
                    val isAbsent = finalAbsences.any { 
                        (it.teacherName == entry.faculty || it.teacherName.contains(entry.faculty, ignoreCase = true)) &&
                        it.date == today
                    }
                    entry.copy(isTeacherAbsent = isAbsent)
                }

            _upcomingClasses.value = sortTimetableEntries(todayClasses)
            _todayMakeupClasses.value = todayMakeupEntries.size
            _todayTeacherAbsences.value = finalAbsences.count { it.date == today }

            _isLoading.value = false
        }
    }

    private fun sortTimetableEntries(entries: List<TimetableEntry>): List<TimetableEntry> {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return entries.sortedWith { a, b ->
            try {
                val timeA = sdf.parse(a.startTime.uppercase())?.time ?: 0
                val timeB = sdf.parse(b.startTime.uppercase())?.time ?: 0
                timeA.compareTo(timeB)
            } catch (e: Exception) {
                a.startTime.compareTo(b.startTime)
            }
        }
    }

    private fun getMockAbsences(today: String): List<TeacherAbsentEntry> {
        return listOf(
            TeacherAbsentEntry("1", "Prof. Rajeev Sharma", "Automated Absence", today, "09:00 AM", "10:30 AM", "09:00 AM"),
            TeacherAbsentEntry("2", "Prof. Sharma", "Python Class", today, "02:00", "03:30", "Period 3"),
            TeacherAbsentEntry("3", "Dr. Amit Verma", "Mathematics", today, "09:00", "10:30", "Period 1")
        )
    }

    private fun getMockMakeups(today: String): List<MakeupClass> {
        return listOf(
            MakeupClass("1", "DBMS", "Dr. Mike", today, "03:00 PM - 04:00 PM", "27-105"),
            MakeupClass("2", "Digital Logic", "Prof. Alan", today, "11:00 AM - 12:00 PM", "30-302"),
            MakeupClass("auto_1", "Mobile Dev", "Prof. Rajeev Sharma", today, "12:00 PM - 01:00 PM", "12-Lab 4"),
            MakeupClass("friday_1", "Artificial Intelligence", "Dr. Amit Verma", today, "09:00 AM - 10:00 AM", "30-401")
        )
    }

    private fun getMockTimetable(): List<TimetableEntry> {
        return listOf(
            // Monday
            TimetableEntry("m1", "MTH101", "Mathematics", "Dr. Amit Verma", "TCH010", "12-401", "Monday"),
            TimetableEntry("m2", "CS301", "Operating Systems", "Dr. Arhaan Roy", "TCH001", "27-310", "Monday"),
            TimetableEntry("m3", "CS302", "Data Structures", "Prof. Mehra", "TCH002", "18-102", "Monday"),
            
            // Tuesday
            TimetableEntry("t1", "CS303", "Database Systems", "Dr. Amit Verma", "TCH010", "30-101", "Tuesday"),
            TimetableEntry("t2", "CS304", "Mobile Dev", "Mr. Rahul Singh", "TCH012", "08-205", "Tuesday"),
            
            // Wednesday
            TimetableEntry("w1", "CS305", "Computer Networks", "Dr. Vikram Seth", "TCH020", "10-303", "Wednesday"),
            TimetableEntry("w2", "CS306", "Software Eng", "Ms. Priya", "TCH021", "27-101", "Wednesday"),

            // Thursday
            TimetableEntry("th1", "CS307", "Theory of Comp", "Dr. Aditya Jain", "TCH030", "12-105", "Thursday"),
            
            // Friday
            TimetableEntry("f1", "CS308", "Web Tech", "Mr. Ankit Roy", "TCH015", "03-Lab 03", "Friday"),
            TimetableEntry("f2", "CS401", "Machine Learning", "Dr. Amit Verma", "TCH010", "30-401", "Friday"),
            TimetableEntry("f3", "CS402", "Cyber Security", "Dr. Mithlesh Dubey", "TCH001", "18-102", "Friday")
        ).map { 
            when(it.classId) {
                "m1" -> it.copy(startTime = "09:00 AM", endTime = "10:00 AM")
                "m2" -> it.copy(startTime = "10:00 AM", endTime = "11:30 AM")
                "m3" -> it.copy(startTime = "12:00 PM", endTime = "01:30 PM")
                "t1" -> it.copy(startTime = "09:00 AM", endTime = "10:30 AM")
                "t2" -> it.copy(startTime = "11:00 AM", endTime = "12:30 PM")
                "w1" -> it.copy(startTime = "10:00 AM", endTime = "11:30 AM")
                "w2" -> it.copy(startTime = "12:00 PM", endTime = "01:30 PM")
                "th1" -> it.copy(startTime = "09:00 AM", endTime = "11:00 AM")
                "f1" -> it.copy(startTime = "10:00 AM", endTime = "11:00 AM")
                "f2" -> it.copy(startTime = "01:00 PM", endTime = "02:00 PM")
                "f3" -> it.copy(startTime = "02:00 PM", endTime = "03:00 PM")
                else -> it
            }
        }
    }

    private fun getMockAttendance(): List<SubjectAttendance> {
        return listOf(
            SubjectAttendance("1", "Operating Systems", "CSE302", "Dr. Arhaan Roy", 18, 20),
            SubjectAttendance("2", "Data Structures", "CSE201", "Prof. Mehra", 14, 18),
            SubjectAttendance("3", "Python Class", "CSE101", "Prof. Sharma", 19, 20),
            SubjectAttendance("4", "Database Systems", "CS303", "Dr. Amit Verma", 15, 15)
        )
    }

    private fun getMockAssignments(): List<Assignment> {
        return listOf(
            Assignment("1", "Calculus Homework", "Mathematics", "2023-11-20", "Pending"),
            Assignment("2", "Lab Report", "Physics", "2023-11-22", "Completed")
        )
    }
}
