package com.example.edutrack.ui.admin.timetable

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.Section
import com.example.edutrack.data.model.TimetableEntry
import com.example.edutrack.data.repository.FirestoreRepository
import com.example.edutrack.ui.components.EduTrackTextField
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminTimetableViewModel @Inject constructor(
    private val repository: FirestoreRepository
) : ViewModel() {
    private val _timetable = MutableStateFlow<List<TimetableEntry>>(emptyList())
    val timetable: StateFlow<List<TimetableEntry>> = _timetable

    private val _sections = MutableStateFlow<List<Section>>(emptyList())
    val sections: StateFlow<List<Section>> = _sections

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _selectedSection = MutableStateFlow<String?>(null)
    val selectedSection: StateFlow<String?> = _selectedSection

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

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
        _selectedSection.value = section
        loadTimetable()
    }

    fun loadTimetable() {
        val sectionId = _selectedSection.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            val all = repository.getTimetable("all")
            // Show entries for this section OR entries that have no section assigned yet (Global)
            _timetable.value = all.filter { it.section == sectionId || it.section.isEmpty() }
            _isLoading.value = false
        }
    }

    fun saveEntry(entry: TimetableEntry) {
        viewModelScope.launch {
            val sectionId = _selectedSection.value ?: return@launch
            val entryToSave = entry.copy(section = sectionId)
            
            // Conflict Detection Logic
            val existing = repository.getTimetable("all")
            val conflict = existing.find {
                it.classId != entryToSave.classId && it.day == entryToSave.day && it.startTime == entryToSave.startTime && (
                    it.faculty == entryToSave.faculty || it.room == entryToSave.room || it.section == entryToSave.section
                )
            }

            if (conflict != null) {
                _error.value = when {
                    conflict.faculty == entryToSave.faculty -> "Conflict: Teacher ${entryToSave.faculty} is busy!"
                    conflict.room == entryToSave.room -> "Conflict: Room ${entryToSave.room} is occupied!"
                    else -> "Conflict: Section ${entryToSave.section} has another class!"
                }
                return@launch
            }

            repository.addTimetableEntry(entryToSave)
            loadTimetable()
            _error.value = null
        }
    }

    fun deleteEntry(id: String) {
        viewModelScope.launch {
            repository.deleteTimetableEntry(id)
            loadTimetable()
        }
    }

    fun clearTimetable() {
        val sectionId = _selectedSection.value ?: return
        viewModelScope.launch {
            _isLoading.value = true
            repository.deleteTimetableForSection(sectionId)
            loadTimetable()
            _isLoading.value = false
        }
    }

    fun clearError() { _error.value = null }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTimetableManagementScreen(
    navController: NavHostController,
    viewModel: AdminTimetableViewModel = hiltViewModel()
) {
    val timetable by viewModel.timetable.collectAsState()
    val sections by viewModel.sections.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedSection by viewModel.selectedSection.collectAsState()
    val error by viewModel.error.collectAsState()

    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (selectedSection == null) {
        SectionListScreen(
            sections = sections,
            isLoading = isLoading,
            onSectionClick = { viewModel.setSection(it) },
            onBack = { navController.popBackStack() }
        )
    } else {
        TimetableGridScreen(
            section = selectedSection!!,
            timetable = timetable,
            error = error,
            onBack = { viewModel.setSection("") }, // Clear selection to go back to list
            onEditEntry = { viewModel.saveEntry(it) },
            onDeleteEntry = { viewModel.deleteEntry(it) },
            onClearAll = { showDeleteConfirm = true },
            onClearError = { viewModel.clearError() }
        )
    }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Clear Timetable?") },
            text = { Text("This will permanently delete ALL entries for Section $selectedSection. Are you sure?") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearTimetable()
                        showDeleteConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                ) { Text("Clear All") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel") } }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionListScreen(
    sections: List<Section>,
    isLoading: Boolean,
    onSectionClick: (String) -> Unit,
    onBack: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Select Section", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        if (isLoading) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (sections.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Info, null, modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.outline)
                    Text("No sections found in database", color = MaterialTheme.colorScheme.outline)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(sections) { section ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSectionClick(section.sectionId) },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(50.dp),
                                shape = RoundedCornerShape(8.dp),
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        section.sectionId.take(2),
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            
                            Spacer(Modifier.width(16.dp))
                            
                            Column(Modifier.weight(1f)) {
                                Text(
                                    section.sectionId,
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    "${section.specialization} • Sem ${section.semester}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                            
                            Icon(
                                Icons.Default.KeyboardArrowRight,
                                null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TimetableGridScreen(
    section: String,
    timetable: List<TimetableEntry>,
    error: String?,
    onBack: () -> Unit,
    onEditEntry: (TimetableEntry) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onClearAll: () -> Unit,
    onClearError: () -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 7 })
    val scope = rememberCoroutineScope()
    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
    val timeSlots = (8..17).map { hour ->
        val h = if (hour > 12) hour - 12 else hour
        val ampm = if (hour >= 12) "PM" else "AM"
        val start = String.format("%02d:00 %s", h, ampm)
        val nextH = if (hour + 1 > 12) hour + 1 - 12 else hour + 1
        val nextAmpm = if (hour + 1 >= 12) "PM" else "AM"
        val end = String.format("%02d:00 %s", nextH, nextAmpm)
        start to end
    }

    var editingEntry by remember { mutableStateOf<TimetableEntry?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Timetable: $section", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(onClick = onClearAll, colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)) {
                        Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Clear All", fontSize = 12.sp)
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            ScrollableTabRow(
                selectedTabIndex = pagerState.currentPage,
                edgePadding = 16.dp,
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                days.forEachIndexed { index, day ->
                    Tab(
                        selected = pagerState.currentPage == index,
                        onClick = { 
                            scope.launch { pagerState.animateScrollToPage(index) } 
                        },
                        text = { Text(day) }
                    )
                }
            }

            if (error != null) {
                Snackbar(
                    modifier = Modifier.padding(8.dp),
                    action = { TextButton(onClick = onClearError) { Text("Dismiss", color = Color.White) } }
                ) { Text(error) }
            }

            HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
                val currentDay = days[page]
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(timeSlots) { slot ->
                        val entry = timetable.find { it.day == currentDay && it.startTime == slot.first }
                        HourlySlotCard(slot.first, slot.second, entry, onEdit = {
                            editingEntry = entry ?: TimetableEntry(
                                day = currentDay,
                                startTime = slot.first,
                                endTime = slot.second,
                                section = section
                            )
                        }, onDelete = {
                            entry?.let { onDeleteEntry(it.classId) }
                        })
                    }
                }
            }
        }

        editingEntry?.let { entry ->
            EditTimetableDialog(
                entry = entry,
                onDismiss = { editingEntry = null },
                onSave = { updated ->
                    onEditEntry(updated)
                    editingEntry = null
                }
            )
        }
    }
}

