package com.example.mymy.ui.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymy.data.model.UserRole
import com.example.mymy.ui.viewmodel.SignUpViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val viewModel: SignUpViewModel = viewModel()

    if (viewModel.isSuccess) {
        LaunchedEffect(Unit) {
            onSignUpSuccess()
        }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Create Account") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = viewModel.name,
                onValueChange = { viewModel.name = it },
                label = { Text("Full Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.email,
                onValueChange = { viewModel.email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = viewModel.password,
                onValueChange = { viewModel.password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            if (viewModel.role == UserRole.STUDENT) {
                var expanded by remember { mutableStateOf(false) }
                val gradeLevels = listOf("Grade 7", "Grade 8", "Grade 9", "Grade 10", "Grade 11", "Grade 12")

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = if (viewModel.gradeLevel.isEmpty()) "" else viewModel.gradeLevel,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor(ExposedDropdownMenuAnchorType.PrimaryEditable, true).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        gradeLevels.forEach { level ->
                            DropdownMenuItem(
                                text = { Text(level) },
                                onClick = {
                                    viewModel.gradeLevel = level
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            Text("Select Role:", style = MaterialTheme.typography.titleSmall)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                UserRole.values().forEach { role ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = viewModel.role == role,
                            onClick = { viewModel.role = role }
                        )
                        Text(text = role.name.replace("_", " ").lowercase().capitalize(), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            viewModel.errorMessage?.let {
                Text(text = it, color = MaterialTheme.colorScheme.error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (viewModel.isLoading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = { viewModel.signUp() },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Register")
                }
                TextButton(onClick = onNavigateToLogin) {
                    Text("Already have an account? Login")
                }
            }
        }
    }
}
