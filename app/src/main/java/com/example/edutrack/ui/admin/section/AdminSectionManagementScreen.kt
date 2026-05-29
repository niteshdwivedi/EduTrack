package com.example.edutrack.ui.admin.section

import androidx.compose.foundation.ExperimentalFoundationApi
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
import com.example.edutrack.data.model.Section
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.ui.components.EduTrackTextField
import com.example.edutrack.ui.navigation.Screen
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSectionViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init { loadSections() }

    fun loadSections() {
        viewModelScope.launch {
            _isLoading.value = true
            _sections.value = repository.getAllSections()
            _isLoading.value = false
        }
    }

    fun addSection(id: String, spec: String, sem: Int) {
        viewModelScope.launch {
            repository.addSection(Section(sectionId = id, specialization = spec, semester = sem))
            loadSections()
        }
    }

    fun deleteSections(ids: List<String>) {
        viewModelScope.launch {
            ids.forEach { repository.deleteSection(it) }
            loadSections()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSectionManagementScreen(
    navController: NavHostController,
    viewModel: AdminSectionViewModel = hiltViewModel()
) {
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var selectedIds by remember { mutableStateOf(setOf<String>()) }
    var isSelectionMode by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelectionMode) "${selectedIds.size} Selected" else "Sections") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (isSelectionMode) {
                            isSelectionMode = false
                            selectedIds = emptySet()
                        } else navController.popBackStack() 
                    }) {
                        Icon(if (isSelectionMode) Icons.Default.Close else Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        IconButton(onClick = { selectedIds = sections.map { it.sectionId }.toSet() }) {
                            Icon(Icons.Default.SelectAll, contentDescription = "Select All")
                        }
                        IconButton(onClick = { 
                            viewModel.deleteSections(selectedIds.toList())
                            isSelectionMode = false
                            selectedIds = emptySet()
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Selected", tint = Color.Red)
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            if (!isSelectionMode) {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.Add, contentDescription = "Add Section")
                }
            }
        }
    ) { padding ->
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { section ->
                    SectionItemCard(
                        section = section,
                        isSelected = selectedIds.contains(section.sectionId),
                        onLongClick = {
                            isSelectionMode = true
                            selectedIds = selectedIds + section.sectionId
                        },
                        onClick = {
                            if (isSelectionMode) {
                                selectedIds = if (selectedIds.contains(section.sectionId)) {
                                    selectedIds - section.sectionId
                                } else {
                                    selectedIds + section.sectionId
                                }
                                if (selectedIds.isEmpty()) isSelectionMode = false
                            } else {
                                // Navigate to timetable with this section
                                // Screen.AdminTimetableManagement.createRoute(section.sectionId)
                                navController.navigate(Screen.AdminTimetableManagement.route) 
                                // Note: Need to update AdminTimetableViewModel to use this passed section
                            }
                        }
                    )
                }
            }
        }

        if (showAddDialog) {
            AddSectionDialog(
                onDismiss = { showAddDialog = false },
                onAdd = { id, spec, sem ->
                    viewModel.addSection(id, spec, sem)
                    showAddDialog = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SectionItemCard(section: Section, isSelected: Boolean, onLongClick: () -> Unit, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().combinedClickable(onLongClick = onLongClick, onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
        )
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = section.sectionId, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text(text = "Specialization: ${section.specialization}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Semester: ${section.semester}", style = MaterialTheme.typography.labelSmall)
            }
            if (isSelected) {
                Icon(Icons.Default.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            } else {
                Icon(Icons.Default.ChevronRight, contentDescription = null)
            }
        }
    }
}

@Composable
fun AddSectionDialog(onDismiss: () -> Unit, onAdd: (String, String, Int) -> Unit) {
    var id by remember { mutableStateOf("") }
    var spec by remember { mutableStateOf("") }
    var sem by remember { mutableStateOf("1") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Section") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                EduTrackTextField(value = id, onValueChange = { id = it }, label = "Section ID (e.g. K23BD)")
                EduTrackTextField(value = spec, onValueChange = { spec = it }, label = "Specialization")
                EduTrackTextField(value = sem, onValueChange = { sem = it }, label = "Semester")
            }
        },
        confirmButton = {
            Button(onClick = { onAdd(id, spec, sem.toIntOrNull() ?: 1) }) { Text("Create") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
