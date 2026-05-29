package com.example.edutrack.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class AssignmentReminderWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val repository: FirestoreRepository,
    private val auth: FirebaseAuth
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val userId = auth.currentUser?.uid ?: return Result.failure()
        
        val assignments = repository.getAssignments(userId)
        val pendingAssignments = assignments.filter { it.status == "Pending" }
        
        if (pendingAssignments.isNotEmpty()) {
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showNotification(
                "Pending Assignments",
                "You have ${pendingAssignments.size} assignments pending. Don't forget to complete them!"
            )
        }

        return Result.success()
    }
}
