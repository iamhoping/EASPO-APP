package com.example.mymy.ui.screens.teacher

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
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
import java.text.SimpleDateFormat
import java.util.*
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
                        label = { Text("Students") },
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
                    3 -> StudentManagement(viewModel, onAddGrade = { showGradeDialog = it }, currentSubject = null)
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
fun StudentManagement(viewModel: TeacherViewModel, onAddGrade: (User) -> Unit, currentSubject: String? = null) {
    var selectedSubject by remember { mutableStateOf<String?>(currentSubject) }
    var searchQuery by remember { mutableStateOf("") }
    
    val distinctSubjects = remember(viewModel.scheduleList) {
        viewModel.scheduleList.mapNotNull { it.subject }.distinct()
    }
    
    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Student Directory", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Manage student records and grades", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("QUICK FILTER", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
            Spacer(modifier = Modifier.height(16.dp))
            
            // Schedule Filter
            if (distinctSubjects.isNotEmpty()) {
                // Safely calculate index: "My Students" is index 0, subjects are 1..N
                val rawIndex = if (selectedSubject == null) 0 else distinctSubjects.indexOf(selectedSubject) + 1
                // Ensure index is within bounds of (1 + subjects size)
                val safeSelectedIndex = if (rawIndex < 0 || rawIndex > distinctSubjects.size) 0 else rawIndex
                
                ScrollableTabRow(
                    selectedTabIndex = safeSelectedIndex,
                    containerColor = Color.Transparent,
                    contentColor = DeepGreen,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    Tab(
                        selected = safeSelectedIndex == 0,
                        onClick = { selectedSubject = null },
                        text = { Text("My Students") }
                    )
                    distinctSubjects.forEachIndexed { index, subject ->
                        Tab(
                            selected = safeSelectedIndex == index + 1,
                            onClick = { selectedSubject = subject },
                            text = { Text(subject) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search student name or ID") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, "Clear Search")
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = OutlinedTextFieldDefaults.colors(unfocusedBorderColor = Color(0xFFF0F0F0))
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            val filteredStudents = remember(viewModel.students, selectedSubject, searchQuery, viewModel.scheduleList) {
                try {
                    var list = if (selectedSubject == null) {
                        viewModel.students
                    } else {
                        viewModel.students.filter { student ->
                           viewModel.scheduleList.any { it.subject == selectedSubject && (it.studentId == student.id || it.studentId == student.studentNo) }
                        }
                    }

                    if (searchQuery.isNotBlank()) {
                        list = list.filter { 
                            (it.name ?: "").contains(searchQuery, ignoreCase = true) || 
                            (it.studentNo?.contains(searchQuery, ignoreCase = true) == true)
                        }
                    }
                    list.sortedBy { it.name ?: "" }
                } catch (e: Exception) {
                    emptyList()
                }
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(filteredStudents) { student ->
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = Color.White,
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                    ) {
                        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(modifier = Modifier.size(48.dp), color = LightGreen, shape = RoundedCornerShape(12.dp)) {
                                Icon(Icons.Default.Person, null, tint = DeepGreen, modifier = Modifier.padding(12.dp))
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(student.name ?: "Unknown", fontWeight = FontWeight.Bold, color = DeepGreen)
                                Text("ID: ${student.studentNo ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = LightText)
                                Text("Guardian: ${student.guardianName ?: "Not Specified"}", style = MaterialTheme.typography.bodySmall, color = DeepGreen, fontWeight = FontWeight.Medium)
                            }
                            IconButton(onClick = { 
                                // Pass the student and current subject context if possible
                                onAddGrade(student) 
                            }) {
                                Icon(Icons.Default.EditNote, "Add Grade", tint = DeepGreen)
                            }
                        }
                    }
                }
                
                if (filteredStudents.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(Icons.Default.PeopleOutline, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("No students enrolled in this class", color = LightText, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun TeacherAttendanceScreen(viewModel: TeacherViewModel) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    
    val distinctSubjects = remember(viewModel.scheduleList) {
        viewModel.scheduleList.mapNotNull { it.subject }.distinct()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Mark Attendance", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("Date: $today", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text("SELECT CLASS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
            Spacer(modifier = Modifier.height(12.dp))

            if (distinctSubjects.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No classes scheduled", color = LightText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                if (selectedSubject == null) {
                    selectedSubject = distinctSubjects.first()
                }

                val selectedIndex = distinctSubjects.indexOf(selectedSubject).coerceAtLeast(0)

                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedIndex,
                    containerColor = Color.Transparent,
                    contentColor = DeepGreen,
                    edgePadding = 0.dp,
                    divider = {}
                ) {
                    distinctSubjects.forEach { subject ->
                        Tab(
                            selected = selectedSubject == subject,
                            onClick = { selectedSubject = subject },
                            text = { Text(subject) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                val enrolledStudents = remember(viewModel.students, selectedSubject, viewModel.scheduleList) {
                    try {
                        val scheduleForSubject = viewModel.scheduleList.filter { it.subject == selectedSubject }
                        val targetSectionIds = scheduleForSubject.mapNotNull { it.sectionId }.toSet()
                        val targetStudentIds = scheduleForSubject.mapNotNull { it.studentId }.toSet()

                        viewModel.students.filter { student ->
                            targetStudentIds.contains(student.id) || 
                            targetStudentIds.contains(student.studentNo) ||
                            (student.sectionId != null && targetSectionIds.contains(student.sectionId))
                        }.sortedBy { it.name }
                    } catch (e: Exception) {
                        emptyList()
                    }
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(enrolledStudents) { student ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                        ) {
                            Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(student.name ?: "Unknown", fontWeight = FontWeight.Bold, color = DeepGreen)
                                    Text("ID: ${student.studentNo ?: "N/A"}", style = MaterialTheme.typography.bodySmall, color = LightText)
                                }
                                
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    IconButton(
                                        onClick = { viewModel.markAttendance(student.id, "Present", selectedSubject ?: "General") },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = LightGreen)
                                    ) {
                                        Icon(Icons.Default.Check, "Present", tint = DeepGreen)
                                    }
                                    IconButton(
                                        onClick = { viewModel.markAttendance(student.id, "Absent", selectedSubject ?: "General") },
                                        colors = IconButtonDefaults.iconButtonColors(containerColor = Color(0xFFFFEBEE))
                                    ) {
                                        Icon(Icons.Default.Close, "Absent", tint = Color.Red)
                                    }
                                }
                            }
                        }
                    }
                    
                    if (enrolledStudents.isEmpty()) {
                        item {
                            Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Default.PeopleOutline, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("No students enrolled in this class", color = LightText, style = MaterialTheme.typography.bodyMedium)
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
