package com.example.edutrack.ui.screens.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.ChatRoom
import com.example.edutrack.data.model.User
import com.example.edutrack.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatListScreen(
    navController: NavHostController,
    viewModel: ChatViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf("") }
    val chatRooms by viewModel.chatRooms.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    var isSearching by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("EduChat", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { 
                    searchQuery = it
                    isSearching = it.isNotBlank()
                    viewModel.searchStudents(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("Search Students by Reg No or Name") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            if (isSearching) {
                LazyColumn {
                    items(searchResults) { user ->
                        StudentSearchCard(user) {
                            navController.navigate(Screen.ChatDetail.createRoute(user.id, user.name))
                        }
                    }
                }
            } else {
                LazyColumn {
                    items(chatRooms) { room ->
                        ChatRoomItem(room) {
                            navController.navigate(Screen.ChatDetail.createRoute(room.id, room.name))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentSearchCard(user: User, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(user.name, fontWeight = FontWeight.Bold)
                Text(user.rollNumber, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
                Text("${user.course} | Semester ${user.semester}", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun ChatRoomItem(room: ChatRoom, onClick: () -> Unit) {
    val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
    val time = sdf.format(Date(room.lastMessageTimestamp))
    
    val icon = when(room.roomType) {
        "Section" -> Icons.Default.Group
        "Teacher" -> Icons.Default.School
        else -> Icons.Default.Person
    }
    
    val color = when(room.roomType) {
        "Section" -> Color(0xFF2196F3)
        "Teacher" -> Color(0xFFE91E63)
        else -> MaterialTheme.colorScheme.primary
    }

    ListItem(
        modifier = Modifier.clickable(onClick = onClick),
        headlineContent = { Text(room.name, fontWeight = FontWeight.Bold) },
        supportingContent = { 
            Text(
                room.lastMessage, 
                maxLines = 1, 
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.outline
            ) 
        },
        leadingContent = {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
        },
        trailingContent = {
            Text(time, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
    )
    HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 0.5.dp)
}
