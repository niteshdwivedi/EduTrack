package com.example.edutrack.ui.screens.attendance

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.SubjectAttendance
import com.example.edutrack.data.model.AttendanceRecord

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceScreen(
    navController: NavHostController,
    viewModel: AttendanceViewModel = hiltViewModel()
) {
    val subjects by viewModel.subjects.collectAsState()
    val overallPercentage by viewModel.overallPercentage.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var selectedSubject by remember { mutableStateOf<SubjectAttendance?>(null) }
    val sheetState = rememberModalBottomSheetState()
    var showSheet by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Attendance", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { /* Search */ }) {
                        Icon(Icons.Default.Search, contentDescription = "Search")
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
                // Analytics Summary
                item {
                    AttendanceSummaryHeader(subjects, overallPercentage)
                }

                item {
                    Text(
                        text = "Subject-wise Attendance",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(subjects) { subject ->
                    AttendanceCard(subject) {
                        selectedSubject = subject
                        showSheet = true
                    }
                }
            }
        }

        if (showSheet && selectedSubject != null) {
            ModalBottomSheet(
                onDismissRequest = { showSheet = false },
                sheetState = sheetState
            ) {
                AttendanceDetailContent(selectedSubject!!)
            }
        }
    }
}

@Composable
fun AttendanceSummaryHeader(subjects: List<SubjectAttendance>, percentage: Int) {
    val totalAttended = subjects.sumOf { it.attended }
    val totalClasses = subjects.sumOf { it.total }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary)
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Overall Attendance", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelLarge)
                Text("$percentage%", color = Color.White, style = MaterialTheme.typography.displayMedium, fontWeight = FontWeight.Bold)
                Text("$totalAttended / $totalClasses Classes", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.bodyMedium)
            }
            Box(
                modifier = Modifier.size(80.dp),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(
                    progress = { percentage / 100f },
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White,
                    strokeWidth = 8.dp,
                    trackColor = Color.White.copy(alpha = 0.2f),
                )
            }
        }
    }
}

@Composable
fun AttendanceCard(subject: SubjectAttendance, onClick: () -> Unit) {
    val percentage = if (subject.total > 0) (subject.attended.toFloat() / subject.total * 100).toInt() else 0
    val statusColor = when {
        percentage >= 75 -> Color(0xFF4CAF50)
        percentage >= 60 -> Color(0xFFFF9800)
        else -> Color(0xFFF44336)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = subject.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text(text = subject.courseCode + " • " + subject.teacherName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                }
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LinearProgressIndicator(
                progress = { percentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = statusColor,
                trackColor = statusColor.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Present: ${subject.attended}", style = MaterialTheme.typography.bodySmall)
                Text(text = "Total: ${subject.total}", style = MaterialTheme.typography.bodySmall)
            }
            
            if (percentage < 75) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFF44336), modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Low attendance warning!", color = Color(0xFFF44336), style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

@Composable
fun AttendanceDetailContent(subject: SubjectAttendance) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "Attendance History",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = subject.name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.primary
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        subject.history.forEach { record ->
            AttendanceHistoryItem(record)
            HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), thickness = 0.5.dp)
        }
        
        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun AttendanceHistoryItem(record: AttendanceRecord) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(
                    if (record.status == "Present") Color(0xFF4CAF50) else Color(0xFFF44336),
                    CircleShape
                )
        )
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = record.date, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
            Text(text = record.time + " • " + record.room, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Text(
            text = record.status,
            style = MaterialTheme.typography.labelLarge,
            color = if (record.status == "Present") Color(0xFF4CAF50) else Color(0xFFF44336),
            fontWeight = FontWeight.Bold
        )
    }
}
