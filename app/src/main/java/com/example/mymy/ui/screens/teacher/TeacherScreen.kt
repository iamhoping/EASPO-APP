package com.example.mymy.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.activity.compose.rememberLauncherForActivityResult
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymy.data.model.User
import com.example.mymy.ui.components.CalendarScheduleView
import com.example.mymy.ui.components.ProfileDashboard
import com.example.mymy.ui.theme.*
import com.example.mymy.ui.viewmodel.TeacherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScreen(
    viewModel: TeacherViewModel = viewModel(),
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showGradeDialog by remember { mutableStateOf<User?>(null) }
    var showEditProfile by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(viewModel.successMessage) {
        viewModel.successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.successMessage = null
        }
    }

    LaunchedEffect(viewModel.errorMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.errorMessage = null
        }
    }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
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
                        icon = { Icon(Icons.Default.DateRange, "Schedule") },
                        label = { Text("Schedule") },
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
                        icon = { Icon(Icons.Default.CheckCircle, "Attendance") },
                        label = { Text("Attendance") },
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
                        icon = { Icon(Icons.Default.Groups, "Students") },
                        label = { Text("Sections") },
                        selected = selectedTab == 3,
                        onClick = { 
                            selectedTab = 3
                            // Reset any internal state if needed
                        },
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
                }
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(padding).fillMaxSize()) {
                if (viewModel.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = DeepGreen)
                }

                when (selectedTab) {
                    0 -> TeacherDashboardHome(
                        viewModel, 
                        onNavigateToSchedule = { selectedTab = 1 },
                        onNavigateToAttendance = { selectedTab = 2 },
                        onNavigateToStudents = { selectedTab = 3 }
                    )
                    1 -> TeacherSchedule(viewModel)
                    2 -> TeacherAttendanceScreen(viewModel)
                    3 -> StudentManagementFlow(viewModel)
                    4 -> TeacherProfile(viewModel, onLogout = onLogout)
                }
            }
        }

        if (showGradeDialog != null) {
            GradeEntryDialog(
                student = showGradeDialog!!,
                onDismiss = { showGradeDialog = null },
                onSave = { subject, score, remarks ->
                    viewModel.uploadGrade(showGradeDialog!!.id, subject, score, remarks)
                    showGradeDialog = null
                }
            )
        }
    }
}

@Composable
fun TeacherDashboardHome(
    viewModel: TeacherViewModel, 
    onNavigateToSchedule: () -> Unit,
    onNavigateToAttendance: () -> Unit,
    onNavigateToStudents: () -> Unit
) {
    val currentDay = remember { 
        SimpleDateFormat("EEEE", Locale.ENGLISH).format(Date()) 
    }
    
    val todaySchedules = remember(viewModel.scheduleList, currentDay) {
        viewModel.scheduleList
            .filter { it.day.equals(currentDay, ignoreCase = true) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.mymy.R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Teacher Home", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Welcome back, Prof. ${viewModel.userProfile?.name?.split(" ")?.lastOrNull() ?: "Educator"}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Surface(
                    modifier = Modifier.size(48.dp),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.2f)
                ) {
                    Icon(Icons.Default.AccountCircle, "Profile", tint = Color.White, modifier = Modifier.padding(4.dp))
                }
            }
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    TeacherStatsCard(
                        count = todaySchedules.size.toString(),
                        label = "Today's Total\nSchedules",
                        icon = Icons.Default.Group,
                        modifier = Modifier.weight(1f)
                    )
                    TeacherStatsCard(
                        count = viewModel.students.size.toString(),
                        label = "My\nStudents",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text("QUICK ACCESS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
            }

            item {
                QuickAccessItem(
                    title = "View Schedule",
                    subtitle = "Manage today's classes",
                    icon = Icons.Default.DateRange,
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                    onClick = onNavigateToSchedule
                )
            }
            item {
                QuickAccessItem(
                    title = "Attendance",
                    subtitle = "Mark daily attendance",
                    icon = Icons.Default.CheckCircle,
                    containerColor = Color.White,
                    contentColor = DeepGreen,
                    onClick = onNavigateToAttendance
                )
            }
            item {
                QuickAccessItem(
                    title = "Students & Grades",
                    subtitle = "Manage student records",
                    icon = Icons.Default.Groups,
                    containerColor = Color.White,
                    contentColor = DeepGreen,
                    onClick = onNavigateToStudents
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXT CLASS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                    Text("See All", style = MaterialTheme.typography.labelLarge, color = DeepGreen, modifier = Modifier.clickable { onNavigateToSchedule() })
                }
            }

            items(todaySchedules.take(1)) { schedule ->
                NextClassCard(schedule, viewModel.students.size)
            }
        }
    }
}

