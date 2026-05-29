package com.example.edutrack.ui.admin.teacher

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.Teacher
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.ui.components.EduTrackTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminTeacherViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    private val _teachers = MutableStateFlow<List<Teacher>>(emptyList())
    val teachers: StateFlow<List<Teacher>> = _teachers

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        loadTeachers()
    }

    fun loadTeachers() {
        viewModelScope.launch {
            _isLoading.value = true
            val data = repository.getAllTeachers()
            
            // If data is empty, we might want to populate some defaults for the user
            if (data.isEmpty()) {
                // Potential initial population logic could go here if needed
            }
            
            _teachers.value = data
            _isLoading.value = false
        }
    }

    fun addOrUpdateTeacher(teacher: Teacher) {
        viewModelScope.launch {
            // Default password if new teacher
            val teacherToSave = if (teacher.password == null || teacher.password.toString().isEmpty()) {
                teacher.copy(password = "123456")
            } else teacher
            
            repository.addTeacher(teacherToSave)
            loadTeachers()
        }
    }

    fun deleteTeacher(id: String) {
        viewModelScope.launch {
            repository.deleteTeacher(id)
            loadTeachers()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeacherManagementScreen(
    navController: NavHostController,
    viewModel: AdminTeacherViewModel = hiltViewModel()
) {
    val teachers by viewModel.teachers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var editingTeacher by remember { mutableStateOf<Teacher?>(null) }
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
                        Text("Teacher Management", fontWeight = FontWeight.Bold)
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
                            selectedIds.forEach { viewModel.deleteTeacher(it) }
                            isSelectionMode = false
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = MaterialTheme.colorScheme.error)
                        }
                    } else {
                        IconButton(onClick = { viewModel.loadTeachers() }) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh")
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Teacher")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (teachers.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PersonOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No teachers found", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(teachers) { teacher ->
                    val isSelected = selectedIds.contains(teacher.teacherId)
                    TeacherCard(
                        teacher = teacher,
                        isSelected = isSelected,
                        onEdit = { editingTeacher = teacher },
                        onDelete = { viewModel.deleteTeacher(teacher.teacherId) },
                        onLongClick = {
                            isSelectionMode = true
                            selectedIds = selectedIds + teacher.teacherId
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (isSelected) {
                                    selectedIds - teacher.teacherId
                                } else {
                                    selectedIds + teacher.teacherId
                                }
                                if (selectedIds.isEmpty()) isSelectionMode = false
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            TeacherDialog(
                title = "Add New Teacher",
                onDismiss = { showAddDialog = false },
                onConfirm = { teacher ->
                    viewModel.addOrUpdateTeacher(teacher)
                    showAddDialog = false
                }
            )
        }

        editingTeacher?.let { teacher ->
            TeacherDialog(
                title = "Edit Teacher",
                teacher = teacher,
                onDismiss = { editingTeacher = null },
                onConfirm = { updated ->
                    viewModel.addOrUpdateTeacher(updated)
                    editingTeacher = null
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TeacherCard(
    teacher: Teacher, 
    isSelected: Boolean = false,
    onEdit: () -> Unit, 
    onDelete: () -> Unit,
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
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp, 
            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = androidx.compose.foundation.shape.CircleShape,
                color = if (teacher.status == "On Leave") MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        if (teacher.status == "On Leave") Icons.Default.EventBusy else Icons.Default.Person,
                        contentDescription = null,
                        tint = if (teacher.status == "On Leave") MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = teacher.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "ID: ${teacher.teacherId} • ${teacher.department}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                
                Surface(
                    modifier = Modifier.padding(top = 4.dp),
                    shape = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
                    color = (if (teacher.status == "Active") Color(0xFFE8F5E9) else Color(0xFFFFEBEE))
                ) {
                    Text(
                        text = teacher.status,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = (if (teacher.status == "Active") Color(0xFF2E7D32) else Color(0xFFC62828)),
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Row {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@Composable
fun TeacherDialog(
    title: String,
    teacher: Teacher? = null,
    onDismiss: () -> Unit,
    onConfirm: (Teacher) -> Unit
) {
    var id by remember { mutableStateOf(teacher?.teacherId ?: "") }
    var name by remember { mutableStateOf(teacher?.name ?: "") }
    var dob by remember { mutableStateOf(teacher?.dob ?: "") }
    var dept by remember { mutableStateOf(teacher?.department ?: "") }
    var email by remember { mutableStateOf(teacher?.email ?: "") }
    var status by remember { mutableStateOf(teacher?.status ?: "Active") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EduTrackTextField(
                    value = id, 
                    onValueChange = { id = it }, 
                    label = "Teacher ID", 
                    enabled = teacher == null,
                    placeholder = "e.g. TCH001"
                )
                EduTrackTextField(value = name, onValueChange = { name = it }, label = "Full Name", placeholder = "Enter name")
                EduTrackTextField(value = dept, onValueChange = { dept = it }, label = "Department", placeholder = "e.g. CSE")
                EduTrackTextField(value = email, onValueChange = { email = it }, label = "Email", placeholder = "e.g. name@domain.com")
                EduTrackTextField(value = dob, onValueChange = { dob = it }, label = "DOB", placeholder = "DD-MM-YYYY")
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Status: ", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = status == "Active",
                        onClick = { status = "Active" },
                        label = { Text("Active") }
                    )
                    Spacer(Modifier.width(8.dp))
                    FilterChip(
                        selected = status == "On Leave",
                        onClick = { status = "On Leave" },
                        label = { Text("On Leave") },
                        colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.errorContainer)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        teacher?.copy(
                            name = name,
                            department = dept,
                            email = email,
                            dob = dob,
                            status = status
                        ) ?: Teacher(
                            teacherId = id,
                            name = name,
                            department = dept,
                            email = email,
                            dob = dob,
                            status = status,
                            password = "123456"
                        )
                    )
                },
                enabled = id.isNotEmpty() && name.isNotEmpty()
            ) {
                Text(if (teacher == null) "Add" else "Update")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
