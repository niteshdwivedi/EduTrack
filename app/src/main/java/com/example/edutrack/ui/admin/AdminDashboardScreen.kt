package com.example.edutrack.ui.admin

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

data class AdminModule(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminDashboardScreen(navController: NavHostController) {
    val context = LocalContext.current
    val modules = listOf(
        AdminModule("Students", Icons.Default.People, Screen.AdminStudentManagement.route, Color(0xFF4CAF50)),
        AdminModule("Teachers", Icons.Default.SupervisorAccount, Screen.AdminTeacherManagement.route, Color(0xFF2196F3)),
        AdminModule("Sections", Icons.Default.AccountTree, Screen.AdminSectionManagement.route, Color(0xFFFF9800)),
        AdminModule("Timetable", Icons.Default.CalendarToday, Screen.AdminTimetableManagement.route, Color(0xFFE91E63)),
        AdminModule("Results", Icons.Default.Assessment, "", Color(0xFF9C27B0)),
        AdminModule("Attendance", Icons.Default.CheckCircle, "", Color(0xFF00BCD4)),
        AdminModule("Subjects", Icons.Default.Book, "", Color(0xFF673AB7)),
        AdminModule("Exams", Icons.Default.Event, "", Color(0xFFFF5722)),
        AdminModule("Notifications", Icons.Default.Notifications, "", Color(0xFF3F51B5)),
        AdminModule("Holidays", Icons.Default.BeachAccess, "", Color(0xFF795548)),
        AdminModule("Makeup", Icons.Default.AddAlert, "", Color(0xFF607D8B)),
        AdminModule("Fees", Icons.Default.Payments, "", Color(0xFF8BC34A)),
        AdminModule("Placements", Icons.Default.Work, "", Color(0xFFFFC107)),
        AdminModule("Analytics", Icons.Default.BarChart, "", Color(0xFFCDDC39)),
        AdminModule("Settings", Icons.Default.Settings, "", Color(0xFFF44336)),
        AdminModule("Rooms", Icons.Default.MeetingRoom, "", Color(0xFF009688)),
        AdminModule("Notes", Icons.Default.Description, "", Color(0xFFE91E63)),
        AdminModule("Assignments", Icons.Default.Assignment, "", Color(0xFF3F51B5)),
        AdminModule("Teacher Leaves", Icons.Default.PersonOff, "", Color(0xFFFF9800)),
        AdminModule("AI Analytics", Icons.Default.AutoAwesome, "", Color(0xFF673AB7))
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Admin Dashboard", fontWeight = FontWeight.Bold) },
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
                text = "University Management",
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
                    AdminModuleItem(module) {
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
fun AdminModuleItem(module: AdminModule, onClick: () -> Unit) {
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
