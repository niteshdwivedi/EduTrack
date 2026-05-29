package com.example.edutrack.util

import com.example.edutrack.data.model.SubjectAttendance

object AttendanceUtils {
    fun calculateOverallPercentage(subjects: List<SubjectAttendance>): Int {
        val totalAttended = subjects.sumOf { it.attended }
        val totalClasses = subjects.sumOf { it.total }
        return if (totalClasses > 0) {
            ((totalAttended.toFloat() / totalClasses) * 100).toInt()
        } else {
            0
        }
    }
}
