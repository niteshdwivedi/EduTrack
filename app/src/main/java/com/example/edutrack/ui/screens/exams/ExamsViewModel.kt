package com.example.edutrack.ui.screens.exams

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.model.Exam
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.notification.ExamAlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class ExamsViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    @ApplicationContext private val context: Context,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _upcomingExams = MutableStateFlow<List<Exam>>(emptyList())
    val upcomingExams: StateFlow<List<Exam>> = _upcomingExams

    private val _historyExams = MutableStateFlow<List<Exam>>(emptyList())
    val historyExams: StateFlow<List<Exam>> = _historyExams

    private val _alarmDays = MutableStateFlow(0)
    val alarmDays: StateFlow<Int> = _alarmDays

    private val _alarmHours = MutableStateFlow(0)
    val alarmHours: StateFlow<Int> = _alarmHours

    private val _alarmMinutes = MutableStateFlow(0)
    val alarmMinutes: StateFlow<Int> = _alarmMinutes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _currentTime = MutableStateFlow(System.currentTimeMillis())
    val currentTime: StateFlow<Long> = _currentTime

    private val _customRingtoneUri = MutableStateFlow<String?>(null)
    val customRingtoneUri: StateFlow<String?> = _customRingtoneUri

    init {
        loadExams()
        startTimer()
        loadAlarmSettings()
    }

    private fun loadAlarmSettings() {
        viewModelScope.launch {
            _alarmDays.value = settingsDataStore.alarmDays.first()
            _alarmHours.value = settingsDataStore.alarmHours.first()
            _alarmMinutes.value = settingsDataStore.alarmMinutes.first()
        }
    }

    private fun startTimer() {
        viewModelScope.launch {
            while (true) {
                _currentTime.value = System.currentTimeMillis()
                delay(1000)
            }
        }
    }

    fun loadExams() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getExams()
            val allExams = if (data.isEmpty()) getMockExams() else data
            
            val now = System.currentTimeMillis()
            val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
            
            val partitioned = allExams.partition { exam ->
                try {
                    val examDate = sdf.parse("${exam.date} ${exam.time}")
                    (examDate?.time ?: 0) > now
                } catch (e: Exception) {
                    true
                }
            }
            
            _upcomingExams.value = partitioned.first.sortedBy { exam ->
                try {
                    sdf.parse("${exam.date} ${exam.time}")?.time ?: Long.MAX_VALUE
                } catch (e: Exception) {
                    Long.MAX_VALUE
                }
            }
            _historyExams.value = partitioned.second.sortedByDescending { exam ->
                try {
                    sdf.parse("${exam.date} ${exam.time}")?.time ?: 0L
                } catch (e: Exception) {
                    0L
                }
            }
            
            _isLoading.value = false
        }
    }

    fun incrementAlarmDays() {
        _alarmDays.value = (_alarmDays.value + 1) % 30
        saveAlarmSettings()
        scheduleAllAlarms()
    }

    fun incrementAlarmHours() {
        _alarmHours.value = (_alarmHours.value + 1) % 24
        saveAlarmSettings()
        scheduleAllAlarms()
    }

    fun incrementAlarmMinutes() {
        _alarmMinutes.value = (_alarmMinutes.value + 5) % 60
        saveAlarmSettings()
        scheduleAllAlarms()
    }

    fun resetAlarm() {
        _alarmDays.value = 0
        _alarmHours.value = 0
        _alarmMinutes.value = 0
        saveAlarmSettings()
        cancelAllAlarms()
    }

    private fun saveAlarmSettings() {
        viewModelScope.launch {
            settingsDataStore.setAlarmSettings(_alarmDays.value, _alarmHours.value, _alarmMinutes.value)
        }
    }

    fun setCustomRingtone(uri: String) {
        _customRingtoneUri.value = uri
    }

    private fun scheduleAllAlarms() {
        if (_alarmDays.value == 0 && _alarmHours.value == 0 && _alarmMinutes.value == 0) {
            cancelAllAlarms()
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())

        _upcomingExams.value.forEach { exam ->
            try {
                val examDate = sdf.parse("${exam.date} ${exam.time}")
                if (examDate != null) {
                    val calendar = Calendar.getInstance()
                    calendar.time = examDate
                    calendar.add(Calendar.DAY_OF_YEAR, -_alarmDays.value)
                    calendar.add(Calendar.HOUR_OF_DAY, -_alarmHours.value)
                    calendar.add(Calendar.MINUTE, -_alarmMinutes.value)

                    val alarmTime = calendar.timeInMillis
                    if (alarmTime > System.currentTimeMillis()) {
                        val intent = Intent(context, ExamAlarmReceiver::class.java).apply {
                            putExtra("subject", exam.subject)
                            putExtra("time", exam.time)
                            putExtra("ringtone", _customRingtoneUri.value)
                            action = "com.example.edutrack.ALARM_${exam.id}"
                        }
                        val pendingIntent = PendingIntent.getBroadcast(
                            context, exam.id.hashCode(), intent, 
                            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                        )
                        
                        val info = AlarmManager.AlarmClockInfo(alarmTime, pendingIntent)
                        alarmManager.setAlarmClock(info, pendingIntent)
                    }
                }
            } catch (e: Exception) {}
        }
    }

    private fun cancelAllAlarms() {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        _upcomingExams.value.forEach { exam ->
            val intent = Intent(context, ExamAlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(
                context, exam.id.hashCode(), intent, 
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            alarmManager.cancel(pendingIntent)
        }
    }

    private fun getMockExams(): List<Exam> {
        return listOf(
            Exam(examId = "1", subject = "Mathematics", date = "25-05-2026", time = "10:30", venue = "Hall A"),
            Exam(examId = "2", subject = "Physics", date = "27-05-2026", time = "14:00", venue = "Lab 2"),
            Exam(examId = "3", subject = "Computer Science", date = "30-05-2026", time = "09:00", venue = "Room 204"),
            Exam(examId = "4", subject = "History of Art", date = "20-05-2026", time = "11:00", venue = "Hall B", completed = true, resultStatus = "Published", marks = "87/100", grade = "A"),
            Exam(examId = "5", subject = "Organic Chemistry", date = "22-05-2026", time = "13:00", venue = "Lab 4", completed = true, resultStatus = "Not Available", attendance = "Absent"),
            Exam(examId = "6", subject = "Microeconomics", date = "23-05-2026", time = "10:00", venue = "Hall C", completed = true, resultStatus = "Pending")
        )
    }
}
