package com.example.edutrack.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.edutrack.ui.screens.exams.AlarmPuzzleActivity

class ExamAlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val subject = intent.getStringExtra("subject") ?: "Exam"
        val time = intent.getStringExtra("time") ?: ""
        val ringtone = intent.getStringExtra("ringtone")
        
        val puzzleIntent = Intent(context, AlarmPuzzleActivity::class.java).apply {
            putExtra("subject", subject)
            putExtra("time", time)
            putExtra("ringtone", ringtone)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        context.startActivity(puzzleIntent)
    }
}
