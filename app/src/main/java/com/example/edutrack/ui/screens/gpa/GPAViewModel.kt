package com.example.edutrack.ui.screens.gpa

import androidx.lifecycle.ViewModel
import com.example.edutrack.data.model.CourseGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GPAViewModel @Inject constructor() : ViewModel() {
    private val _courses = MutableStateFlow(listOf(CourseGrade(id = "1")))
    val courses: StateFlow<List<CourseGrade>> = _courses

    private val _gpa = MutableStateFlow(0.0)
    val gpa: StateFlow<Double> = _gpa

    fun addCourse() {
        val newCourse = CourseGrade(id = System.currentTimeMillis().toString())
        _courses.value = _courses.value + newCourse
    }

    fun updateCourse(id: String, name: String? = null, credits: String? = null, grade: String? = null) {
        _courses.value = _courses.value.map {
            if (it.id == id) {
                it.copy(
                    name = name ?: it.name,
                    credits = credits ?: it.credits,
                    grade = grade ?: it.grade
                )
            } else it
        }
        calculateGPA()
    }

    fun deleteCourse(id: String) {
        _courses.value = _courses.value.filter { it.id != id }
        calculateGPA()
    }

    private fun calculateGPA() {
        var totalPoints = 0.0
        var totalCredits = 0.0
        
        _courses.value.forEach { course ->
            val credits = course.credits.toDoubleOrNull() ?: 0.0
            val gradePoint = course.grade.toDoubleOrNull() ?: 0.0
            totalPoints += gradePoint * credits
            totalCredits += credits
        }
        
        _gpa.value = if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }
}
