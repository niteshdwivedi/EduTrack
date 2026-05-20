package com.example.edutrack.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.edutrack.ui.screens.dashboard.DashboardScreen
import com.example.edutrack.ui.screens.login.LoginScreen
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

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) { SplashScreen(navController) }
        composable(Screen.Login.route) { LoginScreen(navController) }
        composable(Screen.Register.route) { RegisterScreen(navController) }
        composable(Screen.Dashboard.route) { DashboardScreen(navController) }
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
    }
}