package com.example.edutrack.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object Dashboard : Screen("dashboard")
    object Attendance : Screen("attendance")
    object Timetable : Screen("timetable")
    object Notes : Screen("notes")
    object Assignments : Screen("assignments")
    object Exams : Screen("exams")
    object GPACalculator : Screen("gpa_calculator")
    object StudyTimer : Screen("study_timer")
    object JobPortal : Screen("job_portal")
    object Resources : Screen("resources")
    object Analytics : Screen("analytics")
    object Profile : Screen("profile")
    object Settings : Screen("settings")
    object TeacherAbsent : Screen("teacher_absent")
    object MakeupClass : Screen("makeup_class")
    object Reminders : Screen("reminders")
    object Search : Screen("search")
}