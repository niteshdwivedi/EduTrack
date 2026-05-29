package com.example.edutrack.ui.screens.teacher_absent

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.edutrack.data.datastore.SettingsDataStore
import com.example.edutrack.data.model.Teacher
import com.example.edutrack.data.model.TeacherAbsentEntry
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.ui.components.EduTrackTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class TeacherLeaveViewModel @Inject constructor(
    private val repository: FirestoreRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {
    private val _currentStatus = MutableStateFlow("Active")
    val currentStatus: StateFlow<String> = _currentStatus

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _absences = MutableStateFlow<List<TeacherAbsentEntry>>(emptyList())
    val absences: StateFlow<List<TeacherAbsentEntry>> = _absences

    private val _userRole = MutableStateFlow("Student")
    val userRole: StateFlow<String> = _userRole

    init {
        loadAbsences()
        viewModelScope.launch {
            settingsDataStore.userRole.collect { _userRole.value = it ?: "Student" }
        }
    }

    fun loadAbsences() {
        viewModelScope.launch {
            val data = repository.getTeacherAbsences()
            if (data.isEmpty()) {
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                _absences.value = listOf(
                    TeacherAbsentEntry(
                        id = "mock_amit",
                        teacherName = "Dr. Amit Verma",
                        date = today,
                        status = "Absent",
                        subject = "Mathematics",
                        period = "09:00 AM - 10:30 AM"
                    )
                )
            } else {
                _absences.value = data
            }
        }
    }

    fun deleteAbsence(id: String) {
        viewModelScope.launch {
            repository.deleteTeacherAbsence(id)
            loadAbsences()
        }
    }

    fun updateStatus(teacherId: String, name: String, status: String) {
        viewModelScope.launch {
            _isLoading.value = true
            // Update the teacher's main status in the teachers collection
            val teacherSnapshot = repository.firestore.collection("teachers")
                .whereEqualTo("teacherId", teacherId).get().await()
            
            if (!teacherSnapshot.isEmpty) {
                val docId = teacherSnapshot.documents[0].id
                repository.firestore.collection("teachers").document(docId).update("status", status).await()
            }

            _currentStatus.value = status
            
            if (status == "On Leave") {
                val today = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(Date())
                repository.addTeacherAbsence(
                    TeacherAbsentEntry(
                        id = UUID.randomUUID().toString(),
                        teacherName = name,
                        date = today,
                        status = "On Leave"
                    )
                )
            }
            loadAbsences()
            _isLoading.value = false
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherAbsentScreen(
    navController: NavHostController,
    viewModel: TeacherLeaveViewModel = hiltViewModel()
) {
    val absences by viewModel.absences.collectAsState()
    val status by viewModel.currentStatus.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val isTeacher = userRole == "Teacher"

    var showLeaveDialog by remember { mutableStateOf(false) }
    
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
                        Text(if (isTeacher) "Leave Management" else "Teacher On Leave")
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
                            selectedIds.forEach { viewModel.deleteAbsence(it) }
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
                FloatingActionButton(onClick = { showLeaveDialog = true }, containerColor = MaterialTheme.colorScheme.error) {
                    Icon(Icons.Default.Add, contentDescription = "Apply Leave", tint = Color.White)
                }
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (!isSelectionMode && isTeacher) {
                StatusCard(status)
                
                Text(
                    "Recent Absence Records",
                    modifier = Modifier.padding(16.dp),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            if (isLoading) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }

            LazyColumn(modifier = Modifier.weight(1f), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(absences) { absence ->
                    val isSelected = selectedIds.contains(absence.id)
                    AbsenceCard(
                        absence = absence,
                        isSelected = isSelected,
                        onLongClick = {
                            isSelectionMode = true
                            selectedIds = selectedIds + absence.id
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (isSelected) {
                                    selectedIds - absence.id
                                } else {
                                    selectedIds + absence.id
                                }
                                if (selectedIds.isEmpty()) isSelectionMode = false
                            }
                        }
                    )
                }
            }
        }

        if (showLeaveDialog) {
            LeaveDialog(
                onDismiss = { showLeaveDialog = false },
                onConfirm = { id, name ->
                    viewModel.updateStatus(id, name, "On Leave")
                    showLeaveDialog = false
                }
            )
        }
    }
}

@Composable
fun StatusCard(status: String) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFEBEE)
        )
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Current Status", style = MaterialTheme.typography.labelMedium)
                Text(status, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = if (status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828))
            }
            Icon(
                if (status == "Active") Icons.Default.CheckCircle else Icons.Default.PersonOff,
                null,
                tint = if (status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828),
                modifier = Modifier.size(48.dp)
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AbsenceCard(
    absence: TeacherAbsentEntry,
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
        border = BorderStroke(
            3.dp, // Thicker Glowing Red Border
            if (isSelected) MaterialTheme.colorScheme.primary else Color(0xFFD32F2F)
        ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else Color(0xFFFFEBEE)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(absence.teacherName, fontWeight = FontWeight.Black, color = Color(0xFFD32F2F))
            Text("Date: ${absence.date}", style = MaterialTheme.typography.bodySmall, color = Color(0xFFD32F2F).copy(alpha = 0.7f))
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                color = Color(0xFFD32F2F),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "TEACHER ON LEAVE",
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun LeaveDialog(onDismiss: () -> Unit, onConfirm: (String, String) -> Unit) {
    var id by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Apply for Leave") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EduTrackTextField(value = id, onValueChange = { id = it }, label = "Your Teacher ID")
                EduTrackTextField(value = name, onValueChange = { name = it }, label = "Your Name")
            }
        },
        confirmButton = {
            Button(onClick = { onConfirm(id, name) }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) {
                Text("Mark as On Leave")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
