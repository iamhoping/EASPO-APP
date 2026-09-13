package com.example.mymy.ui.screens.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymy.R
import com.example.mymy.data.model.Enrollment
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.Section
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.model.Attendance
import com.example.mymy.ui.components.ProfileDashboard
import com.example.mymy.ui.theme.BackgroundColor
import com.example.mymy.ui.theme.DeepGreen
import com.example.mymy.ui.theme.LightGreen
import com.example.mymy.ui.theme.SageGreen
import com.example.mymy.ui.viewmodel.SchoolAdminViewModel
import com.example.mymy.data.model.Subject
import com.example.mymy.ui.components.*
import java.util.*

val ErrorColor = Color(0xFFD32F2F)
val LightText = Color(0xFF757575)


fun isValidEmail(email: String): Boolean {
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$".toRegex()
    return email.isNotBlank() && emailRegex.matches(email)
}

@Composable
fun SubjectList(
    subjects: List<com.example.mymy.data.model.Subject>,
    onSubjectClick: (com.example.mymy.data.model.Subject) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Subject Management", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${subjects.size} Configured Subjects", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            items(subjects) { subject ->
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSubjectClick(subject) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(subject.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepGreen)
                            Text("Code: ${subject.code ?: "N/A"} • Grade ${subject.gradeLevel ?: "All"}", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DeepGreen)
                    }
                }
            }

            if (subjects.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Book, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No subjects created yet.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSubjectDialog(
    subject: com.example.mymy.data.model.Subject,
    onDismiss: () -> Unit,
    onSave: (com.example.mymy.data.model.Subject) -> Unit,
    onDelete: (Int) -> Unit
) {
    var name by remember { mutableStateOf(subject.name) }
    var code by remember { mutableStateOf(subject.code ?: "") }
    var gradeLevel by remember { mutableStateOf(subject.gradeLevel ?: "7") }
    var units by remember { mutableStateOf(subject.units?.toString() ?: "3") }

    var gradeExpanded by remember { mutableStateOf(false) }
    val gradeLevels = (7..12).map { it.toString() }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (subject.id == null) "Create Subject" else "Edit Subject", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Subject Code") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                ExposedDropdownMenuBox(expanded = gradeExpanded, onExpandedChange = { gradeExpanded = it }) {
                    OutlinedTextField(
                        value = "Grade $gradeLevel",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    ExposedDropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                        gradeLevels.forEach { lvl ->
                            DropdownMenuItem(text = { Text("Grade $lvl") }, onClick = { gradeLevel = lvl; gradeExpanded = false; focusManager.moveFocus(FocusDirection.Down) })
                        }
                    }
                }

                OutlinedTextField(
                    value = units,
                    onValueChange = { if (it.all { char -> char.isDigit() }) units = it },
                    label = { Text("Units") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank() && code.isNotBlank()) {
                            onSave(subject.copy(name = name, code = code, gradeLevel = gradeLevel, units = units.toIntOrNull() ?: 3))
                        }
                    })
                )

                if (subject.id != null) {
                    TextButton(
                        onClick = { onDelete(subject.id!!) },
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Delete, "Delete")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Delete Subject")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    onSave(subject.copy(name = name, code = code, gradeLevel = gradeLevel, units = units.toIntOrNull() ?: 3))
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                enabled = name.isNotBlank() && code.isNotBlank()
            ) {
                Text("Save Subject")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SchoolAdminScreen(
    onLogout: () -> Unit,
    viewModel: SchoolAdminViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }
    val users = viewModel.allUsers
    val filteredUsers = viewModel.filteredUsers
    val schedules = viewModel.allSchedules
    val sections = viewModel.allSections
    val isLoading = viewModel.isLoading
    val focusManager = LocalFocusManager.current
    
    var selectedTab by remember { mutableIntStateOf(0) }
    var showUserDialog by remember { mutableStateOf(false) }
    var showScheduleDialog by remember { mutableStateOf(false) }
    var showSectionDialog by remember { mutableStateOf(false) }
    var showSubjectDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
    var editingSection by remember { mutableStateOf<Section?>(null) }
    var editingSubject by remember { mutableStateOf<com.example.mymy.data.model.Subject?>(null) }
    var showAttendanceLog by remember { mutableStateOf(false) }
    var userToDelete by remember { mutableStateOf<User?>(null) }
    var sectionToDelete by remember { mutableStateOf<Section?>(null) }
    var userToApprove by remember { mutableStateOf<User?>(null) }
    var userToReject by remember { mutableStateOf<User?>(null) }
    var viewingSection by remember { mutableStateOf<Section?>(null) }
    var studentToRemoveFromSection by remember { mutableStateOf<User?>(null) }
    var showLogoutConfirmation by remember { mutableStateOf(false) }

    // Replace banners with ElegantDialogs for high-impact feedback
    viewModel.errorMessage?.let { error ->
        ElegantDialog(
            onDismiss = { viewModel.errorMessage = null },
            title = "Attention",
            message = error,
            type = DialogType.ERROR,
            confirmButtonText = "Understood",
            dismissButtonText = null
        )
    }

    viewModel.successMessage?.let { success ->
        SuccessDialog(
            title = "Success",
            message = success,
            onConfirm = { viewModel.successMessage = null }
        )
    }

    if (showLogoutConfirmation) {
        ElegantDialog(
            onDismiss = { showLogoutConfirmation = false },
            title = "Logout Confirmation",
            message = "Are you sure you want to log out of your admin session?",
            type = DialogType.WARNING,
            icon = Icons.AutoMirrored.Filled.ExitToApp,
            confirmButtonText = "Logout",
            onConfirm = {
                showLogoutConfirmation = false
                onLogout()
            }
        )
    }

    Scaffold(
        bottomBar = {
            Surface(
                tonalElevation = 8.dp,
                shadowElevation = 16.dp,
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = DeepGreen
            ) {
                NavigationBar(
                    containerColor = Color.Transparent,
                    modifier = Modifier.height(80.dp)
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Home, "Home") },
                        label = { Text("Home") },
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SageGreen,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Groups, "Users") },
                        label = { Text("Users") },
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SageGreen,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Class, "Sections") },
                        label = { Text("Sections") },
                        selected = selectedTab == 3,
                        onClick = { selectedTab = 3 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SageGreen,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.DateRange, "Schedules") },
                        label = { Text("Schedules") },
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SageGreen,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Book, "Subjects") },
                        label = { Text("Subjects") },
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SageGreen,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.AccountCircle, "Profile") },
                        label = { Text("Profile") },
                        selected = selectedTab == 5,
                        onClick = { selectedTab = 5 },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            unselectedIconColor = Color.White.copy(alpha = 0.6f),
                            indicatorColor = SageGreen,
                            selectedTextColor = Color.White,
                            unselectedTextColor = Color.White.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab != 0 && selectedTab != 5 && selectedTab != 3) {
                FloatingActionButton(
                    onClick = {
                        when (selectedTab) {
                            1 -> showUserDialog = true
                            2 -> {
                                editingSchedule = Schedule(subject = "", day = "Monday", startTime = "08:00:00", endTime = "09:00:00", room = "")
                                showScheduleDialog = true
                            }
                            4 -> {
                                editingSubject = com.example.mymy.data.model.Subject(name = "", code = "", gradeLevel = "7")
                                showSubjectDialog = true
                            }
                        }
                    },
                    containerColor = DeepGreen,
                    contentColor = Color.White
                ) {
                    Icon(if (selectedTab == 1) Icons.Default.PersonAdd else Icons.Default.Add, null)
                }
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            if (viewModel.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepGreen)
                }
            } else {
                when (selectedTab) {
                    0 -> DashboardHome(
                        viewModel = viewModel,
                        onLogout = { showLogoutConfirmation = true },
                        onRegisterUser = {
                            selectedTab = 1
                            viewModel.roleFilter = null
                            viewModel.statusFilter = null
                            viewModel.searchQuery = ""
                            showUserDialog = true
                        },
                        onCreateSchedule = {
                            selectedTab = 2
                            editingSchedule = Schedule(subject = "", day = "Monday", startTime = "08:00:00", endTime = "09:00:00", room = "")
                            showScheduleDialog = true
                        },
                        onViewAttendance = { showAttendanceLog = true },
                        onViewPending = {
                            selectedTab = 1
                            viewModel.roleFilter = null
                            viewModel.statusFilter = "pending"
                            viewModel.searchQuery = ""
                        }
                    )
                    1 -> UserList(
                        users = filteredUsers,
                        searchQuery = viewModel.searchQuery,
                        onSearchChange = { viewModel.searchQuery = it },
                        selectedRole = viewModel.roleFilter,
                        onRoleChange = { viewModel.roleFilter = it },
                        selectedStatus = viewModel.statusFilter,
                        onStatusChange = { viewModel.statusFilter = it },
                        onConfirmDelete = { userToDelete = it },
                        onApprove = { userToApprove = it },
                        onReject = { userToReject = it }
                    )
                    2 -> AdminScheduleList(schedules, users, viewModel.allEnrollments, onEditSchedule = { 
                        editingSchedule = it
                        showScheduleDialog = true
                    })
                    3 -> {
                        if (viewingSection == null) {
                            SectionList(
                                sections = sections,
                                users = users,
                                onSectionClick = { viewingSection = it },
                                onDeleteSection = { sectionToDelete = it },
                                onCreateSection = {
                                    editingSection = Section(name = "", gradeLevel = "7")
                                    showSectionDialog = true
                                }
                            )
                        } else {
                            SectionDetailView(
                                section = viewingSection!!,
                                students = viewModel.allUsers.filter { it.sectionId == viewingSection?.id },
                                allUsers = viewModel.allUsers,
                                onBack = { viewingSection = null },
                                onRemoveStudent = { student ->
                                    studentToRemoveFromSection = student
                                },
                                onAddStudent = { student ->
                                    viewModel.addStudentToSection(student.id, viewingSection!!.id!!)
                                }
                            )
                        }
                    }
                    4 -> {
                        SubjectList(
                            subjects = viewModel.allSubjects,
                            onSubjectClick = {
                                editingSubject = it
                                showSubjectDialog = true
                            }
                        )
                    }
                    5 -> {
                        ProfileDashboard(
                            user = viewModel.userProfile,
                            onUpdate = { viewModel.updateProfile(it) },
                            onLogout = { showLogoutConfirmation = true }
                        )
                    }
                }
            }
        }


        if (showUserDialog) {
            RegisterUserDialog(
                students = users.filter { it.role == UserRole.STUDENT },
                sections = sections,
                onDismiss = { showUserDialog = false },
                onSave = { name, email, pass, role, sId, tId, childId, pId, gender, contact, address, guardian, grade, status, sectionId ->
                    viewModel.registerUser(name, email, pass, role, sId, tId, childId, pId, gender, contact, address, guardian, grade, status, sectionId)
                    showUserDialog = false
                }
            )
        }

        if (showScheduleDialog && editingSchedule != null) {
            ManageScheduleDialog(
                schedule = editingSchedule!!,
                students = users.filter { it.role == UserRole.STUDENT },
                teachers = users.filter { it.role == UserRole.TEACHER },
                sections = sections,
                subjects = viewModel.allSubjects,
                enrollments = viewModel.allEnrollments,
                onDismiss = { 
                    showScheduleDialog = false
                    editingSchedule = null
                },
                onSave = { updated, studentIds ->
                    viewModel.saveScheduleWithEnrollments(updated, studentIds)
                    showScheduleDialog = false
                    editingSchedule = null
                },
                onDelete = { schedule ->
                    schedule.id?.let { viewModel.deleteSchedule(it) }
                    showScheduleDialog = false
                    editingSchedule = null
                }
            )
        }

        if (showSectionDialog && editingSection != null) {
            ManageSectionDialog(
                section = editingSection!!,
                allStudents = users.filter { it.role == UserRole.STUDENT },
                onDismiss = {
                    showSectionDialog = false
                    editingSection = null
                },
                onSave = { name, grade, studentIds ->
                    if (editingSection?.id == null) {
                        viewModel.createSection(name, grade, studentIds)
                    } else {
                        val updatedSection = editingSection!!.copy(
                            name = name,
                            gradeLevel = grade
                        )
                        viewModel.updateSection(updatedSection, studentIds)
                        if (viewingSection?.id == updatedSection.id) {
                            viewingSection = updatedSection
                        }
                    }
                    showSectionDialog = false
                    editingSection = null
                },
                onDelete = { id ->
                    viewModel.deleteSection(id)
                    if (viewingSection?.id == id) viewingSection = null
                    showSectionDialog = false
                    editingSection = null
                }
            )
        }

        if (showSubjectDialog && editingSubject != null) {
            ManageSubjectDialog(
                subject = editingSubject!!,
                onDismiss = {
                    showSubjectDialog = false
                    editingSubject = null
                },
                onSave = { updated ->
                    if (updated.id == null) {
                        viewModel.createSubject(updated)
                    } else {
                        viewModel.updateSubject(updated)
                    }
                    showSubjectDialog = false
                    editingSubject = null
                },
                onDelete = { id ->
                    viewModel.deleteSubject(id)
                    showSubjectDialog = false
                    editingSubject = null
                }
            )
        }

        if (showAttendanceLog) {
            AttendanceLogDialog(
                attendanceList = viewModel.allAttendance,
                users = viewModel.allUsers,
                sections = viewModel.allSections,
                onDismiss = { showAttendanceLog = false }
            )
        }

        if (sectionToDelete != null) {
            DeleteConfirmationDialog(
                itemName = "Section: ${sectionToDelete?.name}",
                onConfirm = {
                    sectionToDelete?.id?.let { viewModel.deleteSection(it) }
                    sectionToDelete = null
                },
                onDismiss = { sectionToDelete = null }
            )
        }

        if (studentToRemoveFromSection != null) {
            ElegantDialog(
                onDismiss = { studentToRemoveFromSection = null },
                title = "Remove Student",
                message = "Are you sure you want to remove ${studentToRemoveFromSection!!.name} from ${viewingSection?.name}?",
                type = DialogType.WARNING,
                icon = Icons.Default.PersonRemove,
                confirmButtonText = "Remove",
                onConfirm = {
                    studentToRemoveFromSection?.let { student ->
                        viewModel.removeStudentFromSection(student.id)
                    }
                    studentToRemoveFromSection = null
                }
            )
        }

        if (userToDelete != null) {
            DeleteConfirmationDialog(
                itemName = "User: ${userToDelete?.name}",
                onConfirm = {
                    userToDelete?.id?.let { viewModel.deleteUser(it) }
                    userToDelete = null
                },
                onDismiss = { userToDelete = null }
            )
        }

        if (userToApprove != null) {
            ElegantDialog(
                onDismiss = { userToApprove = null },
                title = "Approve User",
                message = "Are you sure you want to approve ${userToApprove?.name}'s registration? They will be able to log in immediately.",
                type = DialogType.INFO,
                confirmButtonText = "Approve",
                onConfirm = {
                    userToApprove?.let { viewModel.approveUser(it) }
                    userToApprove = null
                }
            )
        }

        if (userToReject != null) {
            ElegantDialog(
                onDismiss = { userToReject = null },
                title = "Reject User",
                message = "Are you sure you want to reject ${userToReject?.name}'s registration? Their account will remain inactive.",
                type = DialogType.WARNING,
                confirmButtonText = "Reject",
                onConfirm = {
                    userToReject?.let { viewModel.rejectUser(it) }
                    userToReject = null
                }
            )
        }
    }
}

