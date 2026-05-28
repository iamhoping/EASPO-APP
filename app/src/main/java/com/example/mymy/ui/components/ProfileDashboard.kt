package com.example.mymy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.ui.theme.DeepGreen
import com.example.mymy.ui.theme.LightGreen
import com.example.mymy.ui.theme.LightText
import com.example.mymy.ui.theme.SageGreen

import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import com.example.mymy.util.BarcodeUtils

@Composable
fun ProfileDashboard(
    user: User?,
    onUpdate: (User) -> Unit,
    onLogout: () -> Unit
) {
    if (user == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = DeepGreen)
        }
        return
    }

    val context = LocalContext.current
    var isEditing by remember { mutableStateOf(false) }
    var showQRCodeDialog by remember { mutableStateOf(false) }
    
    // QR Code generation based on student ID
    val qrBitmap = remember(user.studentNo) {
        user.studentNo?.let { BarcodeUtils.generateQRCode(it) }
    }
    
    // Editable States
    var email by remember(user) { mutableStateOf(user.email) }
    var contact by remember(user) { mutableStateOf(user.contact ?: "") }
    var address by remember(user) { mutableStateOf(user.address ?: "") }
    var gender by remember(user) { mutableStateOf(user.gender ?: "") }
    var guardianEmail by remember(user) { mutableStateOf(user.guardianEmail ?: "") }

    // Error States
    var emailError by remember { mutableStateOf<String?>(null) }
    var contactError by remember { mutableStateOf<String?>(null) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FA))
            .verticalScroll(scrollState)
    ) {
        // Header Section
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 200.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Column(
                modifier = Modifier.padding(vertical = 32.dp, horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    modifier = Modifier.size(80.dp),
                    shape = RoundedCornerShape(40.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(
                        Icons.Default.Person,
                        contentDescription = null,
                        modifier = Modifier.padding(16.dp).size(48.dp),
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = user.name ?: "Unknown",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = user.role?.name ?: "No Role",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.7f)
                )

                if ((user.role == UserRole.STUDENT || user.role?.name?.equals("STUDENT", ignoreCase = true) == true) && !user.studentNo.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    TextButton(
                        onClick = { showQRCodeDialog = true },
                        colors = ButtonDefaults.textButtonColors(contentColor = Color.White)
                    ) {
                        Icon(Icons.Default.QrCode, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("View ID Barcode", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Edit Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "PERSONAL INFORMATION",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
                TextButton(
                    onClick = {
                        if (isEditing) {
                            // Validate and Save
                            val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
                            val isContactValid = contact.isEmpty() || contact.all { it.isDigit() } && contact.length >= 10

                            if (!isEmailValid) {
                                emailError = "Invalid email format"
                                return@TextButton
                            }
                            if (!isContactValid) {
                                contactError = "Invalid contact number"
                                return@TextButton
                            }

                            emailError = null
                            contactError = null
                            
                            onUpdate(user.copy(
                                email = email,
                                contact = contact,
                                address = address,
                                gender = gender,
                                guardianEmail = if (user.role == UserRole.STUDENT) guardianEmail else user.guardianEmail
                            ))
                            isEditing = false
                        } else {
                            isEditing = true
                        }
                    }
                ) {
                    Icon(
                        if (isEditing) Icons.Default.Check else Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(if (isEditing) "Save Changes" else "Edit Profile")
                }
            }

            // Read-Only Section (Academic/Identity)
            ProfileCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val idLabel = if (user.role == UserRole.STUDENT) "Student No" else "Employee ID"
                    val idValue = if (user.role == UserRole.STUDENT) user.studentNo else user.teacherId
                    
                    ReadOnlyField(label = idLabel, value = idValue ?: "N/A", icon = Icons.Default.Badge)
                    
                    if (user.role == UserRole.STUDENT || user.gradeLevel != null) {
                        ReadOnlyField(label = "Grade Level", value = user.gradeLevel ?: "N/A", icon = Icons.Default.School)
                    }
                    
                    ReadOnlyField(label = "Full Name", value = user.name ?: "Unknown", icon = Icons.Default.Person)
                }
            }

            // Editable Section
            ProfileCard {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    EditableField(
                        label = "Email Address",
                        value = email ?: "",
                        onValueChange = { email = it },
                        isEditing = isEditing,
                        icon = Icons.Default.Email,
                        error = emailError
                    )
                    
                    EditableField(
                        label = "Contact Number",
                        value = contact,
                        onValueChange = { contact = it },
                        isEditing = isEditing,
                        icon = Icons.Default.Phone,
                        error = contactError
                    )

                    EditableField(
                        label = "Home Address",
                        value = address,
                        onValueChange = { address = it },
                        isEditing = isEditing,
                        icon = Icons.Default.Home
                    )

                    GenderDropdownField(
                        gender = gender,
                        onGenderChange = { gender = it },
                        isEditing = isEditing
                    )

                    if (user.role == UserRole.STUDENT) {
                        EditableField(
                            label = "Guardian Email",
                            value = guardianEmail,
                            onValueChange = { guardianEmail = it },
                            isEditing = isEditing,
                            icon = Icons.Default.Email
                        )
                    }
                }
            }

            if (isEditing) {
                OutlinedButton(
                    onClick = {
                        // Reset states and cancel
                        email = user.email
                        contact = user.contact ?: ""
                        address = user.address ?: ""
                        gender = user.gender ?: ""
                        guardianEmail = user.guardianEmail ?: ""
                        emailError = null
                        contactError = null
                        isEditing = false
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Gray)
                ) {
                    Text("Cancel")
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Logout Button
            Button(
                onClick = onLogout,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFEBEE))
            ) {
                Icon(Icons.Default.Logout, contentDescription = null, tint = Color.Red)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Logout", color = Color.Red, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    if (showQRCodeDialog && qrBitmap != null) {
        AlertDialog(
            onDismissRequest = { showQRCodeDialog = false },
            title = { Text("Attendance QR Code", color = DeepGreen, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Student ID: ${user.studentNo}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(16.dp))
                    androidx.compose.foundation.Image(
                        bitmap = qrBitmap.asImageBitmap(),
                        contentDescription = "Student QR Code",
                        modifier = Modifier.size(250.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Present this code for attendance check",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        BarcodeUtils.saveBitmapToGallery(context, qrBitmap, "Student_QR_${user.studentNo}")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                ) {
                    Icon(Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Download PNG")
                }
            },
            dismissButton = {
                TextButton(onClick = { showQRCodeDialog = false }) {
                    Text("Close")
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenderDropdownField(
    gender: String,
    onGenderChange: (String) -> Unit,
    isEditing: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val genders = listOf("Male", "Female")

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp).offset(y = if (isEditing) 8.dp else 0.dp),
            color = if (isEditing) LightGreen.copy(alpha = 0.3f) else Color(0xFFF0F4F8),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                Icons.Default.Wc,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = if (isEditing) DeepGreen else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = it }
                ) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = DeepGreen,
                            focusedLabelColor = DeepGreen
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        genders.forEach { g ->
                            DropdownMenuItem(
                                text = { Text(g) },
                                onClick = {
                                    onGenderChange(g)
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            } else {
                Text("Gender", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    text = if (gender.isEmpty()) "Not provided" else gender,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (gender.isEmpty()) Color.LightGray else Color.Black
                )
            }
        }
    }
}

@Composable
fun ProfileCard(content: @Composable () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        shadowElevation = 2.dp
    ) {
        Box(modifier = Modifier.padding(20.dp)) {
            content()
        }
    }
}

@Composable
fun ReadOnlyField(label: String, value: String, icon: ImageVector) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier.size(40.dp),
            color = Color(0xFFF0F4F8),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
            Text(value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium, color = Color.DarkGray)
        }
    }
}

@Composable
fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    isEditing: Boolean,
    icon: ImageVector,
    error: String? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Surface(
            modifier = Modifier.size(40.dp).offset(y = if(isEditing) 8.dp else 0.dp),
            color = if (isEditing) LightGreen.copy(alpha = 0.3f) else Color(0xFFF0F4F8),
            shape = RoundedCornerShape(10.dp)
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(10.dp),
                tint = if (isEditing) DeepGreen else Color.Gray
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            if (isEditing) {
                OutlinedTextField(
                    value = value,
                    onValueChange = onValueChange,
                    label = { Text(label) },
                    modifier = Modifier.fillMaxWidth(),
                    isError = error != null,
                    supportingText = error?.let { { Text(it) } },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = DeepGreen,
                        focusedLabelColor = DeepGreen
                    ),
                    singleLine = true
                )
            } else {
                Text(label, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                Text(
                    text = if (value.isEmpty()) "Not provided" else value,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (value.isEmpty()) Color.LightGray else Color.Black
                )
            }
        }
    }
}