@Composable
fun TeacherStatsCard(count: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Surface(modifier = Modifier.size(32.dp), color = LightGreen, shape = RoundedCornerShape(8.dp)) {
                Icon(icon, null, tint = DeepGreen, modifier = Modifier.padding(6.dp))
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(count, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = DeepGreen)
            Text(label, style = MaterialTheme.typography.bodySmall, color = LightText, lineHeight = 14.sp)
        }
    }
}

@Composable
fun QuickAccessItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color, contentColor: Color, onClick: () -> Unit = {}) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(80.dp),
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = if (containerColor == Color.White) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0)) else null,
        shadowElevation = if (containerColor == Color.White) 0.dp else 8.dp
    ) {
        Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(modifier = Modifier.size(48.dp), shape = RoundedCornerShape(24.dp), color = contentColor.copy(alpha = 0.1f)) {
                Icon(icon, null, tint = contentColor, modifier = Modifier.padding(12.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = contentColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = contentColor.copy(alpha = 0.7f))
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = contentColor)
        }
    }
}

@Composable
fun NextClassCard(schedule: com.example.mymy.data.model.Schedule, studentCount: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.width(4.dp).height(40.dp).background(DeepGreen, RoundedCornerShape(2.dp)))
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Surface(color = LightGreen, shape = RoundedCornerShape(4.dp)) {
                    Text("SECTION B", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = DeepGreen, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(schedule.subject ?: "No Subject", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("Room ${schedule.room} • $studentCount Students", style = MaterialTheme.typography.bodySmall, color = LightText)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Schedule, null, modifier = Modifier.size(16.dp), tint = LightText)
                Spacer(modifier = Modifier.width(4.dp))
                // Time Formatting: Military to 12h
                val displayTime = remember(schedule.startTime, schedule.endTime) {
                    fun format(time: String?): String {
                        if (time == null) return "--:--"
                        return try {
                            val p = time.split(":")
                            var h = p[0].toInt()
                            val m = p[1].toInt()
                            val ampm = if (h >= 12) "PM" else "AM"
                            if (h > 12) h -= 12
                            if (h == 0) h = 12
                            "%d:%02d %s".format(h, m, ampm)
                        } catch (e: Exception) {
                            time
                        }
                    }
                    "${format(schedule.startTime)} to ${format(schedule.endTime)}"
                }
                Text(displayTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = DeepGreen)
            }
        }
    }
}

@Composable
fun TeacherSchedule(viewModel: TeacherViewModel) {
    CalendarScheduleView(
        schedules = viewModel.scheduleList,
        title = "Class Schedule",
        userImage = {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = RoundedCornerShape(24.dp),
                color = Color.White.copy(alpha = 0.2f)
            ) {
                Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(8.dp))
            }
        }
    )
}

@Composable
fun StudentManagementFlow(viewModel: TeacherViewModel) {
    var selectedSection by remember { mutableStateOf<com.example.mymy.data.model.Section?>(null) }
    
    if (selectedSection == null) {
        AssignedSectionsScreen(
            sections = viewModel.sections,
            students = viewModel.students,
            onSectionClick = { selectedSection = it }
        )
    } else {
        SectionStudentsScreen(
            section = selectedSection!!,
            viewModel = viewModel,
            onBack = { selectedSection = null }
        )
    }
}

