package com.example.edutrack.data.repository

import com.example.edutrack.data.model.*
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreRepository @Inject constructor(
    val firestore: FirebaseFirestore
) {
    // Attendance
    suspend fun getAttendance(regNum: String): List<SubjectAttendance> {
        return try {
            // Try numeric and string regNum
            var snapshot = firestore.collection("attendance")
                .whereEqualTo("studentReg", regNum.toLongOrNull() ?: regNum)
                .get()
                .await()
            
            if (snapshot.isEmpty) {
                 snapshot = firestore.collection("attendance")
                    .whereEqualTo("studentReg", regNum)
                    .get()
                    .await()
            }
            
            val records = snapshot.documents.map { it.data ?: emptyMap<String, Any>() }
            val grouped = records.groupBy { it["subject"] as? String ?: "Unknown" }
            
            grouped.map { (subject, subjectRecords) ->
                val attended = subjectRecords.count { it["status"] == "Present" }
                val total = subjectRecords.size
                SubjectAttendance(
                    id = subject,
                    name = subject,
                    attended = attended,
                    total = total,
                    teacherName = subjectRecords.firstOrNull()?.get("faculty") as? String ?: ""
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Assignments
    suspend fun getAssignments(regNum: String): List<Assignment> {
        return try {
            firestore.collection("assignments")
                .get().await().toObjects(Assignment::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Notes
    suspend fun getNotes(): List<Note> {
        return try {
            firestore.collection("notes")
                .get().await().toObjects(Note::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Exams
    suspend fun getExams(): List<Exam> {
        return try {
            val snapshot = firestore.collection("exams").get().await()
            val exams = snapshot.toObjects(Exam::class.java)
            exams
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Timetable
    suspend fun getTimetable(regNum: String): List<TimetableEntry> {
        return try {
            val snapshot = firestore.collection("timetable")
                .get().await()
            
            val allEntries = snapshot.toObjects(TimetableEntry::class.java)
            
            // Fetch all teachers to check for leave status
            val teachersSnapshot = firestore.collection("teachers").get().await()
            val teachers = teachersSnapshot.toObjects(Teacher::class.java)
            
            // Update entries with teacher leave status
            val processedEntries = allEntries.map { entry ->
                val teacher = teachers.find { it.name == entry.faculty || it.teacherId == entry.faculty }
                if (teacher?.status == "On Leave") {
                    entry.copy(isTeacherAbsent = true)
                } else {
                    entry
                }
            }
            
            if (regNum == "all") {
                return processedEntries
            }
            
            // Fetch User Section first
            val userSnapshot = firestore.collection("students")
                .whereEqualTo("registrationNumber", regNum)
                .get().await()
            
            val userSection = if (!userSnapshot.isEmpty) {
                userSnapshot.documents[0].getString("section") ?: ""
            } else {
                val regNumLong = regNum.toLongOrNull()
                if (regNumLong != null) {
                    val userSnapshotNum = firestore.collection("students")
                        .whereEqualTo("registrationNumber", regNumLong)
                        .get().await()
                    if (!userSnapshotNum.isEmpty) {
                        userSnapshotNum.documents[0].getString("section") ?: ""
                    } else ""
                } else ""
            }

            processedEntries.filter { 
                it.section.isEmpty() || it.section == userSection 
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Jobs
    suspend fun getJobs(): List<Job> {
        return try {
            firestore.collection("jobs").get().await().toObjects(Job::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Teacher Absent System
    suspend fun getTeacherAbsences(): List<TeacherAbsentEntry> {
        return try {
            firestore.collection("teachers_absent")
                .get().await().toObjects(TeacherAbsentEntry::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addTeacherAbsence(absence: TeacherAbsentEntry) {
        try {
            firestore.collection("teachers_absent").document(absence.id).set(absence).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteTeacherAbsence(id: String) {
        try {
            firestore.collection("teachers_absent").document(id).delete().await()
        } catch (e: Exception) {}
    }

    // Makeup Classes
    suspend fun getMakeupClasses(): List<MakeupClass> {
        return try {
            firestore.collection("makeup_classes")
                .get().await().toObjects(MakeupClass::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addMakeupClass(makeupClass: MakeupClass) {
        try {
            val id = if (makeupClass.id.isEmpty()) firestore.collection("makeup_classes").document().id else makeupClass.id
            firestore.collection("makeup_classes").document(id).set(makeupClass.copy(id = id)).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteMakeupClass(id: String) {
        try {
            firestore.collection("makeup_classes").document(id).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun addNote(userId: String, note: Note) {
        try {
            firestore.collection("users").document(userId).collection("notes")
                .document(note.noteId).set(note).await()
        } catch (e: Exception) {
        }
    }

    suspend fun getResources(): List<Resource> {
        return try {
            firestore.collection("resources")
                .get().await().toObjects(Resource::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    // Teacher Management
    suspend fun getAllTeachers(): List<Teacher> {
        return try {
            firestore.collection("teachers").get().await().toObjects(Teacher::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addTeacher(teacher: Teacher) {
        try {
            firestore.collection("teachers").document(teacher.teacherId).set(teacher).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteTeacher(teacherId: String) {
        try {
            firestore.collection("teachers").document(teacherId).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun updateTeacher(teacher: Teacher) {
        try {
            firestore.collection("teachers").document(teacher.teacherId).set(teacher).await()
        } catch (e: Exception) {}
    }

    // Timetable Management
    suspend fun addTimetableEntry(entry: TimetableEntry) {
        try {
            val docId = "${entry.section}_${entry.day}_${entry.startTime.replace(":", "").replace(" ", "")}"
            firestore.collection("timetable").document(docId).set(entry.copy(classId = docId)).await()
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error adding timetable", e)
        }
    }

    suspend fun deleteTimetableEntry(id: String) {
        try {
            firestore.collection("timetable").document(id).delete().await()
        } catch (e: Exception) {
            android.util.Log.e("Firestore", "Error deleting timetable entry", e)
        }
    }

    // Section Management
    suspend fun getAllSections(): List<Section> {
        return try {
            val snapshot = firestore.collection("sections").get().await()
            if (!snapshot.isEmpty) {
                return snapshot.toObjects(Section::class.java)
            }
            
            // Fallback: Discover sections from students collection
            val studentSnapshot = firestore.collection("students").get().await()
            val sections = studentSnapshot.documents.mapNotNull { it.getString("section") }
                .distinct()
                .filter { it.isNotEmpty() }
                .map { sectionId ->
                    Section(sectionId = sectionId, specialization = "Derived", semester = 1)
                }
            
            sections
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addSection(section: Section) {
        try {
            firestore.collection("sections").document(section.sectionId).set(section).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteSection(sectionId: String) {
        try {
            firestore.collection("sections").document(sectionId).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun deleteTimetableForSection(sectionId: String) {
        try {
            val snapshot = firestore.collection("timetable")
                .whereEqualTo("section", sectionId)
                .get().await()
            val batch = firestore.batch()
            for (doc in snapshot.documents) {
                batch.delete(doc.reference)
            }
            batch.commit().await()
        } catch (e: Exception) {}
    }

    // Student Management
    suspend fun getAllStudents(): List<User> {
        return try {
            firestore.collection("students").get().await().toObjects(User::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun addStudent(student: User) {
        try {
            val docId = student.registrationNumber.toString()
            firestore.collection("students").document(docId).set(student).await()
        } catch (e: Exception) {}
    }

    suspend fun deleteStudent(regNum: String) {
        try {
            firestore.collection("students").document(regNum).delete().await()
        } catch (e: Exception) {}
    }

    suspend fun updateStudent(student: User) {
        try {
            val docId = student.registrationNumber.toString()
            firestore.collection("students").document(docId).set(student).await()
        } catch (e: Exception) {}
    }
}
