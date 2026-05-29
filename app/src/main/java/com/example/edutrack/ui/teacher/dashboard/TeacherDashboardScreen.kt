package com.example.edutrack.ui.teacher.dashboard

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.example.edutrack.ui.navigation.Screen

data class TeacherModule(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherDashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    val modules = listOf(
        TeacherModule("Attendance", Icons.Default.CheckCircle, "", Color(0xFF4CAF50)),
        TeacherModule("Timetable", Icons.Default.DateRange, Screen.Timetable.route, Color(0xFF2196F3)),
        TeacherModule("Makeup", Icons.Default.AddAlert, Screen.MakeupClass.route, Color(0xFF2196F3)),
        TeacherModule("Notes", Icons.Default.UploadFile, Screen.Notes.route, Color(0xFFFF9800)),
        TeacherModule("Leaves", Icons.Default.PersonOff, Screen.TeacherAbsent.route, Color(0xFFF44336)),
        TeacherModule("Assignment", Icons.Default.Assignment, Screen.Assignments.route, Color(0xFFE91E63)),
        TeacherModule("Students", Icons.Default.People, Screen.AdminStudentManagement.route, Color(0xFF9C27B0)), // Shared with Admin for now
        TeacherModule("Announce", Icons.Default.Announcement, "", Color(0xFFFFC107)),
        TeacherModule("Analytics", Icons.Default.BarChart, Screen.Analytics.route, Color(0xFF00BCD4)),
        TeacherModule("Cancel", Icons.Default.Cancel, "", Color(0xFFD32F2F))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teacher Dashboard", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton(onClick = { 
                        navController.navigate(Screen.Login.route) {
                            popUpTo(0)
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = "Logout")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Welcome, Professor",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(5),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(modules) { module ->
                    TeacherModuleItem(module) {
                        if (module.route.isNotEmpty()) {
                            navController.navigate(module.route)
                        } else {
                            Toast.makeText(context, "${module.title} logic not implemented", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherModuleItem(module: TeacherModule, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(module.color.copy(alpha = 0.1f), CircleShape)
                .padding(12.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = module.icon,
                contentDescription = module.title,
                tint = module.color,
                modifier = Modifier.size(28.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = module.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontWeight = FontWeight.Medium
        )
    }
}