@Composable
fun AssignedSectionsScreen(
    sections: List<com.example.mymy.data.model.Section>,
    students: List<User>,
    onSectionClick: (com.example.mymy.data.model.Section) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FBFB))) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("My Sections", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Select a section to manage students", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        if (sections.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.FolderOpen, null, modifier = Modifier.size(64.dp), tint = LightGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("No sections assigned yet", color = LightText)
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(sections) { section ->
                    val studentCount = remember(students, section.id) {
                        students.count { it.sectionId == section.id }
                    }
                    SectionCard(section = section, studentCount = studentCount, onClick = { onSectionClick(section) })
                }
            }
        }
    }
}

@Composable
fun SectionCard(section: com.example.mymy.data.model.Section, studentCount: Int, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(60.dp),
                color = LightGreen,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Class, null, tint = DeepGreen, modifier = Modifier.padding(16.dp))
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    "${section.gradeLevel} — ${section.name}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
                Text("S.Y. 2025-2026", style = MaterialTheme.typography.bodySmall, color = LightText)
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = DeepGreen)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Adviser: Teacher", style = MaterialTheme.typography.labelSmall, color = DeepGreen)
                }
            }
            
            Column(horizontalAlignment = Alignment.End) {
                Text(studentCount.toString(), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = DeepGreen)
                Text("Students", style = MaterialTheme.typography.labelSmall, color = LightText)
            }
        }
    }
}

