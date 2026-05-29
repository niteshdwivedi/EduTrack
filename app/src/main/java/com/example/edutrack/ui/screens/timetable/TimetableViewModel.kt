package com.example.edutrack.ui.screens.timetable

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.model.MakeupClass
import com.example.edutrack.data.model.TeacherAbsentEntry
import com.example.edutrack.data.model.TimetableEntry
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.notification.AlarmReceiver
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TimetableViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val auth: FirebaseAuth,
    private val settingsDataStore: SettingsDataStore,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTimetable()
    }

    fun loadTimetable() {
        viewModelScope.launch {
            _isLoading.value = true
            val regNum = settingsDataStore.registrationNumber.first() ?: ""
            
            // Parallel fetch mock and real (mock for demo if real empty)
            val realData = repository.getTimetable(regNum)
            val absences = repository.getTeacherAbsences()
            val makeups = repository.getMakeupClasses()

            val combined = (realData + getMockTimetable() + getMockMakeups(SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date()))
                .map { makeup ->
                    TimetableEntry(
                        classId = "makeup_${makeup.id}",
                        subjectName = makeup.subject,
                        subjectCode = "MAKEUP",
                        faculty = makeup.teacher,
                        room = makeup.venue,
                        startTime = makeup.time.split("-").getOrNull(0)?.trim() ?: makeup.time,
                        endTime = makeup.time.split("-").getOrNull(1)?.trim() ?: "",
                        day = SimpleDateFormat("EEEE", Locale.getDefault()).format(Date()),
                        isMakeupClass = true
                    )
                }).distinctBy { it.id }

            _timetable.value = combined.map { entry ->
                val isAbsent = absences.any { it.teacherName == entry.faculty && it.status == "Absent" }
                entry.copy(isTeacherAbsent = isAbsent)
            }
            _isLoading.value = false
        }
    }

    private fun getMockAbsences(today: String): List<TeacherAbsentEntry> {
        return listOf(
            TeacherAbsentEntry("1", "Dr. Amit Verma", "Mathematics", today, "09:00 AM", "10:30 AM", "Period 1")
        )
    }

    private fun getMockMakeups(today: String): List<MakeupClass> {
        return listOf(
            MakeupClass("1", "DBMS", "Dr. Mike", today, "02:00 PM - 03:30 PM", "105")
        )
    }

    fun toggleAlarm(entryId: String) {
        _timetable.value = _timetable.value.map { entry ->
            if (entry.id == entryId) {
                val newState = !entry.alarmEnabled
                if (newState) scheduleNotification(entry) else cancelNotification(entry)
                entry.copy(alarmEnabled = newState)
            } else entry
        }
    }

    private fun scheduleNotification(entry: TimetableEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", "Class Reminder")
            putExtra("message", "Your class '${entry.subject}' starts in 10 minutes.")
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            entry.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse class time
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        val classTime = try {
            val date = sdf.parse(entry.startTime.uppercase())
            val cal = Calendar.getInstance()
            val classCal = Calendar.getInstance().apply {
                time = date!!
                set(Calendar.YEAR, cal.get(Calendar.YEAR))
                set(Calendar.MONTH, cal.get(Calendar.MONTH))
                set(Calendar.DAY_OF_MONTH, cal.get(Calendar.DAY_OF_MONTH))
            }
            // Set 10 mins before
            classCal.add(Calendar.MINUTE, -10)
            classCal.timeInMillis
        } catch (e: Exception) {
            System.currentTimeMillis() + 60000 // Fallback 1 min from now
        }

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            classTime,
            pendingIntent
        )
    }

    private fun cancelNotification(entry: TimetableEntry) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            entry.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun getMockTimetable(): List<TimetableEntry> {
        return listOf(
            TimetableEntry("1", "CSE302", "Operating Systems", "Dr. Arhaan Roy", "TCH001", "27-310", "Monday", "09:00 AM", "10:30 AM"),
            TimetableEntry("2", "CSE201", "Data Structures", "Prof. Mehra", "TCH002", "18-102", "Monday", "10:45 AM", "12:00 PM")
        )
    }
}
