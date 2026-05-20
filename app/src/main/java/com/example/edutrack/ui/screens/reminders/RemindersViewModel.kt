package com.example.edutrack.ui.screens.reminders

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import com.example.edutrack.data.model.Reminder
import com.example.edutrack.notification.AlarmReceiver
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class RemindersViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _reminders = MutableStateFlow(
        listOf(
            Reminder("1", "Mathematics Assignment", "Submit on portal", "09:00 AM"),
            Reminder("2", "Physics Lab", "Bring records", "02:00 PM")
        )
    )
    val reminders: StateFlow<List<Reminder>> = _reminders

    fun scheduleReminder(reminder: Reminder, timeInMillis: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra("title", reminder.title)
            putExtra("message", reminder.description)
        }
        
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            reminder.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            timeInMillis,
            pendingIntent
        )
    }

    fun toggleReminder(id: String, enabled: Boolean) {
        _reminders.value = _reminders.value.map {
            if (it.id == id) it.copy(isEnabled = enabled) else it
        }
    }
}
