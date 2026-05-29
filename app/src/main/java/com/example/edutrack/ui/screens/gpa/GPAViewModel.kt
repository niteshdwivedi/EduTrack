package com.example.edutrack.ui.screens.gpa

import androidx.lifecycle.ViewModel
import com.example.edutrack.data.model.CourseGrade
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

@HiltViewModel
class GPAViewModel @Inject constructor() : ViewModel() {
    private val _courses = MutableStateFlow(listOf(
        CourseGrade("1", "Advanced Java", "4", "A"),
        CourseGrade("2", "Operating Systems", "4", "B+"),
        CourseGrade("3", "Database Systems", "3", "A-")
    ))
    val courses: StateFlow<List<CourseGrade>> = _courses

    private val _tgpa = MutableStateFlow(0.0)
    val tgpa: StateFlow<Double> = _tgpa
    
    private val _cgpa = MutableStateFlow(9.2) // Mock CGPA out of 10
    val cgpa: StateFlow<Double> = _cgpa

    init {
        calculateGPA()
    }

    fun addCourse() {
        val newCourse = CourseGrade(id = System.currentTimeMillis().toString(), credits = "4")
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
            val credits = course.credits?.toString()?.toDoubleOrNull() ?: 0.0
            val gradePoint = gradeToPoints(course.grade)
            totalPoints += gradePoint * credits
            totalCredits += credits
        }
        
        _tgpa.value = if (totalCredits > 0) totalPoints / totalCredits else 0.0
    }

    private fun gradeToPoints(grade: String): Double {
        return when(grade.uppercase()) {
            "O", "A+" -> 10.0
            "A" -> 9.0
            "B+" -> 8.0
            "B" -> 7.0
            "C+" -> 6.0
            "C" -> 5.0
            "D" -> 4.0
            else -> 0.0
        }
    }
}
