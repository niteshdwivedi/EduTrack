package com.example.edutrack.ui.screens.makeup_class

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.data.model.MakeupClass

/**
 * Screen to display scheduled makeup classes.
 * Uses MakeupClassViewModel to provide data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MakeupClassScreen(
    navController: NavHostController,
    viewModel: MakeupClassViewModel = hiltViewModel()
) {
    val makeupClasses by viewModel.makeupClasses.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Makeup Class System") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
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
                MakeupClassCard(item)
            }
        }
    }
}

@Composable
fun MakeupClassCard(makeupClass: MakeupClass) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = makeupClass.subject, style = MaterialTheme.typography.titleLarge)
            Text(
                text = "Teacher: ${makeupClass.teacher}",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(text = "Date: ${makeupClass.date}", style = MaterialTheme.typography.bodyMedium)
                Text(text = "Time: ${makeupClass.time}", style = MaterialTheme.typography.bodyMedium)
            }
            Text(text = "Venue: ${makeupClass.venue}", style = MaterialTheme.typography.bodySmall)
        }
    }
}
