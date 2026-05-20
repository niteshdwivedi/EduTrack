package com.example.edutrack.data.repository

import com.example.edutrack.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Collection Paths
    private fun userDoc(userId: String) = firestore.collection("users").document(userId)
    private fun universityDoc() = firestore.collection("university_data")

    // Attendance
    suspend fun getAttendance(userId: String): List<SubjectAttendance> {
        return try {
            userDoc(userId).collection("attendance")
                .get().await().toObjects(SubjectAttendance::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun updateAttendance(userId: String, attendance: SubjectAttendance) {
        userDoc(userId).collection("attendance").document(attendance.id).set(attendance).await()
    }

    // Assignments
    suspend fun getAssignments(userId: String): List<Assignment> {
        return try {
            userDoc(userId).collection("assignments")
                .get().await().toObjects(Assignment::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Notes
    suspend fun getNotes(userId: String): List<Note> {
        return try {
            userDoc(userId).collection("notes")
                .get().await().toObjects(Note::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addNote(userId: String, note: Note) {
        userDoc(userId).collection("notes").document(note.id).set(note).await()
    }

    // Exams
    suspend fun getExams(userId: String): List<Exam> {
        return try {
            userDoc(userId).collection("exams")
                .get().await().toObjects(Exam::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Timetable
    suspend fun getTimetable(userId: String): List<TimetableEntry> {
        return try {
            userDoc(userId).collection("timetable")
                .get().await().toObjects(TimetableEntry::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Jobs (Shared across university/app)
    suspend fun getJobs(): List<Job> {
        return try {
            firestore.collection("jobs").get().await().toObjects(Job::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Resources
    suspend fun getResources(): List<Resource> {
        return try {
            firestore.collection("resources").get().await().toObjects(Resource::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Teacher Absent System
    suspend fun getTeacherAbsences(): List<TeacherAbsentEntry> {
        return try {
            universityDoc().document("teacher_absences").collection("entries")
                .get().await().toObjects(TeacherAbsentEntry::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Makeup Classes
    suspend fun getMakeupClasses(): List<MakeupClass> {
        return try {
            universityDoc().document("makeup_classes").collection("entries")
                .get().await().toObjects(MakeupClass::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
