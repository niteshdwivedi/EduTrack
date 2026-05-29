package com.example.edutrack.ui.admin

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import com.example.edutrack.data.model.Section
import com.example.edutrack.data.model.User
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.ui.components.EduTrackTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStudentViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    private val _students = MutableStateFlow<List<User>>(emptyList())
    val students: StateFlow<List<User>> = _students

    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedSection = MutableStateFlow<String?>(null)
    val selectedSection: StateFlow<String?> = _selectedSection

    init {
        loadSections()
    }

    fun loadSections() {
        viewModelScope.launch {
            _isLoading.value = true
            _sections.value = repository.getAllSections()
            _isLoading.value = false
        }
    }

    fun setSection(section: String) {
        _selectedSection.value = if (section.isEmpty()) null else section
        if (section.isNotEmpty()) loadStudents()
    }

    fun loadStudents() {
        val sectionId = _selectedSection.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val all = repository.getAllStudents()
            _students.value = all.filter { it.section == sectionId }
            _isLoading.value = false
        }
    }

    fun addOrUpdateStudent(student: User) {
        viewModelScope.launch {
            repository.addStudent(student)
            loadStudents()
        }
    }

    fun deleteStudent(regNum: String) {
        viewModelScope.launch {
            repository.deleteStudent(regNum)
            loadStudents()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminStudentManagementScreen(
    navController: NavHostController,
    viewModel: AdminStudentViewModel = hiltViewModel()
) {
    val students by viewModel.students.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingStudent by remember { mutableStateOf<User?>(null) }

    if (selectedSection == null) {
        StudentSectionList(
            sections = sections,
            isLoading = isLoading,
            onSectionClick = { viewModel.setSection(it) },
            onBack = { navController.popBackStack() }
        )
    } else {
        StudentGridScreen(
            section = selectedSection!!,
            students = students,
            isLoading = isLoading,
            onBack = { viewModel.setSection("") },
            onAddClick = { showAddDialog = true },
            onEdit = { editingStudent = it },
            onDelete = { viewModel.deleteStudent(it) }
        )
    }

    if (showAddDialog) {
        StudentDialog(
            title = "Add New Student",
            section = selectedSection ?: "",
            onDismiss = { showAddDialog = false },
            onConfirm = { 
                viewModel.addOrUpdateStudent(it)
                showAddDialog = false
            }
        )
    }

    editingStudent?.let { student ->
        StudentDialog(
            title = "Edit Student",
            student = student,
            section = selectedSection ?: "",
            onDismiss = { editingStudent = null },
            onConfirm = { 
                viewModel.addOrUpdateStudent(it)
                editingStudent = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentSectionList(
    sections: List<Section>,
    isLoading: Boolean,
    onSectionClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Students: Select Section", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { section ->
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSectionClick(section.sectionId) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(40.dp), shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(section.sectionId.take(2), fontWeight = FontWeight.Bold)
                                }
                            }
                            Spacer(Modifier.width(16.dp))
                            Text(section.sectionId, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.KeyboardArrowRight, null)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentGridScreen(
    section: String,
    students: List<User>,
    isLoading: Boolean,
    onBack: () -> Unit,
    onAddClick: () -> Unit,
    onEdit: (User) -> Unit,
    onDelete: (String) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Section: $section", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, null)
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students) { student ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(student.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Text("Reg: ${student.registrationNumber}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                            }
                            Row {
                                IconButton(onClick = { onEdit(student) }) {
                                    Icon(Icons.Default.Edit, null, tint = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { onDelete(student.registrationNumber.toString()) }) {
                                    Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentDialog(
    title: String,
    section: String,
    student: User? = null,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var regNum by remember { mutableStateOf(student?.registrationNumber?.toString() ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var email by remember { mutableStateOf(student?.email ?: "") }
    var roll by remember { mutableStateOf(student?.rollNumber ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EduTrackTextField(value = regNum, onValueChange = { regNum = it }, label = "Registration Number", enabled = student == null)
                EduTrackTextField(value = name, onValueChange = { name = it }, label = "Full Name")
                EduTrackTextField(value = email, onValueChange = { email = it }, label = "Email")
                EduTrackTextField(value = roll, onValueChange = { roll = it }, label = "Roll Number")
            }
        },
        confirmButton = {
            Button(onClick = { 
                onConfirm(student?.copy(name = name, email = email, rollNumber = roll) ?: User(
                    registrationNumber = regNum,
                    name = name,
                    email = email,
                    rollNumber = roll,
                    section = section,
                    password = "123456"
                ))
            }) {
                Text(if (student == null) "Add" else "Update")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
