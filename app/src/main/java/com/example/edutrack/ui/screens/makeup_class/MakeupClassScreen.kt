package com.example.edutrack.ui.screens.makeup_class

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.MakeupClass
import com.example.edutrack.ui.components.EduTrackTextField
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeupClassScreen(
    navController: NavHostController,
    viewModel: MakeupClassViewModel = hiltViewModel()
) {
    val makeupClasses by viewModel.makeupClasses.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isTeacher = userRole == "Teacher"
    
    var showAddDialog by remember { mutableStateOf(false) }
    
    // Multi-select state
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var isSelectionMode by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    if (isSelectionMode) {
                        Text("${selectedIds.size} Selected")
                    } else {
                        Text("Makeup Class System")
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedIds = emptySet()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { 
                            selectedIds.forEach { viewModel.deleteMakeupClass(it) }
                            isSelectionMode = false
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode && isTeacher) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Schedule Makeup")
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(makeupClasses) { item ->
                val isSelected = selectedIds.contains(item.id)
                MakeupClassCard(
                    makeupClass = item,
                    isSelected = isSelected,
                    onLongClick = {
                        isSelectionMode = true
                        selectedIds = selectedIds + item.id
                    },
                    onClick = {
                        if (isSelectionMode) {
                            selectedIds = if (isSelected) {
                                selectedIds - item.id
                            } else {
                                selectedIds + item.id
                            }
                            if (selectedIds.isEmpty()) isSelectionMode = false
                        }
                    }
                )
            }
        }

        if (showAddDialog) {
            AddMakeupDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { makeup ->
                    viewModel.addMakeupClass(makeup)
                    showAddDialog = false
                }
            )
        }
    }
}

@Composable
fun AddMakeupDialog(onDismiss: () -> Unit, onAdd: (MakeupClass) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var teacher by remember { mutableStateOf("") }
    var section by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var time by remember { mutableStateOf("") }
    var venue by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Schedule Makeup Class") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EduTrackTextField(value = subject, onValueChange = { subject = it }, label = "Subject")
                EduTrackTextField(value = teacher, onValueChange = { teacher = it }, label = "Teacher")
                EduTrackTextField(value = section, onValueChange = { section = it }, label = "Section (e.g. 223BD)")
                EduTrackTextField(value = date, onValueChange = { date = it }, label = "Date (DD-MM-YYYY)")
                EduTrackTextField(value = time, onValueChange = { time = it }, label = "Time (e.g. 10:00 AM - 11:00 AM)")
                EduTrackTextField(value = venue, onValueChange = { venue = it }, label = "Venue (Room)")
            }
        },
        confirmButton = {
            Button(onClick = { 
                onAdd(MakeupClass(
                    id = UUID.randomUUID().toString(), 
                    subject = subject, 
                    teacher = teacher, 
                    date = date, 
                    time = time, 
                    venue = venue
                    // In a real scenario we'd add section to the model too
                ))
            }) {
                Text("Schedule")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MakeupClassCard(
    makeupClass: MakeupClass,
    isSelected: Boolean = false,
    onLongClick: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(
            2.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFF00C853)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = makeupClass.subject, 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Teacher: ${makeupClass.teacher}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Date: ${makeupClass.date}", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Time: ${makeupClass.time}", 
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
            Text(
                text = "Venue: ${makeupClass.venue}", 
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
            )
        }
    }
}
