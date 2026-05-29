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
    object AIAssistant : Screen("ai_assistant")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password/{regNum}") {
        fun createRoute(regNum: String) = "reset_password/$regNum"
    }
    object ChatList : Screen("chat_list")
    object ChatDetail : Screen("chat_detail/{roomId}/{roomName}") {
        fun createRoute(roomId: String, roomName: String) = "chat_detail/$roomId/$roomName"
    }
    
    // Admin Screens
    object AdminDashboard : Screen("admin_dashboard")
    object AdminTeacherManagement : Screen("admin_teacher_mgmt")
    object AdminStudentManagement : Screen("admin_student_mgmt")
    object AdminSectionManagement : Screen("admin_section_mgmt")
    object AdminTimetableManagement : Screen("admin_timetable_mgmt")
    object AdminTeacherLeave : Screen("admin_teacher_leave")
    
    // Teacher Screens
    object TeacherDashboard : Screen("teacher_dashboard")
}
