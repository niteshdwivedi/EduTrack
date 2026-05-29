package com.example.edutrack.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.ui.components.EduTrackButton
import com.example.edutrack.ui.components.EduTrackTextField
import com.example.edutrack.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var regNum by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    val isLoading by viewModel.isLoading.collectAsState()
    val dobVerified by viewModel.dobVerified.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(dobVerified) {
        if (dobVerified) {
            navController.navigate(Screen.ResetPassword.createRoute(regNum))
            viewModel.clearFlags()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Forgot Password") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Verify Details",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Enter registration number and DOB to reset password",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            EduTrackTextField(
                value = regNum,
                onValueChange = { regNum = it },
                label = "Registration Number",
                leadingIcon = Icons.Default.Person
            )

            Spacer(modifier = Modifier.height(16.dp))

            EduTrackTextField(
                value = dob,
                onValueChange = { dob = it },
                label = "DOB (DD/MM/YYYY)",
                leadingIcon = Icons.Default.CalendarToday
            )

            if (error != null) {
                Text(
                    text = error!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            EduTrackButton(
                text = "Verify & Continue",
                onClick = { viewModel.verifyDob(regNum, dob) },
                isLoading = isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = { navController.popBackStack() }) {
                Text("Go Back")
            }
        }
    }
}