@Composable
fun SectionStudentsScreen(
    section: com.example.mymy.data.model.Section,
    viewModel: TeacherViewModel,
    onBack: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val sectionStudents = remember(viewModel.students, section.id) {
        viewModel.students.filter { it.sectionId == section.id }
    }
    
    val filteredStudents = sectionStudents.filter {
        (it.name ?: "").contains(searchQuery, ignoreCase = true) || 
        (it.studentNo ?: "").contains(searchQuery, ignoreCase = true)
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(120.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Column {
                    Text(section.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("${section.gradeLevel} • ${sectionStudents.size} Students", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search student name or ID") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFF0F0F0))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Text("STUDENT LIST", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredStudents) { student ->
                    StudentGradeRow(
                        student = student,
                        subjects = viewModel.scheduleList.mapNotNull { it.subject }.distinct(),
                        onSaveGrade = { subj, score, remark ->
                            viewModel.uploadGrade(student.id, subj, score, remark)
                        }
                    )
                }
                
                if (filteredStudents.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Text("No students found", color = LightText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentGradeRow(
    student: User,
    subjects: List<String>,
    onSaveGrade: (String, Double, String) -> Unit
) {
    var score by remember { mutableStateOf("") }
    var selectedSubject by remember { mutableStateOf(subjects.firstOrNull() ?: "General") }
    var selectedRemark by remember { mutableStateOf("Passed") }
    var showMenu by remember { mutableStateOf(false) }
    var showSubjectMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(modifier = Modifier.size(40.dp), color = LightGreen, shape = RoundedCornerShape(10.dp)) {
                    Icon(Icons.Default.Person, null, tint = DeepGreen, modifier = Modifier.padding(8.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.name ?: "Unknown", fontWeight = FontWeight.Bold, color = DeepGreen)
                    Text("ID: ${student.studentNo ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = LightText)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                // Subject Dropdown
                Box(modifier = Modifier.weight(1.2f)) {
                    OutlinedButton(
                        onClick = { showSubjectMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedSubject, maxLines = 1, fontSize = 12.sp, color = DeepGreen)
                    }
                    DropdownMenu(expanded = showSubjectMenu, onDismissRequest = { showSubjectMenu = false }) {
                        subjects.forEach { subj ->
                            DropdownMenuItem(text = { Text(subj) }, onClick = { selectedSubject = subj; showSubjectMenu = false })
                        }
                    }
                }

                // Grade Input
                OutlinedTextField(
                    value = score,
                    onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) score = it },
                    modifier = Modifier.weight(0.8f),
                    placeholder = { Text("0-100", fontSize = 12.sp) },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(fontSize = 12.sp)
                )

                // Remarks Dropdown
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(horizontal = 8.dp),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(selectedRemark, fontSize = 12.sp, color = DeepGreen)
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        listOf("Passed", "Failed", "Incomplete").forEach { remark ->
                            DropdownMenuItem(text = { Text(remark) }, onClick = { selectedRemark = remark; showMenu = false })
                        }
                    }
                }

                    IconButton(
                        onClick = { 
                            val s = score.toDoubleOrNull()
                            if (s != null && s in 0.0..100.0) {
                                onSaveGrade(selectedSubject, s, selectedRemark)
                            }
                        },
                        modifier = Modifier.size(40.dp).background(DeepGreen, RoundedCornerShape(8.dp))
                    ) {
                        Icon(Icons.Default.Save, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    }
            }
        }
    }
}

@Composable
fun TeacherAttendanceScreen(viewModel: TeacherViewModel) {
    var selectedSection by remember { mutableStateOf<com.example.mymy.data.model.Section?>(null) }
    
    if (selectedSection == null) {
        SectionSelectionForAttendance(
            sections = viewModel.sections,
            onSectionClick = { selectedSection = it }
        )
    } else {
        SectionAttendanceDetailScreen(
            section = selectedSection!!,
            viewModel = viewModel,
            onBack = { selectedSection = null }
        )
    }
}

@Composable
fun SectionSelectionForAttendance(
    sections: List<com.example.mymy.data.model.Section>,
    onSectionClick: (com.example.mymy.data.model.Section) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FBFB))) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Attendance", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Select a section to view attendance", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(sections) { section ->
                Surface(
                    onClick = { onSectionClick(section) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(modifier = Modifier.size(50.dp), color = LightGreen, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Default.Groups, null, tint = DeepGreen, modifier = Modifier.padding(12.dp))
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column {
                            Text("${section.gradeLevel} - ${section.name}", fontWeight = FontWeight.Bold, color = DeepGreen)
                            Text("S.Y. 2025-2026", style = MaterialTheme.typography.bodySmall, color = LightText)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = DeepGreen)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SectionAttendanceDetailScreen(
    section: com.example.mymy.data.model.Section,
    viewModel: TeacherViewModel,
    onBack: () -> Unit
) {
    var selectedSchedule by remember { mutableStateOf<com.example.mymy.data.model.Schedule?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }
    
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = System.currentTimeMillis()
    )

    val schedulesForSection = remember(viewModel.scheduleList, section.id) {
        viewModel.scheduleList.filter { it.sectionId == section.id }
    }
    
    val sectionStudents = remember(viewModel.students, section.id) {
        viewModel.students.filter { it.sectionId == section.id }
    }

    LaunchedEffect(viewModel.selectedDate, section.id) {
        section.id?.let { viewModel.fetchAttendanceForSection(it, viewModel.selectedDate) }
    }

    val scanLauncher = rememberLauncherForActivityResult(
        contract = ScanContract(),
        onResult = { result ->
            result.contents?.let { studentNo ->
                val student = viewModel.students.find { it.studentNo == studentNo }
                if (student != null && selectedSchedule != null) {
                    viewModel.markAttendance(student.id, "Present", selectedSchedule!!)
                } else if (student == null) {
                    viewModel.errorMessage = "Student with ID $studentNo not found"
                } else {
                    viewModel.errorMessage = "Please select a schedule first"
                }
            }
        }
    )

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(16.dp).fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                    }
                    Column {
                        Text(section.name, color = Color.White, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Attendance History", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                    }
                }
                
                Row(modifier = Modifier.align(Alignment.CenterEnd), verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = {
                            val options = ScanOptions()
                            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                            options.setPrompt("Scan Student ID QR Code")
                            options.setBeepEnabled(true)
                            options.setOrientationLocked(false)
                            scanLauncher.launch(options)
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = Color.White.copy(alpha = 0.2f),
                            contentColor = Color.White
                        )
                    ) {
                        Icon(Icons.Default.QrCodeScanner, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan", style = MaterialTheme.typography.labelSmall)
                    }
                    
                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        onClick = { showDatePicker = true },
                        color = Color.White.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CalendarToday, null, tint = Color.White, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(viewModel.selectedDate, color = Color.White, style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }
        }

        if (showDatePicker) {
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            sdf.timeZone = TimeZone.getTimeZone("UTC")
                            viewModel.selectedDate = sdf.format(Date(millis))
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("SELECT SCHEDULE", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
            Spacer(modifier = Modifier.height(12.dp))

            if (schedulesForSection.isEmpty()) {
                Text("No schedules found for this section", color = LightText)
            } else {
                if (selectedSchedule == null) selectedSchedule = schedulesForSection.first()

                SecondaryScrollableTabRow(
                    selectedTabIndex = schedulesForSection.indexOf(selectedSchedule).coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    contentColor = DeepGreen,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    schedulesForSection.forEach { schedule ->
                        Tab(
                            selected = selectedSchedule == schedule,
                            onClick = { selectedSchedule = schedule },
                            text = { Text(schedule.subject ?: "No Subject") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("STUDENT STATUS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                
                // Summary
                val presentCount = viewModel.attendanceRecords.count { it.status == "Present" && it.scheduleId == selectedSchedule?.id }
                Text("$presentCount/${sectionStudents.size} Present", style = MaterialTheme.typography.labelMedium, color = SageGreen)
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(sectionStudents) { student ->
                    val attendance = viewModel.attendanceRecords.find { 
                        it.studentId == student.id && it.scheduleId == selectedSchedule?.id 
                    }
                    val status = attendance?.status ?: "Absent" // Default to Absent if no record
                    val timeIn = attendance?.timeIn

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(40.dp), color = LightGreen.copy(alpha = 0.3f), shape = RoundedCornerShape(10.dp)) {
                                Icon(Icons.Default.Person, null, tint = DeepGreen, modifier = Modifier.padding(8.dp))
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name ?: "Unknown", fontWeight = FontWeight.Bold, color = DeepGreen)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(12.dp), tint = LightText)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(timeIn ?: "--:--", style = MaterialTheme.typography.bodySmall, color = LightText)
                                }
                            }
                            
                            StatusBadge(status)

                            Spacer(modifier = Modifier.width(8.dp))
                            
                            // Manual Update Options
                            var showActions by remember { mutableStateOf(false) }
                            IconButton(onClick = { showActions = true }) {
                                Icon(Icons.Default.MoreVert, null, tint = LightText)
                            }

                            if (showActions) {
                                androidx.compose.ui.window.Dialog(onDismissRequest = { showActions = false }) {
                                    Surface(shape = RoundedCornerShape(16.dp), color = Color.White) {
                                        Column(modifier = Modifier.padding(16.dp)) {
                                            Text("Mark ${student.name} as:", fontWeight = FontWeight.Bold)
                                            Spacer(modifier = Modifier.height(8.dp))
                                            TextButton(onClick = { 
                                                selectedSchedule?.let { viewModel.markAttendance(student.id, "Present", it) }
                                                showActions = false 
                                            }) { Text("Present", color = SageGreen) }
                                            TextButton(onClick = { 
                                                selectedSchedule?.let { viewModel.markAttendance(student.id, "Absent", it) }
                                                showActions = false 
                                            }) { Text("Absent", color = Color.Red) }
                                        }
                                    }
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
fun StatusBadge(status: String) {
    val color = when(status) {
        "Present" -> SageGreen
        "Late" -> Color(0xFFFFB74D)
        else -> Color(0xFFEF5350)
    }
    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = status,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun TeacherProfile(viewModel: TeacherViewModel, onLogout: () -> Unit) {
    ProfileDashboard(
        user = viewModel.userProfile,
        onUpdate = { viewModel.updateProfile(it) },
        onLogout = onLogout
    )
}

@Composable
fun TeacherGradingScreen(viewModel: TeacherViewModel) {
    var selectedSubject by remember { mutableStateOf(viewModel.scheduleList.firstOrNull()?.subject ?: "All Subjects") }
    var expanded by remember { mutableStateOf(false) }
    
    val subjects = viewModel.scheduleList.mapNotNull { it.subject }.distinct()
    
    Column(modifier = Modifier.fillMaxSize().background(BackgroundColor).padding(24.dp)) {
        Text("GRADING SYSTEM", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Subject Selector
        Box {
            OutlinedButton(
                onClick = { expanded = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepGreen)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Subject: $selectedSubject")
                    Icon(Icons.Default.ArrowDropDown, null)
                }
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                subjects.forEach { subject ->
                    DropdownMenuItem(
                        text = { Text(subject) },
                        onClick = {
                            selectedSubject = subject
                            expanded = false
                        }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Student List for Grading
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            val enrolledStudents = viewModel.students.filter { student ->
                val scheduleForSubject = viewModel.scheduleList.filter { it.subject == selectedSubject }
                val targetSectionIds = scheduleForSubject.mapNotNull { it.sectionId }.toSet()
                val targetStudentIds = scheduleForSubject.mapNotNull { it.studentId }.toSet()
                
                targetStudentIds.contains(student.id) || 
                targetStudentIds.contains(student.studentNo) ||
                (student.sectionId != null && targetSectionIds.contains(student.sectionId))
            }
            
            items(enrolledStudents) { student ->
                val existingGrade = viewModel.studentGrades[student.id]?.find { it.subject == selectedSubject }
                var gradeInput by remember(student.id, selectedSubject) { mutableStateOf(existingGrade?.score?.toString() ?: "") }
                
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(student.name ?: "Unknown", fontWeight = FontWeight.Bold, color = DeepGreen)
                            Text("ID: ${student.studentNo ?: student.id.take(8)}", style = MaterialTheme.typography.bodySmall, color = LightText)
                        }
                        
                        OutlinedTextField(
                            value = gradeInput,
                            onValueChange = { gradeInput = it },
                            modifier = Modifier.width(80.dp),
                            placeholder = { Text("0.0") },
                            singleLine = true,
                            shape = RoundedCornerShape(8.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color(0xFFF0F0F0),
                                focusedBorderColor = SageGreen
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(8.dp))
                        
                        IconButton(
                            onClick = { 
                                val score = gradeInput.toDoubleOrNull() ?: 0.0
                                viewModel.uploadGrade(student.id, selectedSubject, score)
                            },
                            colors = IconButtonDefaults.iconButtonColors(containerColor = LightGreen)
                        ) {
                            Icon(Icons.Default.Check, "Save", tint = DeepGreen)
                        }
                    }
                }
            }
            
            if (enrolledStudents.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HistoryEdu, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No students found for this subject", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            } else {
                item {
                    Button(
                        onClick = {
                            // In a real bulk save, we'd collect all inputs
                            // For now, the per-row save is safer for Supabase
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DeepGreen)
                    ) {
                        Text("Save All Changes", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun GradeEntryDialog(student: User, onDismiss: () -> Unit, onSave: (String, Double, String) -> Unit) {
    var subject by remember { mutableStateOf("") }
    var score by remember { mutableStateOf("") }
    var remarks by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Record Grade", fontWeight = FontWeight.Bold, color = DeepGreen) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Student: ${student.name}", style = MaterialTheme.typography.bodyMedium, color = LightText)
                
                OutlinedTextField(
                    value = subject, 
                    onValueChange = { subject = it; isError = false }, 
                    label = { Text("Subject Name") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError && subject.isBlank(),
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = score, 
                    onValueChange = { score = it; isError = false }, 
                    label = { Text("Score (e.g. 85)") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = isError && score.toDoubleOrNull() == null,
                    shape = RoundedCornerShape(12.dp)
                )
                
                OutlinedTextField(
                    value = remarks, 
                    onValueChange = { remarks = it }, 
                    label = { Text("Remarks (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (isError) {
                    Text(
                        "Please enter a valid subject and numerical score.",
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { 
                    val numericScore = score.toDoubleOrNull()
                    if (subject.isNotBlank() && numericScore != null) {
                        onSave(subject, numericScore, remarks)
                    } else {
                        isError = true
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Save Grade")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = LightText) }
        }
    )
}

@Composable
fun EditTeacherProfileDialog(user: User, onDismiss: () -> Unit, onSave: (User) -> Unit) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var contact by remember { mutableStateOf(user.contact ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Profile") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, shape = RoundedCornerShape(12.dp))
                OutlinedTextField(value = contact, onValueChange = { contact = it }, label = { Text("Contact") }, shape = RoundedCornerShape(12.dp))
            }
        },
        confirmButton = {
            Button(onClick = { onSave(user.copy(name = name, contact = contact)) }, colors = ButtonDefaults.buttonColors(containerColor = SageGreen)) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
