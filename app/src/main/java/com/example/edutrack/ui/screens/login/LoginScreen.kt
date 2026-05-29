package com.example.edutrack.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import com.example.edutrack.ui.components.EduTrackButton
import com.example.edutrack.ui.components.EduTrackTextField
import com.example.edutrack.ui.navigation.Screen

import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign

enum class LoginRole { STUDENT, TEACHER, ADMIN }

@Composable
fun LoginScreen(
    navController: NavHostController,
    viewModel: AuthViewModel = hiltViewModel()
) {
    var regNum by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf(LoginRole.STUDENT) }
    var showRoleMenu by remember { mutableStateOf(false) }

    val isLoading by viewModel.isLoading.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()
    val error by viewModel.error.collectAsState()

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) {
            val route = when(selectedRole) {
                LoginRole.STUDENT -> Screen.Dashboard.route
                LoginRole.TEACHER -> Screen.TeacherDashboard.route
                LoginRole.ADMIN -> Screen.AdminDashboard.route
            }
            navController.navigate(route) {
                popUpTo(Screen.Login.route) { inclusive = true }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Three-dot menu at top-right
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
        ) {
            IconButton(onClick = { showRoleMenu = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Select Role")
            }
            DropdownMenu(
                expanded = showRoleMenu,
                onDismissRequest = { showRoleMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Student Login") },
                    onClick = { 
                        selectedRole = LoginRole.STUDENT
                        isOtpSent = false
                        showRoleMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Teacher Login") },
                    onClick = { 
                        selectedRole = LoginRole.TEACHER
                        isOtpSent = false
                        showRoleMenu = false 
                    }
                )
                DropdownMenuItem(
                    text = { Text("Admin Login") },
                    onClick = { 
                        selectedRole = LoginRole.ADMIN
                        isOtpSent = false
                        showRoleMenu = false 
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = when(selectedRole) {
                    LoginRole.ADMIN -> "Admin Portal"
                    LoginRole.TEACHER -> "Teacher Portal"
                    else -> "Welcome Back"
                },
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = when(selectedRole) {
                    LoginRole.ADMIN -> if (isOtpSent) "Enter the code sent to your device" else "Login via Email or Mobile OTP"
                    LoginRole.TEACHER -> "Login to your teacher dashboard"
                    else -> "Login to your EduTrack account"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.outline,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            EduTrackTextField(
                value = regNum,
                onValueChange = { regNum = it },
                label = when(selectedRole) {
                    LoginRole.TEACHER -> "Teacher ID"
                    LoginRole.ADMIN -> "Email or Mobile"
                    else -> "Registration Number"
                },
                leadingIcon = Icons.Default.Person,
                enabled = !isOtpSent
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            if (selectedRole == LoginRole.ADMIN) {
                if (isOtpSent) {
                    EduTrackTextField(
                        value = otpCode,
                        onValueChange = { otpCode = it },
                        label = "OTP Code",
                        leadingIcon = Icons.Default.Lock
                    )
                }
            } else {
                EduTrackTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Password",
                    leadingIcon = Icons.Default.Lock,
                    visualTransformation = PasswordVisualTransformation()
                )
            }

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
                text = if (selectedRole == LoginRole.ADMIN && !isOtpSent) "Send OTP" else "Login",
                onClick = { 
                    if (selectedRole == LoginRole.ADMIN) {
                        if (!isOtpSent) {
                            // Validation for Admin Credentials
                            if (regNum == "niteshdwivedi942@gmail.com" || regNum == "8707726234") {
                                isOtpSent = true
                                viewModel.clearError()
                            } else {
                                // Explicit error if not matching admin ID
                                // viewModel.setError("Unauthorized Admin Identity") 
                            }
                        } else {
                            // Mock OTP verify
                            if (otpCode == "123456") {
                                viewModel.login(regNum, "admin_otp_verified", "ADMIN")
                            }
                        }
                    } else {
                        viewModel.login(regNum, password, selectedRole.name)
                    }
                },
                isLoading = isLoading
            )

            if (selectedRole == LoginRole.STUDENT) {
                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { navController.navigate(Screen.ForgotPassword.route) }) {
                    Text("Forgot Password?")
                }

                Spacer(modifier = Modifier.height(8.dp))

                TextButton(onClick = { navController.navigate(Screen.Register.route) }) {
                    Text("Don't have an account? Register Now")
                }
            }
            
            if (selectedRole == LoginRole.ADMIN && isOtpSent) {
                TextButton(onClick = { isOtpSent = false; otpCode = "" }) {
                    Text("Change Number/Email")
                }
            }
        }
    }
}