@Composable
fun DashboardHome(
    viewModel: SchoolAdminViewModel,
    onLogout: () -> Unit,
    onRegisterUser: () -> Unit,
    onCreateSchedule: () -> Unit,
    onViewAttendance: () -> Unit,
    onViewPending: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(24.dp).fillMaxSize(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    androidx.compose.foundation.Image(
                        painter = androidx.compose.ui.res.painterResource(id = R.drawable.logo),
                        contentDescription = "Logo",
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Column {
                        Text("Admin Dashboard", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("School Management System", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                IconButton(onClick = onLogout) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, "Logout", tint = Color.White)
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Statistics Section
            Text(
                text = "OVERVIEW",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DeepGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AdminStatCard(
                    title = "Students",
                    count = viewModel.allStudents.size.toString(),
                    icon = Icons.Default.Groups,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Teachers",
                    count = viewModel.allTeachers.size.toString(),
                    icon = Icons.Default.Person,
                    modifier = Modifier.weight(1f)
                )
                AdminStatCard(
                    title = "Parents",
                    count = viewModel.allParents.size.toString(),
                    icon = Icons.Default.FamilyRestroom,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Quick Actions Section
            Text(
                text = "QUICK ACTIONS",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DeepGreen,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            if (viewModel.pendingUsers.isNotEmpty()) {
                Surface(
                    onClick = onViewPending,
                    color = SageGreen.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(40.dp).background(SageGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.NotificationsActive, null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                        Column(modifier = Modifier.weight(1f).padding(horizontal = 16.dp)) {
                            Text("${viewModel.pendingUsers.size} Pending Registrations", fontWeight = FontWeight.Bold, color = DeepGreen)
                            Text("Review and approve new accounts", style = MaterialTheme.typography.bodySmall, color = DeepGreen.copy(alpha = 0.7f))
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = DeepGreen)
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                QuickActionButtonExtended(
                    label = "Register New User",
                    subtitle = "Add students, teachers, or parents",
                    icon = Icons.Default.PersonAdd,
                    onClick = onRegisterUser
                )
                QuickActionButtonExtended(
                    label = "Create Schedule",
                    subtitle = "Manage class hours and rooms",
                    icon = Icons.Default.AddBox,
                    onClick = onCreateSchedule
                )
                QuickActionButtonExtended(
                    label = "Attendance Log",
                    subtitle = "Review daily attendance records",
                    icon = Icons.AutoMirrored.Filled.Assignment,
                    onClick = onViewAttendance
                )
            }
        }
    }
}

@Composable
fun QuickActionButtonExtended(label: String, subtitle: String, icon: ImageVector, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(12.dp), color = LightGreen) {
                Icon(icon, null, tint = DeepGreen, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = LightText)
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DeepGreen, modifier = Modifier.size(20.dp))
        }
    }
}

@Composable
fun AdminStatCard(title: String, count: String, icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(10.dp),
                color = LightGreen.copy(alpha = 0.5f)
            ) {
                Icon(icon, null, tint = DeepGreen, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.ExtraBold, color = DeepGreen)
            Text(title, style = MaterialTheme.typography.bodySmall, color = LightText, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
fun UserList(
    users: List<User>,
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    selectedRole: UserRole?,
    onRoleChange: (UserRole?) -> Unit,
    selectedStatus: String?,
    onStatusChange: (String?) -> Unit,
    onConfirmDelete: (User) -> Unit,
    onApprove: (User) -> Unit,
    onReject: (User) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("User Management", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${users.size} Total Registered Users", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(modifier = Modifier.padding(horizontal = 24.dp).padding(top = 16.dp)) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name or email...") },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = DeepGreen) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onSearchChange("") }) {
                            Icon(Icons.Default.Clear, null)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { /* cleared via LocalFocusManager inside parent if needed or local */ }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = DeepGreen,
                    unfocusedBorderColor = Color(0xFFE0E0E0),
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Filter Chips
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val roles = listOf(null, UserRole.STUDENT, UserRole.TEACHER, UserRole.PARENT, UserRole.SCANNER)
                roles.forEach { role ->
                    FilterChip(
                        selected = selectedRole == role,
                        onClick = { onRoleChange(role) },
                        label = { 
                            Text(
                                when(role) {
                                    UserRole.STUDENT -> "Students"
                                    UserRole.TEACHER -> "Teachers"
                                    UserRole.PARENT -> "Parents"
                                    UserRole.SCANNER -> "Scanners"
                                    else -> "All"
                                },
                                maxLines = 1
                            ) 
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = DeepGreen,
                            selectedLabelColor = Color.White
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))


        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(users) { user ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(modifier = Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(24.dp), color = LightGreen) {
                            Box(contentAlignment = Alignment.Center) {
                                Text((user.name ?: "U").take(1).uppercase(), fontWeight = FontWeight.Bold, color = DeepGreen)
                            }
                        }
                        
                        Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                            Text(user.name ?: "Unknown", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            val roleText = when(user.role) {
                                UserRole.STUDENT -> "STUDENT • ${user.studentNo ?: "No ID"}${user.gradeLevel?.let { " • Grade $it" } ?: ""}"
                                UserRole.TEACHER -> "TEACHER • ${user.teacherId ?: "No ID"}"
                                UserRole.PARENT -> "PARENT • ${user.parentId ?: "No ID"}"
                                else -> user.role?.name ?: "Unknown"
                            }
                            Text("$roleText • ${user.gender ?: "Not Set"}", color = DeepGreen, style = MaterialTheme.typography.labelMedium)
                            Text(user.email ?: "", color = LightText, style = MaterialTheme.typography.labelSmall)
                        }

                        IconButton(onClick = { onConfirmDelete(user) }) {
                            Icon(Icons.Default.Delete, "Delete", tint = ErrorColor)
                        }
                    }
                    
                    if (user.status == "pending") {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(8.dp),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = { onReject(user) },
                                colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
                            ) {
                                Text("Reject")
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Button(
                                onClick = { onApprove(user) },
                                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Approve")
                            }
                        }
                    } else if (user.status == "rejected") {
                         Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                             Icon(Icons.Default.Info, null, tint = ErrorColor, modifier = Modifier.size(16.dp))
                             Spacer(modifier = Modifier.width(8.dp))
                             Text("Registration Rejected", color = ErrorColor, style = MaterialTheme.typography.labelSmall)
                             Spacer(modifier = Modifier.weight(1f))
                             TextButton(onClick = { onApprove(user) }) {
                                 Text("Re-approve")
                             }
                        }
                    }
                }
            }
            
            if (users.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.PeopleOutline, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No users found.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScheduleList(
    schedules: List<Schedule>,
    users: List<User>,
    enrollments: List<Enrollment>,
    onEditSchedule: (Schedule) -> Unit
) {
    val grouped = schedules.groupBy { "${it.subject} | ${it.day} | ${it.startTime}-${it.endTime}" }
    
    Column(modifier = Modifier.fillMaxSize()) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Master Schedule", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${grouped.size} Classes Configured", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            items(grouped.keys.toList()) { key ->
                val classSchedules = grouped[key] ?: emptyList()
                val first = classSchedules.first()
                val teacherName = users.find { it.id == first.teacherId || (it.teacherId != null && it.teacherId == first.teacherId) }?.name ?: "Unknown Teacher"
                val studentCount = enrollments.count { it.scheduleId == first.id }

                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onEditSchedule(first) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(first.subject ?: "No Subject", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepGreen)
                            Surface(color = LightGreen, shape = RoundedCornerShape(8.dp)) {
                                Text(first.day, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), style = MaterialTheme.typography.labelMedium, color = DeepGreen)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(16.dp), tint = LightText)
                            // Time Formatting: Military to 12h
                            val displayTime = remember(first.startTime, first.endTime) {
                                fun format(time: String?): String {
                                    if (time == null) return "--:--"
                                    try {
                                        val p = time.split(":")
                                        var h = p[0].toInt()
                                        val m = p[1].toInt()
                                        val ampm = if (h >= 12) "PM" else "AM"
                                        if (h > 12) h -= 12
                                        if (h == 0) h = 12
                                        return "%d:%02d %s".format(h, m, ampm)
                                    } catch (e: Exception) { return time }
                                }
                                "${format(first.startTime)} to ${format(first.endTime)}"
                            }
                            Text(displayTime, modifier = Modifier.padding(start = 8.dp), color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }

                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                            Icon(Icons.Default.Person, null, modifier = Modifier.size(16.dp), tint = LightText)
                            Text("Teacher: $teacherName", modifier = Modifier.padding(start = 8.dp), color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }

                        //Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(top = 4.dp)) {
                          //  Icon(Icons.Default.Group, null, modifier = Modifier.size(16.dp), tint = LightText)
                            //Text("$studentCount Students Enrolled", modifier = Modifier.padding(start = 8.dp), color = LightText, style = MaterialTheme.typography.bodyMedium)
                        //}
                    }
                }
            }
            
            if (grouped.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No schedules created yet.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SectionList(
    sections: List<Section>,
    users: List<User>,
    onSectionClick: (Section) -> Unit,
    onDeleteSection: (Section) -> Unit,
    onCreateSection: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            Surface(
                modifier = Modifier.fillMaxWidth().height(140.dp),
                color = DeepGreen,
                shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
            ) {
                Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                    Column {
                        Text("Section Management", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text("${sections.size} Active Sections", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 24.dp)
            ) {
                items(sections) { section ->
                    val studentCount = users.count { it.sectionId == section.id }
                    Card(
                        modifier = Modifier.fillMaxWidth().clickable { onSectionClick(section) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp).fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(section.name, fontWeight = FontWeight.Bold, fontSize = 18.sp, color = DeepGreen)
                                Text("Grade ${section.gradeLevel} • $studentCount Students", color = LightText, style = MaterialTheme.typography.bodyMedium)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { onDeleteSection(section) }) {
                                    Icon(Icons.Default.Delete, "Delete", tint = ErrorColor.copy(alpha = 0.7f))
                                }
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DeepGreen)
                            }
                        }
                    }
                }
                
                if (sections.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.Class, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No sections created yet.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = onCreateSection,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp),
            containerColor = DeepGreen,
            contentColor = Color.White
        ) {
            Icon(Icons.Default.Add, "Create Section")
        }
    }
}

