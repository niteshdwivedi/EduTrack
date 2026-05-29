package com.example.edutrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.edutrack.ui.screens.dashboard.DashboardScreen
import com.example.edutrack.ui.screens.login.LoginScreen
import com.example.edutrack.ui.screens.login.ForgotPasswordScreen
import com.example.edutrack.ui.screens.login.ResetPasswordScreen
import com.example.edutrack.ui.screens.register.RegisterScreen
import com.example.edutrack.ui.screens.splash.SplashScreen
import com.example.edutrack.ui.screens.attendance.AttendanceScreen
import com.example.edutrack.ui.screens.timetable.TimetableScreen
import com.example.edutrack.ui.screens.notes.NotesScreen
import com.example.edutrack.ui.screens.assignments.AssignmentScreen
import com.example.edutrack.ui.screens.exams.ExamsScreen
import com.example.edutrack.ui.screens.gpa.GPACalculatorScreen
import com.example.edutrack.ui.screens.timer.StudyTimerScreen
import com.example.edutrack.ui.screens.profile.ProfileScreen
import com.example.edutrack.ui.screens.settings.SettingsScreen
import com.example.edutrack.ui.screens.jobs.JobPortalScreen
import com.example.edutrack.ui.screens.resources.ResourcesScreen
import com.example.edutrack.ui.screens.analytics.AnalyticsScreen
import com.example.edutrack.ui.screens.teacher_absent.TeacherAbsentScreen
import com.example.edutrack.ui.screens.makeup_class.MakeupClassScreen
import com.example.edutrack.ui.screens.reminders.RemindersScreen
import com.example.edutrack.ui.screens.search.SearchScreen
import com.example.edutrack.ui.screens.ai.AIScreen

import com.example.edutrack.ui.screens.chat.ChatListScreen
import com.example.edutrack.ui.screens.chat.ChatDetailScreen

import com.example.edutrack.ui.admin.AdminDashboardScreen
import com.example.edutrack.ui.admin.section.AdminSectionManagementScreen
import com.example.edutrack.ui.admin.teacher.AdminTeacherManagementScreen
import com.example.edutrack.ui.admin.timetable.AdminTimetableManagementScreen
import com.example.edutrack.ui.admin.AdminStudentManagementScreen
import com.example.edutrack.ui.teacher.dashboard.TeacherDashboardScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.ForgotPassword.route) { ForgotPasswordScreen(navController) }
        composable(Screen.ResetPassword.route) { backStackEntry ->
            val regNum = backStackEntry.arguments?.getString("regNum") ?: ""
            ResetPasswordScreen(navController, regNum)
        }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
        
        // Admin Screens
        composable(Screen.AdminDashboard.route) { AdminDashboardScreen(navController) }
        composable(Screen.AdminTeacherManagement.route) { AdminTeacherManagementScreen(navController) }
        composable(Screen.AdminSectionManagement.route) { AdminSectionManagementScreen(navController) }
        composable(Screen.AdminTimetableManagement.route) { AdminTimetableManagementScreen(navController) }
        composable(Screen.AdminStudentManagement.route) { AdminStudentManagementScreen(navController) }
        
        // Teacher Screens
        composable(Screen.TeacherDashboard.route) { TeacherDashboardScreen(navController) }

        composable(Screen.Attendance.route) { AttendanceScreen(navController) }
        composable(Screen.Timetable.route) { TimetableScreen(navController) }
        composable(Screen.Notes.route) { NotesScreen(navController) }
        composable(Screen.Assignments.route) { AssignmentScreen(navController) }
        composable(Screen.Exams.route) { ExamsScreen(navController) }
        composable(Screen.GPACalculator.route) { GPACalculatorScreen(navController) }
        composable(Screen.StudyTimer.route) { StudyTimerScreen(navController) }
        composable(Screen.JobPortal.route) { JobPortalScreen(navController) }
        composable(Screen.Resources.route) { ResourcesScreen(navController) }
        composable(Screen.Analytics.route) { AnalyticsScreen(navController) }
        composable(Screen.Profile.route) { ProfileScreen(navController) }
        composable(Screen.Settings.route) { SettingsScreen(navController) }
        composable(Screen.TeacherAbsent.route) { TeacherAbsentScreen(navController) }
        composable(Screen.MakeupClass.route) { MakeupClassScreen(navController) }
        composable(Screen.Reminders.route) { RemindersScreen(navController) }
        composable(Screen.Search.route) { SearchScreen(navController) }
        composable(Screen.AIAssistant.route) { AIScreen(navController) }
        composable(Screen.ChatList.route) { ChatListScreen(navController) }
        composable(Screen.ChatDetail.route) { backStackEntry ->
            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
            val roomName = backStackEntry.arguments?.getString("roomName") ?: ""
            ChatDetailScreen(navController, roomId, roomName)
        }
    }
}