@Composable
fun HourlySlotCard(start: String, end: String, entry: TimetableEntry?, onEdit: () -> Unit, onDelete: () -> Unit) {
    val isAbsent = entry?.isTeacherAbsent == true
    Card(
        modifier = Modifier.fillMaxWidth().height(if (isAbsent) 95.dp else 80.dp).clickable { onEdit() },
        colors = CardDefaults.cardColors(
            containerColor = when {
                isAbsent -> MaterialTheme.colorScheme.errorContainer
                entry != null -> MaterialTheme.colorScheme.primaryContainer
                else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            }
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.width(80.dp)) {
                Text(start, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(end, fontSize = 10.sp, color = MaterialTheme.colorScheme.outline)
            }
            
            VerticalDivider(modifier = Modifier.padding(horizontal = 8.dp).fillMaxHeight(), color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            
            if (entry != null) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(entry.subjectName, fontWeight = FontWeight.ExtraBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text("${entry.faculty} • Rm ${entry.room}", style = MaterialTheme.typography.bodySmall)
                    
                    if (isAbsent) {
                        Surface(
                            modifier = Modifier.padding(top = 4.dp),
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.error
                        ) {
                            Text(
                                "TEACHER ON LEAVE",
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                color = Color.White,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                    }
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, null, tint = if (isAbsent) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.error, modifier = Modifier.size(20.dp))
                }
            } else {
                Text("Empty Slot", modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.bodyMedium)
                Icon(Icons.Default.Add, null, tint = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

@Composable
fun EditTimetableDialog(entry: TimetableEntry, onDismiss: () -> Unit, onSave: (TimetableEntry) -> Unit) {
    var subject by remember { mutableStateOf(entry.subjectName) }
    var code by remember { mutableStateOf(entry.subjectCode) }
    var teacher by remember { mutableStateOf(entry.faculty) }
    var room by remember { mutableStateOf(entry.room) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                if (entry.subjectName.isEmpty()) "Add Class: ${entry.startTime}" else "Edit Slot: ${entry.startTime}",
                fontWeight = FontWeight.Bold
            ) 
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                EduTrackTextField(
                    value = subject, 
                    onValueChange = { subject = it }, 
                    label = "Subject Name",
                    placeholder = "e.g. Mobile Application Development"
                )
                EduTrackTextField(
                    value = code, 
                    onValueChange = { code = it }, 
                    label = "Subject Code",
                    placeholder = "e.g. CSE-441"
                )
                EduTrackTextField(
                    value = teacher, 
                    onValueChange = { teacher = it }, 
                    label = "Teacher Name",
                    placeholder = "e.g. Dr. Rajesh Kumar"
                )
                EduTrackTextField(
                    value = room, 
                    onValueChange = { room = it }, 
                    label = "Room",
                    placeholder = "e.g. 38 510 (38: Bldg, 5: Floor, 10: Room)"
                )
                
                if (room.isNotEmpty()) {
                    Text(
                        text = "Format: ${parseRoomInfo(room)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(entry.copy(subjectName = subject, subjectCode = code, faculty = teacher, room = room)) },
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Entry")
            }
        },
        dismissButton = { 
            TextButton(onClick = onDismiss) { Text("Cancel") } 
        }
    )
}

fun parseRoomInfo(room: String): String {
    val digits = room.filter { it.isDigit() }
    return if (digits.length >= 5) {
        val bldg = digits.substring(0, 2)
        val floor = digits.substring(2, 3)
        val rm = digits.substring(3)
        "Bldg: $bldg, Floor: $floor, Room: $rm"
    } else {
        "Enter at least 5 digits (e.g. 38510)"
    }
}

@Composable
fun SectionDialog(current: String, onDismiss: () -> Unit, onConfirm: (String) -> Unit) {
    var text by remember { mutableStateOf(current) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Manage Section") },
        text = {
            EduTrackTextField(value = text, onValueChange = { text = it }, label = "Enter Section Name (e.g. K23BD)")
        },
        confirmButton = {
            Button(onClick = { onConfirm(text) }) { Text("Go") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}