@Composable
fun SectionDetailView(
    section: Section,
    students: List<User>,
    allUsers: List<User> = emptyList(),
    onBack: () -> Unit,
    onRemoveStudent: (User) -> Unit,
    onAddStudent: (User) -> Unit = {}
) {
    var showAddStudentDialog by remember { mutableStateOf(false) }

    if (showAddStudentDialog) {
        AddStudentToSectionDialog(
            section = section,
            allUsers = allUsers,
            onDismiss = { showAddStudentDialog = false },
            onAddStudent = {
                onAddStudent(it)
                showAddStudentDialog = false
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(160.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                IconButton(onClick = onBack, modifier = Modifier.align(Alignment.TopStart)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Column(modifier = Modifier.align(Alignment.CenterStart).padding(top = 40.dp)) {
                    Text("Grade ${section.gradeLevel}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodyMedium)
                    Text(section.name, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENROLLED STUDENTS (${students.size})",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
                
                TextButton(
                    onClick = { showAddStudentDialog = true },
                    colors = ButtonDefaults.textButtonColors(contentColor = DeepGreen)
                ) {
                    Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Add Student")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(students) { student ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color.White,
                        shadowElevation = 1.dp
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                modifier = Modifier.size(40.dp),
                                shape = RoundedCornerShape(20.dp),
                                color = LightGreen
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        (student.name ?: "S").take(1).uppercase(),
                                        fontWeight = FontWeight.Bold,
                                        color = DeepGreen
                                    )
                                }
                            }
                            
                            Column(modifier = Modifier.weight(1f).padding(start = 16.dp)) {
                                Text(
                                    student.name ?: "Unknown",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                                Text(
                                    "ID: ${student.studentNo ?: "N/A"}",
                                    color = LightText,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            
                            IconButton(onClick = { onRemoveStudent(student) }) {
                                Icon(Icons.Default.Delete, "Remove Student", tint = ErrorColor.copy(alpha = 0.7f))
                            }
                        }
                    }
                }

                if (students.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No students enrolled in this section.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentToSectionDialog(
    section: Section,
    allUsers: List<User>,
    onDismiss: () -> Unit,
    onAddStudent: (User) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Filter users who are students, match the grade level, and aren't in a section yet
    val availableStudents = remember(allUsers, section, searchQuery) {
        allUsers.filter { user ->
            user.role == UserRole.STUDENT &&
            user.gradeLevel == section.gradeLevel &&
            user.sectionId == null &&
            (searchQuery.isEmpty() || (user.name?.contains(searchQuery, ignoreCase = true) == true) || (user.studentNo?.contains(searchQuery) == true))
        }
    }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Student to ${section.name}") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().heightIn(max = 400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Students") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                    leadingIcon = { Icon(Icons.Default.Search, null) },
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                )
                
                if (availableStudents.isEmpty()) {
                    Box(Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No available students found for Grade ${section.gradeLevel}", color = LightText)
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableStudents) { student ->
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable { onAddStudent(student) },
                                colors = CardDefaults.cardColors(containerColor = Color.White),
                                border = androidx.compose.foundation.BorderStroke(1.dp, SageGreen.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(student.name ?: "Unknown", fontWeight = FontWeight.Bold)
                                        Text("ID: ${student.studentNo ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = LightText)
                                    }
                                    Icon(Icons.Default.Add, null, tint = DeepGreen)
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Close") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageSectionDialog(
    section: Section,
    allStudents: List<User>,
    onDismiss: () -> Unit,
    onSave: (String, String, List<String>) -> Unit,
    onDelete: (Long) -> Unit
) {
    var name by remember { mutableStateOf(section.name) }
    var gradeLevel by remember { mutableStateOf(section.gradeLevel) }

    val filteredStudents = remember(gradeLevel, allStudents) {
        allStudents.filter {
            val studentGrade = it.gradeLevel?.trim() ?: ""
            val targetGrade = gradeLevel.trim()
            studentGrade == targetGrade && 
            (it.sectionId == null || (section.id != null && it.sectionId == section.id))
        }
    }
    
    val initialSelectedIds = remember(section.id, allStudents) {
        if (section.id == null) {
            emptySet()
        } else {
            allStudents.filter { it.sectionId == section.id }.map { it.id }.toSet()
        }
    }
    var selectedStudentIds by remember { mutableStateOf(initialSelectedIds) }

    var gradeExpanded by remember { mutableStateOf(false) }
    val gradeLevels = (7..12).map { it.toString() }
    val focusManager = LocalFocusManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (section.id == null) "Create Section" else "Edit Section", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Section Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = {
                        if (name.isNotBlank()) {
                            onSave(name, gradeLevel, selectedStudentIds.toList())
                        }
                    })
                )

                ExposedDropdownMenuBox(expanded = gradeExpanded, onExpandedChange = { gradeExpanded = it }) {
                    OutlinedTextField(
                        value = "Grade $gradeLevel",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade Level") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        enabled = section.id == null,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    ExposedDropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                        gradeLevels.forEach { lvl ->
                            DropdownMenuItem(
                                text = { Text("Grade $lvl") },
                                onClick = { 
                                    gradeLevel = lvl
                                    gradeExpanded = false
                                    // Reset selected students that don't match the new grade level
                                    // and aren't already part of this section (if editing)
                                    selectedStudentIds = selectedStudentIds.filter { id ->
                                        val student = allStudents.find { it.id == id }
                                        student?.gradeLevel == lvl
                                    }.toSet()
                                }
                            )
                        }
                    }
                }

                if (true) { // Always show student management, even for new sections
                    Text("Manage Students (Grade $gradeLevel)", style = MaterialTheme.typography.labelMedium, color = DeepGreen, fontWeight = FontWeight.Bold)
                    Surface(
                        modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 200.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF8F8F8),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                    ) {
                        if (filteredStudents.isEmpty()) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Text(
                                    "No unassigned Grade $gradeLevel students found",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        } else {
                            LazyColumn(modifier = Modifier.padding(8.dp)) {
                                items(filteredStudents) { student ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth().clickable {
                                            selectedStudentIds = if (selectedStudentIds.contains(student.id)) selectedStudentIds - student.id else selectedStudentIds + student.id
                                        }.padding(vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Checkbox(
                                            checked = selectedStudentIds.contains(student.id),
                                            onCheckedChange = { checked ->
                                                selectedStudentIds = if (checked) selectedStudentIds + student.id else selectedStudentIds - student.id
                                            },
                                            colors = CheckboxDefaults.colors(checkedColor = DeepGreen)
                                        )
                                        Column(modifier = Modifier.padding(start = 8.dp)) {
                                            Text(student.name ?: "Unknown", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                            Text("ID: ${student.studentNo ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = LightText)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (section.id != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(
                            onClick = { onDelete(section.id!!) },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor),
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        ) {
                            Icon(Icons.Default.Delete, "Delete")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Delete Section")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, gradeLevel, selectedStudentIds.toList()) },
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                enabled = name.isNotBlank()
            ) {
                Text("Save Section")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AttendanceLogDialog(
    attendanceList: List<Attendance>,
    users: List<User>,
    sections: List<Section>,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Attendance Logs",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 500.dp)
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search by Student Name or Status") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true
                )
                
                val filteredAttendance = attendanceList.filter { log ->
                    val studentName = users.find { it.id == log.studentId }?.name ?: ""
                    studentName.contains(searchQuery, ignoreCase = true) ||
                            log.status.contains(searchQuery, ignoreCase = true)
                }.sortedByDescending { it.date }
                
                if (filteredAttendance.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No attendance logs found",
                            color = Color.Gray,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredAttendance) { log ->
                            val studentName = users.find { it.id == log.studentId }?.name ?: "Unknown Student"
                            val sectionName = sections.find { it.id == log.sectionId }?.name ?: "No Section"
                            
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F9F6))
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = studentName,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = DeepGreen
                                        )
                                        
                                        val statusColor = when (log.status.lowercase()) {
                                            "present" -> Color(0xFF43A047)
                                            "absent" -> Color(0xFFE53935)
                                            "late" -> Color(0xFFFFB300)
                                            else -> Color.Gray
                                        }
                                        
                                        Surface(
                                            color = statusColor.copy(alpha = 0.1f),
                                            shape = RoundedCornerShape(8.dp)
                                        ) {
                                            Text(
                                                text = log.status.replaceFirstChar { it.uppercase() },
                                                color = statusColor,
                                                style = MaterialTheme.typography.bodySmall,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                            )
                                        }
                                    }
                                    
                                    Spacer(modifier = Modifier.height(4.dp))
                                    
                                    Text(
                                        text = "Section: $sectionName",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    
                                    Spacer(modifier = Modifier.height(2.dp))
                                    
                                    Text(
                                        text = "Date: ${log.date}" + 
                                                (if (!log.timeIn.isNullOrBlank()) " | In: ${log.timeIn}" else "") +
                                                (if (!log.timeOut.isNullOrBlank()) " | Out: ${log.timeOut}" else ""),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.DarkGray
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterUserDialog(
    students: List<User>,
    sections: List<Section>,
    onDismiss: () -> Unit,
    onSave: (String, String, String, UserRole, String?, String?, String?, String?, String?, String?, String?, String?, String?, String, Long?) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var role by remember { mutableStateOf(UserRole.STUDENT) }
    var gender by remember { mutableStateOf("Male") }
    var contact by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var gradeLevel by remember { mutableStateOf("7") }
    var studentId by remember { mutableStateOf("") }
    var teacherId by remember { mutableStateOf("") }
    var parentId by remember { mutableStateOf("") }
    var childId by remember { mutableStateOf<String?>(null) }
    var guardianEmail by remember { mutableStateOf("") }
    var status by remember { mutableStateOf("active") }
    var selectedSectionId by remember { mutableStateOf<Long?>(null) }
    
    var roleExpanded by remember { mutableStateOf(false) }
    var genderExpanded by remember { mutableStateOf(false) }
    var gradeExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }

    val genders = listOf("Male", "Female")
    val gradeLevels = (7..12).map { it.toString() }
    val focusManager = LocalFocusManager.current

    val filteredSections = remember(gradeLevel, sections) {
        sections.filter { it.gradeLevel == gradeLevel }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Register New User", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = name, 
                    onValueChange = { name = it }, 
                    label = { Text("Full Name") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = email, 
                    onValueChange = { email = it }, 
                    label = { Text("Email Address") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp),
                    isError = email.isNotBlank() && !isValidEmail(email),
                    supportingText = {
                        if (email.isNotBlank() && !isValidEmail(email)) {
                            Text("Invalid email format", color = Color.Red)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = password, 
                    onValueChange = { password = it }, 
                    label = { Text("Password") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp),
                    isError = password.isNotBlank() && password.length < 6,
                    supportingText = {
                        if (password.isNotBlank() && password.length < 6) {
                            Text("Password must be at least 6 characters", color = Color.Red)
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = contact, 
                    onValueChange = { contact = it }, 
                    label = { Text("Contact Number") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                OutlinedTextField(
                    value = address, 
                    onValueChange = { address = it }, 
                    label = { Text("Address") }, 
                    modifier = Modifier.fillMaxWidth(), 
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )
                
                ExposedDropdownMenuBox(expanded = genderExpanded, onExpandedChange = { genderExpanded = it }) {
                    OutlinedTextField(
                        value = gender,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Gender") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genderExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    ExposedDropdownMenu(expanded = genderExpanded, onDismissRequest = { genderExpanded = false }) {
                        genders.forEach { g ->
                            DropdownMenuItem(text = { Text(g) }, onClick = { gender = g; genderExpanded = false; focusManager.moveFocus(FocusDirection.Down) })
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = roleExpanded, onExpandedChange = { roleExpanded = it }) {
                    OutlinedTextField(
                        value = role.name,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = roleExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    ExposedDropdownMenu(expanded = roleExpanded, onDismissRequest = { roleExpanded = false }) {
                        UserRole.entries.filter { it != UserRole.WEB_ADMIN }.forEach { r ->
                            DropdownMenuItem(text = { Text(r.name) }, onClick = { role = r; roleExpanded = false; focusManager.moveFocus(FocusDirection.Down) })
                        }
                    }
                }

                // Status selection for Admin


                if (role == UserRole.STUDENT) {
                    ExposedDropdownMenuBox(expanded = gradeExpanded, onExpandedChange = { gradeExpanded = it }) {
                        OutlinedTextField(
                            value = "Grade $gradeLevel",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Grade Level") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        ExposedDropdownMenu(expanded = gradeExpanded, onDismissRequest = { gradeExpanded = false }) {
                            gradeLevels.forEach { lvl ->
                                DropdownMenuItem(text = { Text("Grade $lvl") }, onClick = { 
                                    gradeLevel = lvl; gradeExpanded = false
                                    selectedSectionId = null // Reset section when grade changes
                                    focusManager.moveFocus(FocusDirection.Down) 
                                })
                            }
                        }
                    }

                    // Section Selection
                    ExposedDropdownMenuBox(expanded = sectionExpanded, onExpandedChange = { sectionExpanded = it }) {
                        OutlinedTextField(
                            value = sections.find { it.id == selectedSectionId }?.name ?: "No Section",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Section (Optional)") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                        )
                        ExposedDropdownMenu(expanded = sectionExpanded, onDismissRequest = { sectionExpanded = false }) {
                            DropdownMenuItem(text = { Text("None") }, onClick = { selectedSectionId = null; sectionExpanded = false; focusManager.moveFocus(FocusDirection.Down) })
                            filteredSections.forEach { section ->
                                DropdownMenuItem(text = { Text(section.name) }, onClick = { selectedSectionId = section.id; sectionExpanded = false; focusManager.moveFocus(FocusDirection.Down) })
                            }
                        }
                    }

                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("Student ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { 
                                studentId = "STU-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
                            }) {
                                Icon(Icons.Default.Refresh, "Generate")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    
                    OutlinedTextField(
                        value = guardianEmail,
                        onValueChange = { guardianEmail = it },
                        label = { Text("Guardian Email") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val isEmailValid = isValidEmail(email)
                            val isFormValid = name.isNotBlank() && 
                                            isEmailValid && 
                                            password.length >= 6 &&
                                            contact.isNotBlank() &&
                                            (role != UserRole.PARENT || parentId.isNotBlank()) &&
                                            (role != UserRole.STUDENT || studentId.isNotBlank()) &&
                                            (role != UserRole.TEACHER || teacherId.isNotBlank())
                            if (isFormValid) {
                                onSave(name, email, password, role, studentId, teacherId, childId, parentId, gender, contact, address, guardianEmail, gradeLevel, status, selectedSectionId)
                            }
                        })
                    )
                }
                if (role == UserRole.TEACHER) {
                    OutlinedTextField(
                        value = teacherId,
                        onValueChange = { teacherId = it },
                        label = { Text("Teacher ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { 
                                teacherId = "TCH-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
                            }) {
                                Icon(Icons.Default.Refresh, "Generate")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val isEmailValid = isValidEmail(email)
                            val isFormValid = name.isNotBlank() && 
                                            isEmailValid && 
                                            password.length >= 6 &&
                                            contact.isNotBlank() &&
                                            teacherId.isNotBlank()
                            if (isFormValid) {
                                onSave(name, email, password, role, studentId, teacherId, childId, parentId, gender, contact, address, guardianEmail, null, status, null)
                            }
                        })
                    )
                }
                if (role == UserRole.PARENT) {
                    OutlinedTextField(
                        value = parentId,
                        onValueChange = { parentId = it },
                        label = { Text("Parent ID") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        trailingIcon = {
                            IconButton(onClick = { 
                                parentId = "PRN-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
                            }) {
                                Icon(Icons.Default.Refresh, "Generate")
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            val isEmailValid = isValidEmail(email)
                            val isFormValid = name.isNotBlank() && 
                                            isEmailValid && 
                                            password.length >= 6 &&
                                            contact.isNotBlank() &&
                                            parentId.isNotBlank()
                            if (isFormValid) {
                                onSave(name, email, password, role, studentId, teacherId, childId, parentId, gender, contact, address, guardianEmail, null, status, null)
                            }
                        })
                    )
                }
            }
        },
        confirmButton = {
            val isEmailValid = isValidEmail(email)
            val isFormValid = name.isNotBlank() && 
                            isEmailValid && 
                            password.length >= 6 &&
                            contact.isNotBlank() &&
                            (role != UserRole.PARENT || parentId.isNotBlank()) &&
                            (role != UserRole.STUDENT || studentId.isNotBlank()) &&
                            (role != UserRole.TEACHER || teacherId.isNotBlank())

            Button(
                onClick = { 
                    onSave(name, email, password, role, studentId, teacherId, childId, parentId, gender, contact, address, guardianEmail, if (role == UserRole.STUDENT) gradeLevel else null, status, selectedSectionId)
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                enabled = isFormValid
            ) {
                Text("Register User")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManageScheduleDialog(
    schedule: Schedule,
    students: List<User>,
    teachers: List<User>,
    sections: List<Section>,
    subjects: List<Subject>,
    enrollments: List<Enrollment>,
    onDismiss: () -> Unit,
    onSave: (Schedule, List<String>) -> Unit,
    onDelete: (Schedule) -> Unit
) {
    var subject by remember { mutableStateOf(schedule.subject) }
    var day by remember { mutableStateOf(schedule.day.ifBlank { "Monday" }) }
    var room by remember { mutableStateOf(schedule.room) }
    
    // Initialize selected students from enrollments if it's an existing schedule
    val initialSelectedIds = remember(schedule.id, enrollments) {
        if (schedule.id != null) {
            enrollments.filter { it.scheduleId == schedule.id }.map { it.studentId }.toSet()
        } else {
            emptySet()
        }
    }
    var selectedStudentIds by remember { mutableStateOf(initialSelectedIds) }
    var selectedTeacherId by remember { mutableStateOf(schedule.teacherId) }
    var selectedSectionId by remember { mutableStateOf(schedule.sectionId) }

    var teacherExpanded by remember { mutableStateOf(false) }
    var sectionExpanded by remember { mutableStateOf(false) }
    var dayExpanded by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current

    val days = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday")
    val fromTimes = listOf(
        "7:00 AM", "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "1:00 PM", "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM"
    )
    val toTimes = listOf(
        "8:00 AM", "9:00 AM", "10:00 AM", "11:00 AM", "12:00 PM", "1:00 PM",
        "2:00 PM", "3:00 PM", "4:00 PM", "5:00 PM", "6:00 PM"
    )

    // Helper to format minutes to 12h time string
    fun minutesTo12h(totalMinutes: Int): String {
        var h = totalMinutes / 60
        val m = totalMinutes % 60
        val ampm = if (h >= 12) "PM" else "AM"
        if (h > 12) h -= 12
        if (h == 0) h = 12
        return "%d:%02d %s".format(h, m, ampm)
    }

    // Initial parsing of startTime/endTime "HH:mm:ss"
    val initialFromTime = remember(schedule.startTime) {
        try {
            val p = (schedule.startTime ?: "07:00:00").split(":")
            val h = p[0].toInt()
            val m = p[1].toInt()
            minutesTo12h(h * 60 + m)
        } catch (e: Exception) { "7:00 AM" }
    }
    val initialToTime = remember(schedule.endTime) {
        try {
            val p = (schedule.endTime ?: "10:00:00").split(":")
            val h = p[0].toInt()
            val m = p[1].toInt()
            minutesTo12h(h * 60 + m)
        } catch (e: Exception) { "10:00 AM" }
    }

    var fromTime by remember { mutableStateOf(initialFromTime) }
    var toTime by remember { mutableStateOf(initialToTime) }
    var fromExpanded by remember { mutableStateOf(false) }
    var toExpanded by remember { mutableStateOf(false) }

    // Helper to compare times for validation
    fun timeToMinutes(timeStr: String): Int {
        val parts = timeStr.split(" ")
        if (parts.size < 2) return 0
        val hm = parts[0].split(":")
        var h = hm[0].toInt()
        val m = if (hm.size > 1) hm[1].toInt() else 0
        val ampm = parts[1]
        if (ampm == "PM" && h != 12) h += 12
        if (ampm == "AM" && h == 12) h = 0
        return h * 60 + m
    }

    val isTimeValid = timeToMinutes(toTime) > timeToMinutes(fromTime)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Class Schedule", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                /*OutlinedTextField(
                    value = subject ?: "", 
                    onValueChange = { subject = it }, 
                    label = { Text("Subject Name") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )*/

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // Day Selector
                    ExposedDropdownMenuBox(
                        expanded = dayExpanded,
                        onExpandedChange = { dayExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = day,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Day") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dayExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = dayExpanded, onDismissRequest = { dayExpanded = false }) {
                            days.forEach { d ->
                                DropdownMenuItem(text = { Text(d) }, onClick = { day = d; dayExpanded = false })
                            }
                        }
                    }

                    // From Time Selector
                    ExposedDropdownMenuBox(
                        expanded = fromExpanded,
                        onExpandedChange = { fromExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "From: $fromTime",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Start") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = fromExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp)
                        )
                        ExposedDropdownMenu(expanded = fromExpanded, onDismissRequest = { fromExpanded = false }) {
                            fromTimes.forEach { t ->
                                DropdownMenuItem(text = { Text(t) }, onClick = { fromTime = t; fromExpanded = false })
                            }
                        }
                    }

                    // To Time Selector
                    ExposedDropdownMenuBox(
                        expanded = toExpanded,
                        onExpandedChange = { toExpanded = it },
                        modifier = Modifier.weight(1f)
                    ) {
                        OutlinedTextField(
                            value = "To: $toTime",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("End") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = toExpanded) },
                            modifier = Modifier.menuAnchor(),
                            shape = RoundedCornerShape(12.dp),
                            isError = !isTimeValid
                        )
                        ExposedDropdownMenu(expanded = toExpanded, onDismissRequest = { toExpanded = false }) {
                            val fromMin = timeToMinutes(fromTime)
                            toTimes.forEach { t ->
                                val toMin = timeToMinutes(t)
                                val isSelectable = toMin > fromMin
                                DropdownMenuItem(
                                    enabled = isSelectable,
                                    text = { 
                                        Text(
                                            text = t,
                                            color = if (!isSelectable) Color.Gray else Color.Unspecified
                                        ) 
                                    }, 
                                    onClick = { 
                                        toTime = t
                                        toExpanded = false 
                                    }
                                )
                            }
                        }
                    }
                }
                if (!isTimeValid) {
                    Text(
                        "End time must be after start time",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }

                OutlinedTextField(
                    value = room, 
                    onValueChange = { room = it }, 
                    label = { Text("Room / Location") }, 
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                )

                ExposedDropdownMenuBox(expanded = teacherExpanded, onExpandedChange = { teacherExpanded = it }) {
                    OutlinedTextField(
                        value = teachers.find { it.id == selectedTeacherId }?.name ?: "Assign Teacher",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Teacher") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = teacherExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    ExposedDropdownMenu(expanded = teacherExpanded, onDismissRequest = { teacherExpanded = false }) {
                        teachers.forEach { teacher ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(teacher.name ?: "Unknown")
                                        Text(teacher.email ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                }, 
                                onClick = { selectedTeacherId = teacher.id; teacherExpanded = false; focusManager.moveFocus(FocusDirection.Down) }
                            )
                        }
                    }
                }

                ExposedDropdownMenuBox(expanded = sectionExpanded, onExpandedChange = { sectionExpanded = it }) {
                    OutlinedTextField(
                        value = sections.find { it.id == selectedSectionId }?.name ?: "Assign Section (Optional)",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Section") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = sectionExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) })
                    )
                    ExposedDropdownMenu(expanded = sectionExpanded, onDismissRequest = { sectionExpanded = false }) {
                        DropdownMenuItem(text = { Text("None") }, onClick = { selectedSectionId = null; sectionExpanded = false; focusManager.moveFocus(FocusDirection.Down) })
                        sections.forEach { section ->
                            DropdownMenuItem(
                                text = { Text("${section.name} (Grade ${section.gradeLevel})") }, 
                                onClick = { 
                                    selectedSectionId = section.id
                                    // Auto-enroll section students
                                    val sectionStudents = students.filter { it.sectionId == section.id }.map { it.id }.toSet()
                                    selectedStudentIds = sectionStudents
                                    sectionExpanded = false 
                                    focusManager.moveFocus(FocusDirection.Down)
                                }
                            )
                        }
                    }
                }

                var subjectExpanded by remember { mutableStateOf(false) }
                val filteredSubjects = remember(selectedSectionId, subjects) {
                    val section = sections.find { it.id == selectedSectionId }
                    if (section != null) {
                        subjects.filter { it.gradeLevel == section.gradeLevel }
                    } else {
                        subjects
                    }
                }

                ExposedDropdownMenuBox(expanded = subjectExpanded, onExpandedChange = { subjectExpanded = it }) {
                    OutlinedTextField(
                        value = subject ?: "Select Subject",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Subject") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = subjectExpanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = {
                            if ((subject ?: "").isNotBlank() && selectedTeacherId != null && isTimeValid) {
                                // Trigger Save logic manually or let user click button
                                focusManager.clearFocus()
                            }
                        })
                    )
                    ExposedDropdownMenu(expanded = subjectExpanded, onDismissRequest = { subjectExpanded = false }) {
                        filteredSubjects.forEach { s: com.example.mymy.data.model.Subject ->
                            DropdownMenuItem(
                                text = { 
                                    Column {
                                        Text(s.name)
                                        Text(s.code ?: "", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                                    }
                                },
                                onClick = {
                                    subject = s.name
                                    subjectExpanded = false
                                    focusManager.clearFocus()
                                }
                            )
                        }
                    }
                }

               /* Text("Enrolled Students", style = MaterialTheme.typography.labelMedium, color = DeepGreen, fontWeight = FontWeight.Bold)
                Surface(
                    modifier = Modifier.fillMaxWidth().heightIn(max = 180.dp),
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFF8F8F8),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFEEEEEE))
                ) {
                    LazyColumn(modifier = Modifier.padding(8.dp)) {
                        items(students) { student ->
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    selectedStudentIds = if (selectedStudentIds.contains(student.id)) selectedStudentIds - student.id else selectedStudentIds + student.id
                                }.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = selectedStudentIds.contains(student.id), 
                                    onCheckedChange = null, 
                                    colors = CheckboxDefaults.colors(checkedColor = DeepGreen)
                                )
                                Column(modifier = Modifier.padding(start = 8.dp)) {
                                    Text(student.name ?: "Unknown", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                    Text("ID: ${student.studentNo ?: "N/A"}", style = MaterialTheme.typography.labelSmall, color = LightText)
                                }
                            }
                        }
                    }
                }*/

                if (schedule.id != null) {
                    TextButton(
                        onClick = { onDelete(schedule) }, 
                        colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor),
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    ) {
                        Icon(Icons.Default.Delete, "Delete")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Remove Schedule")
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    // Convert 7:00 AM to 07:00:00 for Database TIME type
                    fun formatToTime(timeStr: String): String {
                        val parts = timeStr.split(" ")
                        val hm = parts[0].split(":")
                        var h = hm[0].toInt()
                        val m = if (hm.size > 1) hm[1].toInt() else 0
                        val ampm = parts[1]
                        if (ampm == "PM" && h != 12) h += 12
                        if (ampm == "AM" && h == 12) h = 0
                        return String.format("%02d:%02d:00", h, m)
                    }

                    val base = schedule.copy(
                        subject = subject, 
                        day = day, 
                        room = room, 
                        teacherId = selectedTeacherId,
                        sectionId = selectedSectionId,
                        startTime = formatToTime(fromTime),
                        endTime = formatToTime(toTime)
                    )
                    onSave(base, selectedStudentIds.toList())
                }, 
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = (subject ?: "").isNotBlank() && selectedTeacherId != null && isTimeValid
            ) {
                Text("Confirm Schedule", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
