package com.example.edutrack.ui.screens.exams

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.Exam
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExamsScreen(
    navController: NavHostController,
    viewModel: ExamsViewModel = hiltViewModel()
) {
    val upcomingExams by viewModel.upcomingExams.collectAsState()
    val historyExams by viewModel.historyExams.collectAsState()
    val alarmDays by viewModel.alarmDays.collectAsState()
    val alarmHours by viewModel.alarmHours.collectAsState()
    val alarmMinutes by viewModel.alarmMinutes.collectAsState()
    val currentTime by viewModel.currentTime.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val ringtoneLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { viewModel.setCustomRingtone(it.toString()) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Exam Management", fontWeight = FontWeight.Black) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Smart Alarm Controller
                item {
                    SmartAlarmController(
                        alarmDays = alarmDays,
                        alarmHours = alarmHours,
                        alarmMinutes = alarmMinutes,
                        onIncrementDays = { viewModel.incrementAlarmDays() },
                        onIncrementHours = { viewModel.incrementAlarmHours() },
                        onIncrementMinutes = { viewModel.incrementAlarmMinutes() },
                        onReset = { viewModel.resetAlarm() },
                        onChangeRingtone = { ringtoneLauncher.launch("audio/*") }
                    )
                }

                // Upcoming Exams
                if (upcomingExams.isNotEmpty()) {
                    item {
                        Text(
                            "Upcoming Exams",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    }
                    items(upcomingExams) { exam ->
                        UpcomingExamCard(
                            exam = exam,
                            alarmD = alarmDays,
                            alarmH = alarmHours,
                            alarmM = alarmMinutes,
                            currentTime = currentTime,
                            onChangeRingtone = { ringtoneLauncher.launch("audio/*") }
                        )
                    }
                }

                // History
                if (historyExams.isNotEmpty()) {
                    item {
                        Text(
                            "Exam History",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(historyExams) { exam ->
                        HistoryExamCard(exam)
                    }
                }
                
                item { Spacer(modifier = Modifier.height(32.dp)) }
            }
        }
    }
}

@Composable
fun SmartAlarmController(
    alarmDays: Int,
    alarmHours: Int,
    alarmMinutes: Int,
    onIncrementDays: () -> Unit,
    onIncrementHours: () -> Unit,
    onIncrementMinutes: () -> Unit,
    onReset: () -> Unit,
    onChangeRingtone: () -> Unit
) {
    val isActive = alarmDays > 0 || alarmHours > 0 || alarmMinutes > 0
    val tealColor = Color(0xFF009688) // Same as "New Notes" banner
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) tealColor else MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) { // Reduced padding from 24 to 16
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (isActive) Icons.Default.AlarmOn else Icons.Default.AlarmOff,
                        contentDescription = null,
                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Smart Alarm Controller",
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall // Reduced from titleMedium
                    )
                }
                IconButton(
                    onClick = onReset,
                    modifier = Modifier.size(24.dp) // Smaller button
                ) {
                    Icon(
                        Icons.Default.Refresh, 
                        contentDescription = "Reset", 
                        tint = if (isActive) Color.White else MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp)) // Reduced from 24 to 8
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        "ALARM DURATION",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isActive) Color.White.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline,
                        fontSize = 9.sp
                    )
                    Text(
                        "${alarmDays}d ${alarmHours}h ${alarmMinutes}m",
                        style = MaterialTheme.typography.titleLarge, // Reduced from displaySmall
                        fontWeight = FontWeight.Black,
                        color = if (isActive) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Button(
                        onClick = onIncrementDays,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color.White.copy(alpha = 0.2f) else tealColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Day", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = onIncrementHours,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color.White.copy(alpha = 0.2f) else tealColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("Hour", fontSize = 11.sp, color = Color.White)
                    }
                    Button(
                        onClick = onIncrementMinutes,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                        modifier = Modifier.height(32.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) Color.White.copy(alpha = 0.2f) else tealColor
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text("5 Min", fontSize = 11.sp, color = Color.White)
                    }
                }
            }
            
            AnimatedVisibility(visible = isActive) {
                Column {
                    Spacer(modifier = Modifier.height(12.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Alarm rings $alarmDays days, $alarmHours hours and $alarmMinutes minutes before exam.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun UpcomingExamCard(
    exam: Exam,
    alarmD: Int,
    alarmH: Int,
    alarmM: Int,
    currentTime: Long,
    onChangeRingtone: () -> Unit
) {
    val sdf = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.getDefault())
    val examTime = try { sdf.parse("${exam.date} ${exam.time}")?.time ?: 0L } catch (e: Exception) { 0L }
    
    val alarmRingTime = if (alarmD > 0 || alarmH > 0 || alarmM > 0) {
        examTime - TimeUnit.DAYS.toMillis(alarmD.toLong()) - TimeUnit.HOURS.toMillis(alarmH.toLong()) - TimeUnit.MINUTES.toMillis(alarmM.toLong())
    } else 0L

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFFF9800), Color(0xFFFFC107))
                    )
                )
                .padding(12.dp)
        ) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(exam.subjectCode, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color.White.copy(alpha = 0.8f))
                            if (exam.section.isNotEmpty()) {
                                Text(" • Section ${exam.section}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, color = Color.White.copy(alpha = 0.7f))
                            }
                        }
                        Text(exam.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Color.White, maxLines = 1, overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis)
                    }
                    Box(
                        modifier = Modifier
                            .background(Color.White.copy(alpha = 0.2f), CircleShape)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("Room ${exam.venue}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 10.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CalendarToday, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(exam.date, color = Color.White, style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(12.dp))
                    Icon(Icons.Default.AccessTime, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(exam.time, color = Color.White, style = MaterialTheme.typography.bodySmall)
                }

                if (alarmRingTime > 0) {
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = Color.White.copy(alpha = 0.2f))
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val alarmSdf = SimpleDateFormat("dd MMM • hh:mm a", Locale.getDefault())
                    val alarmTimeString = alarmSdf.format(Date(alarmRingTime))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Alarm at: $alarmTimeString",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            
                            if (alarmRingTime > currentTime) {
                                val timeDiff = alarmRingTime - currentTime
                                val days = TimeUnit.MILLISECONDS.toDays(timeDiff)
                                val hours = TimeUnit.MILLISECONDS.toHours(timeDiff) % 24
                                val minutes = TimeUnit.MILLISECONDS.toMinutes(timeDiff) % 60
                                val seconds = TimeUnit.MILLISECONDS.toSeconds(timeDiff) % 60

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Alarm, contentDescription = null, tint = Color.Black, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = String.format("Rings in: %dD %02dH %02dM %02dS", days, hours, minutes, seconds),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                        
                        IconButton(
                            onClick = onChangeRingtone,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(Icons.Default.MusicNote, contentDescription = "Tone", tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryExamCard(exam: Exam) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(exam.subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(exam.date, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Attendance", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        exam.attendance,
                        fontWeight = FontWeight.Bold,
                        color = if (exam.attendance == "Present") Color(0xFF4CAF50) else Color(0xFFF44336)
                    )
                }
                Column {
                    Text("Marks", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(exam.marks?.toString()?.ifEmpty { "N/A" } ?: "N/A", fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Grade", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(exam.grade.ifEmpty { "N/A" }, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Result", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
                    Text(
                        exam.resultStatus ?: "N/A",
                        fontWeight = FontWeight.Bold,
                        color = when(exam.resultStatus) {
                            "Published" -> Color(0xFF4CAF50)
                            "Pending" -> Color(0xFFFF9800)
                            else -> MaterialTheme.colorScheme.outline
                        }
                    )
                }
            }
        }
    }
}
