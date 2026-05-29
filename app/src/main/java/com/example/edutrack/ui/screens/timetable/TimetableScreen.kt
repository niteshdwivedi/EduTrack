package com.example.edutrack.ui.screens.timetable

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.style.TextOverflow
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.TimetableEntry
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale
import java.text.SimpleDateFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableScreen(
    navController: NavHostController,
    viewModel: TimetableViewModel = hiltViewModel(),
    dashboardViewModel: com.example.edutrack.ui.screens.dashboard.DashboardViewModel = hiltViewModel() // Sync makeups from here
) {
    val timetable by viewModel.timetable.collectAsState()
    val upcomingClasses by dashboardViewModel.upcomingClasses.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    // Auto-select today
    val calendar = Calendar.getInstance()
    val todayIndex = (calendar.get(Calendar.DAY_OF_WEEK) + 5) % 7 // 0=Mon, 4=Fri
    
    val pagerState = rememberPagerState(initialPage = todayIndex, pageCount = { 7 })
    val scope = rememberCoroutineScope()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Add Filter */ }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            // Day Selector Tabs
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {},
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[pagerState.currentPage]),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            ) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                        text = { Text(day) }
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.Top
            ) { page ->
                val dayOfWeek = page + 1
                
                // For today, show synced data from dashboard (includes makeups and cancellations)
                val isToday = dayOfWeek == ((Calendar.getInstance().get(Calendar.DAY_OF_WEEK) + 5) % 7 + 1)
                
                val dayEntries = if (isToday) {
                    upcomingClasses
                } else {
                    sortTimetableEntries(timetable.filter { it.dayOfWeek == dayOfWeek })
                }
                
                if (dayEntries.isEmpty() && !isLoading) {
                    EmptyTimetable()
                } else {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(dayEntries) { entry ->
                            TimetableCard(entry) {
                                viewModel.toggleAlarm(entry.id)
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun sortTimetableEntries(entries: List<TimetableEntry>): List<TimetableEntry> {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    return entries.sortedWith { a, b ->
        try {
            val timeA = sdf.parse(a.startTime.uppercase())?.time ?: 0
            val timeB = sdf.parse(b.startTime.uppercase())?.time ?: 0
            timeA.compareTo(timeB)
        } catch (e: Exception) {
            a.startTime.compareTo(b.startTime)
        }
    }
}

@Composable
fun TimetableCard(entry: TimetableEntry, onAlarmToggle: () -> Unit) {
    val isAbsent = entry.isTeacherAbsent
    val isMakeup = entry.isMakeupClass
    val isDarkMode = isSystemInDarkTheme()

    val primaryColor = when {
        isAbsent -> Color(0xFFD32F2F) // Bold Red
        isMakeup -> Color(0xFF2196F3) // Bold Blue
        else -> MaterialTheme.colorScheme.primary
    }

    // Adaptive Background for readability
    val cardColor = when {
        isDarkMode -> MaterialTheme.colorScheme.surface // Black/Dark Surface in Dark mode
        isAbsent -> Color(0xFFFFEBEE) // Light Red in Light mode
        isMakeup -> Color(0xFFE3F2FD) // Light Blue in Light mode
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    // REDUCE height for Alarm classes (Generic non-status classes)
    val cardHeightModifier = if (!isAbsent && !isMakeup) Modifier.height(110.dp) else Modifier

    val textColor = when {
        isDarkMode && isAbsent -> Color(0xFFFF8A80) // High contrast red for dark mode
        isDarkMode && isMakeup -> Color(0xFF82B1FF) // High contrast blue for dark mode
        isAbsent -> Color(0xFFD32F2F)
        isMakeup -> Color(0xFF2196F3)
        else -> MaterialTheme.colorScheme.onSurface
    }

    // High contrast secondary text for dark mode
    val secondaryTextColor = if (isDarkMode) Color.White.copy(alpha = 0.9f) else MaterialTheme.colorScheme.onSurfaceVariant

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .then(cardHeightModifier)
            .padding(vertical = 6.dp)
            .border(
                BorderStroke(3.dp, primaryColor), // Thicker border (3.dp)
                RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Background Icon for Absent/Close (Top End)
            if (isAbsent) {
                Icon(
                    Icons.Default.Close,
                    contentDescription = null,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(18.dp),
                    tint = primaryColor
                )
            } else {
                IconButton(
                    onClick = onAlarmToggle,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        if (entry.alarmEnabled) Icons.Default.AlarmOn else Icons.Default.AlarmAdd,
                        contentDescription = "Set Alarm",
                        tint = if (entry.alarmEnabled) primaryColor else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // 1. Time Badge at TOP
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = primaryColor.copy(alpha = if (isDarkMode) 0.2f else 0.1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "${entry.startTime} - ${entry.endTime}",
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelMedium,
                            color = textColor,
                            fontWeight = FontWeight.Black
                        )
                    }
                    
                    if (isMakeup) {
                        Spacer(Modifier.width(8.dp))
                        Badge(containerColor = primaryColor) {
                            Text(
                                "Makeup Class", 
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Subject Name - Reduced size to titleMedium
                Text(
                    text = entry.subject,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = textColor
                )

                // 3. Subject Code & Teacher ID - Always shown
                val subjectCode = if (entry.subjectCode.isEmpty()) "COURSE" else entry.subjectCode
                Text(
                    text = "$subjectCode (${entry.teacherId})",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = textColor.copy(alpha = 0.9f)
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 4. Teacher & Room - Badged Style for visibility
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Teacher Badge
                    Surface(
                        color = primaryColor.copy(alpha = if (isDarkMode) 0.3f else 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = textColor)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = entry.teacherName,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkMode) Color.White else textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    // Room Badge
                    Surface(
                        color = primaryColor.copy(alpha = if (isDarkMode) 0.3f else 0.1f),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Room, null, modifier = Modifier.size(14.dp), tint = textColor)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                text = if (entry.cabinNumber.isNotEmpty()) "${entry.roomNumber} (${entry.cabinNumber})" else entry.roomNumber,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (isDarkMode) Color.White else textColor,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                // 5. Absent Status Bar
                if (isAbsent) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        color = primaryColor.copy(alpha = if (isDarkMode) 0.2f else 0.1f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Cancel, null, tint = textColor, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Teacher Absent",
                                style = MaterialTheme.typography.labelLarge,
                                color = textColor,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyTimetable() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                Icons.Default.EventBusy,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text("No classes today!", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.outline)
            Text("Enjoy your free time!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
    }
}
