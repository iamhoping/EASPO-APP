package com.example.mymy.ui.screens.student

import androidx.compose.foundation.background
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymy.data.model.User
import com.example.mymy.ui.components.CalendarScheduleView
import com.example.mymy.ui.components.ProfileDashboard
import com.example.mymy.ui.theme.*
import com.example.mymy.ui.viewmodel.StudentViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentScreen(
    viewModel: StudentViewModel = viewModel(),
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showEditProfile by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
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
                        icon = { Icon(Icons.Filled.List, "Grades") },
                        label = { Text("Grades") },
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
                        icon = { Icon(Icons.Default.CheckCircle, "Attendance") },
                        label = { Text("Attendance") },
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
                    0 -> DashboardContent(viewModel, onTabChange = { selectedTab = it })
                    1 -> ScheduleList(viewModel)
                    2 -> GradesList(viewModel)
                    3 -> AttendanceList(viewModel)
                    4 -> ProfileContent(viewModel, onLogout = onLogout)
                }
            }
        }
    }
}

@Composable
fun DashboardContent(viewModel: StudentViewModel, onTabChange: (Int) -> Unit) {
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
                            Text("Student Home", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Welcome back, ${viewModel.userProfile?.name?.split(" ")?.firstOrNull() ?: "Student"}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
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
                    val today = remember { 
                        Calendar.getInstance().getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.ENGLISH).uppercase() 
                    }
                    val todayClasses = viewModel.scheduleList.filter { it.day.uppercase() == today }.size
                    
                    StudentStatsCard(
                        count = todayClasses.toString(),
                        label = "Today's Classes",
                        icon = Icons.Default.DateRange,
                        modifier = Modifier.weight(1f)
                    )
                    StudentStatsCard(
                        count = viewModel.overallAttendance,
                        label = "Overall\nAttendance",
                        icon = Icons.Default.ShowChart,
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
                    subtitle = "Check today's classes",
                    icon = Icons.Default.DateRange,
                    containerColor = DeepGreen,
                    contentColor = Color.White,
                    onClick = { onTabChange(1) }
                )
            }
            item {
                QuickAccessItem(
                    title = "Grades",
                    subtitle = "Recent evaluations",
                    icon = Icons.Default.MilitaryTech,
                    containerColor = Color.White,
                    contentColor = DeepGreen,
                    onClick = { onTabChange(2) }
                )
            }
            item {
                QuickAccessItem(
                    title = "Attendance",
                    subtitle = "View your record",
                    icon = Icons.Default.ContactPage,
                    containerColor = Color.White,
                    contentColor = DeepGreen,
                    onClick = { onTabChange(3) }
                )
            }

            item {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("NEXT UP", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                    Text("See All", style = MaterialTheme.typography.labelLarge, color = DeepGreen)
                }
            }

            items(viewModel.scheduleList.take(1)) { schedule ->
                val teacher = viewModel.teachersList.find { it.id == schedule.teacherId }
                NextUpCard(schedule, teacher?.name ?: "No Teacher")
            }
        }
    }
}

