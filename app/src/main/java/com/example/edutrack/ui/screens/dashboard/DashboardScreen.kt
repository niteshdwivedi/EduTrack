package com.example.edutrack.ui.screens.dashboard

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.TimetableEntry
import com.example.edutrack.ui.navigation.Screen
import kotlinx.coroutines.launch

data class DashboardItem(
    val title: String,
    val icon: ImageVector,
    val route: String,
    val color: Color,
    val badgeCount: Int = 0
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavHostController,
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val userName by viewModel.userName.collectAsState()
    val attendancePercentage by viewModel.attendancePercentage.collectAsState()
    val pendingAssignments by viewModel.pendingAssignmentsCount.collectAsState()
    val todayAbsences by viewModel.todayTeacherAbsences.collectAsState()
    val todayMakeupClasses by viewModel.todayMakeupClasses.collectAsState()
    val upcomingClasses by viewModel.upcomingClasses.collectAsState()
    val examCountdown by viewModel.nextExamCountdown.collectAsState()
    
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    // FULL LIST RESTORED
    val quickActions = listOf(
        DashboardItem("Attendance", Icons.Default.CheckCircle, Screen.Attendance.route, Color(0xFF4CAF50)),
        DashboardItem("Exams", Icons.Default.Event, Screen.Exams.route, Color(0xFF6200EE)),
        DashboardItem("Timetable", Icons.Default.DateRange, Screen.Timetable.route, Color(0xFF2196F3)),
        DashboardItem("Notes", Icons.Default.Edit, Screen.Notes.route, Color(0xFFFF9800)),
        DashboardItem("Assignment", Icons.Default.Assignment, Screen.Assignments.route, Color(0xFFE91E63)),
        DashboardItem("Job Portal", Icons.Default.Work, Screen.JobPortal.route, Color(0xFF9C27B0)),
        DashboardItem("GPA Calc", Icons.Default.Calculate, Screen.GPACalculator.route, Color(0xFF00BCD4)),
        DashboardItem("EduChat", Icons.Default.Chat, Screen.ChatList.route, Color(0xFF4CAF50)),
        DashboardItem("AI Chat", Icons.Default.AutoAwesome, Screen.AIAssistant.route, Color(0xFF673AB7)),
        DashboardItem("Absents", Icons.Default.PersonOff, Screen.TeacherAbsent.route, Color(0xFFF44336), todayAbsences),
        DashboardItem("Makeup", Icons.Default.AddAlert, Screen.MakeupClass.route, Color(0xFF2196F3), todayMakeupClasses),
        DashboardItem("Timer", Icons.Default.Timer, Screen.StudyTimer.route, Color(0xFF795548)),
        DashboardItem("Resources", Icons.Default.Book, Screen.Resources.route, Color(0xFF607D8B)),
        DashboardItem("Analytics", Icons.Default.BarChart, Screen.Analytics.route, Color(0xFF3F51B5)),
        DashboardItem("Reminders", Icons.Default.Notifications, Screen.Reminders.route, Color(0xFFFFC107))
    )

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                EduTrackDrawerContent(navController, userName)
            }
        }
    ) {
        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "EduTrack",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.headlineSmall
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(Icons.Default.Menu, contentDescription = "Menu")
                        }
                    },
                    actions = {
                        IconButton(onClick = { navController.navigate(Screen.Search.route) }) {
                            Icon(Icons.Default.Search, contentDescription = "Search")
                        }
                        IconButton(onClick = { navController.navigate(Screen.Profile.route) }) {
                            Icon(Icons.Default.AccountCircle, contentDescription = "Profile")
                        }
                    }
                )
            },
            bottomBar = {
                EduTrackBottomBar(navController)
            }
        ) { padding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // 1. Welcome Header
                item {
                    WelcomeHeader(userName)
                }

                // 2. Banner Pager (NOW AT TOP with countdown)
                item {
                    BannerPager(examCountdown, navController)
                }

                // 3. Motivation Quote
                item {
                    MotivationQuote()
                }

                // 4. Attendance & Assignments Summary
                item {
                    SummarySection(navController, attendancePercentage, pendingAssignments)
                }

                // 5. Quick Actions Grid (Now with all icons back)
                item {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }

                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        items(quickActions) { item ->
                            QuickActionCard(item) {
                                navController.navigate(item.route)
                            }
                        }
                    }
                }

                // 6. Today's Schedule (Upcoming Classes + Makeups)
                item {
                    SectionHeader("Today's Schedule", "Full Timetable") {
                        navController.navigate(Screen.Timetable.route)
                    }
                }

                if (upcomingClasses.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Text(
                                "No classes scheduled for today.",
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                } else {
                    items(upcomingClasses) { entry ->
                        ClassItem(entry) {
                            navController.navigate(Screen.Timetable.route)
                        }
                    }
                }

                if (todayAbsences > 0 || todayMakeupClasses > 0) {
                    item {
                        Text(
                            text = "Urgent Alerts",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    item {
                        AlertSection(navController, todayAbsences, todayMakeupClasses)
                    }
                }

                // 7. University News & Placement
                item {
                    SectionHeader("University News & Placement", "View All") {
                        navController.navigate(Screen.JobPortal.route)
                    }
                }

                item {
                    val pagerState = rememberPagerState(pageCount = { 3 })
                    HorizontalPager(
                        state = pagerState,
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        pageSpacing = 12.dp
                    ) { page ->
                        val (company, color, date) = when(page) {
                            0 -> Triple("Google Recruitment Drive 2024", Color(0xFF2D2D2D), "25 May 2026")
                            1 -> Triple("Microsoft Hiring 2024", Color(0xFF00A4EF).copy(alpha = 0.8f), "28 May 2026")
                            else -> Triple("Amazon SDE Roles", Color(0xFFFF9900).copy(alpha = 0.8f), "01 June 2026")
                        }
                        PlacementCard(company, color, date) {
                            // In a real app, you'd pass the ID to navigate specifically
                            navController.navigate(Screen.JobPortal.route)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
fun PlacementCard(company: String, bgColor: Color, date: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(24.dp)
            ) {
                Surface(
                    color = Color.White.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = "PLACEMENT",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = company,
                    style = MaterialTheme.typography.headlineSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Drive Date: $date",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.LightGray
                )
                Text(
                    text = "Click to view details",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.LightGray.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
fun WelcomeHeader(userName: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Good Morning,",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.outline
        )
        Text(
            text = userName,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BannerPager(examCountdown: String, navController: NavHostController) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    HorizontalPager(
        state = pagerState,
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
        contentPadding = PaddingValues(horizontal = 16.dp),
        pageSpacing = 8.dp
    ) { page ->
        Card(
            modifier = Modifier.fillMaxSize().clickable {
                when(page) {
                    0 -> navController.navigate(Screen.Exams.route)
                    1 -> navController.navigate(Screen.Resources.route)
                    else -> navController.navigate(Screen.Notes.route)
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = when(page) {
                    0 -> Color(0xFF6200EE)
                    1 -> Color(0xFF03DAC5)
                    else -> Color(0xFF018786)
                }
            )
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = when(page) {
                            0 -> "Mid-Term Exams Start Next Week!"
                            1 -> "Join the Workshop on AI/ML"
                            else -> "New Notes Uploaded for Physics"
                        },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (page == 0 && examCountdown.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = examCountdown,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MotivationQuote() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.FormatQuote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "The beautiful thing about learning is that no one can take it away from you.",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
fun SummarySection(navController: NavHostController, attendance: Int, assignments: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        SummaryCard(
            title = "Attendance",
            value = "$attendance%",
            subtitle = "Overall",
            color = Color(0xFF4CAF50),
            modifier = Modifier.weight(1f)
        ) {
            navController.navigate(Screen.Attendance.route)
        }
        SummaryCard(
            title = "Assignments",
            value = String.format("%02d", assignments),
            subtitle = "Pending",
            color = Color(0xFFFF5722),
            modifier = Modifier.weight(1f)
        ) {
            navController.navigate(Screen.Assignments.route)
        }
    }
}

@Composable
fun SummaryCard(
    title: String,
    value: String,
    subtitle: String,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = title, style = MaterialTheme.typography.labelLarge, color = color)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = value, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text(text = subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
fun QuickActionCard(item: DashboardItem, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(80.dp)
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.TopEnd) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(item.color.copy(alpha = 0.1f), CircleShape)
                    .border(1.dp, item.color.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(item.icon, contentDescription = item.title, tint = item.color)
            }
            if (item.badgeCount > 0) {
                Badge(
                    containerColor = MaterialTheme.colorScheme.error,
                    modifier = Modifier.offset(x = 4.dp, y = (-4).dp)
                ) {
                    Text(item.badgeCount.toString())
                }
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = item.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
    }
}

@Composable
fun SectionHeader(title: String, actionText: String?, onActionClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title, 
            style = MaterialTheme.typography.titleMedium, 
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f),
            maxLines = 1,
            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
        )
        if (actionText != null) {
            Text(
                text = actionText,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clickable(onClick = onActionClick)
            )
        }
    }
}

@Composable
fun ClassItem(entry: TimetableEntry, onClick: () -> Unit) {
    val isAbsent = entry.isTeacherAbsent
    val isMakeup = entry.isMakeupClass
    
    val cardColor = when {
        isAbsent -> Color(0xFFFFEBEE)
        isMakeup -> Color(0xFFE3F2FD)
        else -> Color.White
    }
    
    val sideBarColor = when {
        isAbsent -> Color(0xFFD32F2F)
        isMakeup -> Color(0xFF2196F3)
        else -> {
            // Assign some default colors based on subject or index if we want
            if (entry.id.toIntOrNull()?.let { it % 2 == 0 } == true) Color(0xFF673AB7) else Color(0xFF2196F3)
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // Left Color Bar
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(sideBarColor)
            )
            
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${entry.subjectCode} (${entry.teacherId})",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (isAbsent) Color(0xFFD32F2F).copy(alpha = 0.7f) else sideBarColor
                            )
                            if (entry.section.isNotEmpty()) {
                                Text(
                                    text = " • Sec ${entry.section}",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAbsent) Color(0xFFD32F2F).copy(alpha = 0.6f) else sideBarColor.copy(alpha = 0.8f)
                                )
                            }
                        }
                        Text(
                            text = entry.subject,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isAbsent) Color(0xFFD32F2F) else Color.Black
                        )
                    }
                    
                    Column(horizontalAlignment = Alignment.End) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = sideBarColor.copy(alpha = 0.1f),
                        ) {
                            Text(
                                text = "Room ${entry.roomNumber} (${entry.cabinNumber})",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = sideBarColor
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = entry.teacherName,
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.Gray,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isAbsent) Icons.Default.Cancel else Icons.Default.AccessTime,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = if (isAbsent) Color(0xFFD32F2F) else Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${entry.startTime} - ${entry.endTime}",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isAbsent) Color(0xFFD32F2F) else Color.Gray
                    )
                    
                    if (isAbsent) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Cancelled",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                    }
                    if (isMakeup) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Makeup Class",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = sideBarColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AlertSection(navController: NavHostController, absences: Int, makeup: Int) {
    Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        if (absences > 0) {
            AlertCard(
                title = "Teacher Absent",
                message = "$absences teachers are absent today.",
                icon = Icons.Default.PersonOff,
                color = Color(0xFFF44336)
            ) {
                navController.navigate(Screen.TeacherAbsent.route)
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
        if (makeup > 0) {
            AlertCard(
                title = "Makeup Class",
                message = "You have $makeup makeup classes today.",
                icon = Icons.Default.AddAlert,
                color = Color(0xFF2196F3)
            ) {
                navController.navigate(Screen.MakeupClass.route)
            }
        }
    }
}

@Composable
fun AlertCard(title: String, message: String, icon: ImageVector, color: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.05f)),
        border = BorderStroke(if (title == "Makeup Class") 2.dp else 1.dp, if (title == "Makeup Class") Color(0xFF00C853) else color.copy(alpha = 0.2f))
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (title == "Makeup Class") Color(0xFF00C853) else color)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = if (title == "Makeup Class") Color(0xFF00C853) else color, fontWeight = FontWeight.Bold)
                Text(text = message, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
fun EduTrackBottomBar(navController: NavHostController) {
    val items = listOf(
        Screen.Dashboard to Icons.Default.Home to "Dashboard",
        Screen.Timetable to Icons.Default.DateRange to "Timetable",
        Screen.Notes to Icons.Default.Edit to "Notes",
        Screen.Reminders to Icons.Default.Notifications to "Reminders",
        Screen.Profile to Icons.Default.Person to "Profile"
    )
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        items.forEach { (pair, label) ->
            val (screen, icon) = pair
            NavigationBarItem(
                icon = { Icon(icon, contentDescription = label) },
                label = { Text(label, fontSize = 9.sp, maxLines = 1, softWrap = false) },
                selected = false,
                onClick = { navController.navigate(screen.route) },
                alwaysShowLabel = true
            )
        }
    }
}

@Composable
fun EduTrackDrawerContent(navController: NavHostController, userName: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        userName.take(1).uppercase(),
                        color = Color.White,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(text = userName, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Student | Semester 6", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.outline)
            }
        }
        
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        DrawerItem("Dashboard", Icons.Default.Dashboard) { navController.navigate(Screen.Dashboard.route) }
        DrawerItem("Attendance", Icons.Default.CheckCircle) { navController.navigate(Screen.Attendance.route) }
        DrawerItem("Subjects", Icons.Default.Book) { navController.navigate(Screen.Resources.route) }
        DrawerItem("Assignments", Icons.Default.Assignment) { navController.navigate(Screen.Assignments.route) }
        DrawerItem("Exams", Icons.Default.Event) { navController.navigate(Screen.Exams.route) }
        DrawerItem("Jobs", Icons.Default.Work) { navController.navigate(Screen.JobPortal.route) }
        DrawerItem("Settings", Icons.Default.Settings) { navController.navigate(Screen.Settings.route) }
        
        Spacer(modifier = Modifier.weight(1f))
        
        DrawerItem("Logout", Icons.Default.Logout) { 
            navController.navigate(Screen.Login.route) {
                popUpTo(0)
            }
        }
    }
}

@Composable
fun DrawerItem(title: String, icon: ImageVector, onClick: () -> Unit) {
    NavigationDrawerItem(
        label = { Text(title) },
        selected = false,
        onClick = onClick,
        icon = { Icon(icon, contentDescription = null) },
        modifier = Modifier.padding(vertical = 4.dp)
    )
}