@Composable
fun StudentStatsCard(count: String, label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier) {
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
fun QuickAccessItem(title: String, subtitle: String, icon: androidx.compose.ui.graphics.vector.ImageVector, containerColor: Color, contentColor: Color, onClick: () -> Unit) {
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
fun NextUpCard(schedule: com.example.mymy.data.model.Schedule, teacherName: String) {
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
                    Text("MATHEMATICS", modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = DeepGreen, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(schedule.subject ?: "No Subject", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                Text("Room ${schedule.room} • Prof. $teacherName", style = MaterialTheme.typography.bodySmall, color = LightText)
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
fun ScheduleList(viewModel: StudentViewModel) {
    CalendarScheduleView(
        schedules = viewModel.scheduleList,
        title = "Class Schedule",
        userRole = com.example.mymy.data.model.UserRole.STUDENT,
        teacherNames = viewModel.teachersList.associate { (it.id ?: "") to (it.name ?: "Unknown Teacher") },
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
fun GradesList(viewModel: StudentViewModel) {
    var selectedSubject by remember { mutableStateOf<String?>(null) }
    
    // Group grades by subject for navigation
    val gradesBySubject = remember(viewModel.gradesList) {
        viewModel.gradesList.groupBy { it.subject }
    }
    
    // Get unique subjects from both grades and schedule (to show subjects even without grades)
    val allSubjects = remember(viewModel.gradesList, viewModel.scheduleList) {
        (viewModel.gradesList.map { it.subject } + viewModel.scheduleList.mapNotNull { it.subject }).distinct().sorted()
    }

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (selectedSubject != null) {
                        IconButton(onClick = { selectedSubject = null }, modifier = Modifier.padding(end = 8.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                    Column {
                        Text(
                            if (selectedSubject == null) "My Grades" else selectedSubject!!,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (selectedSubject == null) "Academic performance records" else "Quarterly Evaluation",
                            color = Color.White.copy(alpha = 0.7f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }

        Column(modifier = Modifier.fillMaxSize().padding(24.dp)) {
            if (selectedSubject == null) {
                Text("ENROLLED SUBJECTS", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(16.dp))

                if (allSubjects.isEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.MenuBook, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No subjects found.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        items(allSubjects) { subject ->
                            Surface(
                                onClick = { selectedSubject = subject },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)),
                                shadowElevation = 2.dp
                            ) {
                                Row(
                                    modifier = Modifier.padding(20.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Surface(
                                            modifier = Modifier.size(40.dp),
                                            color = LightGreen,
                                            shape = RoundedCornerShape(10.dp)
                                        ) {
                                            Icon(Icons.Default.MenuBook, null, tint = DeepGreen, modifier = Modifier.padding(8.dp))
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Text(subject, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepGreen)
                                    }
                                    Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = SageGreen)
                                }
                            }
                        }
                    }
                }
            } else {
                Text("QUARTERLY GRADES", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
                Spacer(modifier = Modifier.height(16.dp))

                val subjectGrades = gradesBySubject[selectedSubject] ?: emptyList()
                val quarters = listOf("1st Quarter", "2nd Quarter", "3rd Quarter", "4th Quarter")

                LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    items(quarters) { quarter ->
                        val grade = subjectGrades.find { it.quarter?.contains(quarter.take(1)) == true || it.quarter == quarter }
                        
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(quarter, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepGreen)
                                    if (grade != null) {
                                        Text(grade.remarks ?: "Academic evaluation", style = MaterialTheme.typography.bodySmall, color = LightText)
                                    } else {
                                        Text("Not yet graded", style = MaterialTheme.typography.bodySmall, color = LightText.copy(alpha = 0.6f))
                                    }
                                }
                                
                                if (grade != null) {
                                    Surface(
                                        color = if (grade.score >= 75) LightGreen else ErrorColor.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(
                                            "%.1f".format(grade.score),
                                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (grade.score >= 75) DeepGreen else ErrorColor
                                        )
                                    }
                                } else {
                                    Text("N/A", style = MaterialTheme.typography.titleMedium, color = LightText.copy(alpha = 0.4f))
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
fun AttendanceList(viewModel: StudentViewModel) {
    val attendanceBySchedule = viewModel.attendanceBySchedule
    val schedules = viewModel.scheduleList

    Column(modifier = Modifier.fillMaxSize().background(Color.White)) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Attendance Logs", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("View your daily attendance history", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                Text("ATTENDANCE HISTORY", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = DeepGreen)
            }

            items(viewModel.attendanceList.sortedByDescending { it.date }) { att ->
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(att.date, fontWeight = FontWeight.SemiBold, color = DeepGreen)
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(att.subject ?: "General", style = MaterialTheme.typography.bodySmall, color = LightText)
                                if (att.status == "Present" && att.timeIn != null) {
                                    Text(" • ", color = LightText)
                                    // Extract time if it's a full timestamp, otherwise show as is
                                    val timeOnly = if (att.timeIn.contains(" ")) att.timeIn.split(" ")[1] else att.timeIn
                                    Text(timeOnly, style = MaterialTheme.typography.bodySmall, color = LightText)
                                }
                            }
                        }
                        Surface(
                            color = if (att.status == "Present") SageGreen.copy(alpha = 0.1f) else ErrorColor.copy(alpha = 0.1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                att.status,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = if (att.status == "Present") DeepGreen else ErrorColor,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }
            
            if (viewModel.attendanceList.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventNote, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No attendance records found", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileContent(viewModel: StudentViewModel, onLogout: () -> Unit) {
    ProfileDashboard(
        user = viewModel.userProfile,
        onUpdate = { viewModel.updateProfile(it) },
        onLogout = onLogout
    )
}
