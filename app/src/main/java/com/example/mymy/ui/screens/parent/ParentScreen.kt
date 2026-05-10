package com.example.mymy.ui.screens.parent

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymy.data.model.Attendance
import com.example.mymy.data.model.Grade
import com.example.mymy.data.model.Schedule
import com.example.mymy.ui.components.CalendarScheduleView
import com.example.mymy.ui.theme.*
import com.example.mymy.ui.viewmodel.ParentViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ParentScreen(
    viewModel: ParentViewModel = viewModel(),
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var studentIdToLink by remember { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.fetchChildren()
    }

    // Handle Success/Error Messages via Snackbar
    LaunchedEffect(viewModel.errorMessage, viewModel.successMessage) {
        viewModel.errorMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Long)
            viewModel.errorMessage = null
        }
        viewModel.successMessage?.let {
            snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
            viewModel.successMessage = null
        }
    }

    Scaffold(
        topBar = {
            // Replaced with specific screen headers in each tab
        },
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
                        icon = { Icon(Icons.Default.Home, "Overview") },
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
                        icon = { Icon(Icons.Default.Schedule, "Schedule") },
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
                        icon = { Icon(Icons.Default.Grade, "Grades") },
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
                        icon = { Icon(Icons.Default.FactCheck, "Attendance") },
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
                }
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundColor)
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize()
            ) {
                if (viewModel.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SageGreen)
                }

                if (viewModel.children.isNotEmpty()) {
                    when (selectedTab) {
                        0 -> ParentDashboardOverview(viewModel, onLogout = onLogout, onNavigateToTab = { selectedTab = it })
                        1 -> ChildScheduleList(viewModel)
                        2 -> ChildGradesList(viewModel)
                        3 -> ChildAttendanceList(viewModel)
                    }
                } else if (!viewModel.isLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(80.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = SageGreen.copy(alpha = 0.1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.GroupOff,
                                contentDescription = null,
                                modifier = Modifier.padding(20.dp),
                                tint = SageGreen
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        Text(
                            "No children linked to your account.",
                            color = DarkText,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Text(
                            "Enter a Student ID to start monitoring their academic progress, attendance, and schedules.",
                            color = LightText,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 32.dp)
                        )

                        OutlinedTextField(
                            value = studentIdToLink,
                            onValueChange = { if (it.length <= 15) studentIdToLink = it.uppercase() },
                            label = { Text("Student ID (e.g. STD-XXXX)") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = SageGreen,
                                focusedLabelColor = SageGreen
                            ),
                            leadingIcon = { Icon(Icons.Default.Fingerprint, null, tint = SageGreen) }
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Button(
                            onClick = { 
                                if (studentIdToLink.isNotBlank()) {
                                    viewModel.linkStudent(studentIdToLink)
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                            shape = RoundedCornerShape(16.dp),
                            enabled = studentIdToLink.isNotBlank() && !viewModel.isLoading
                        ) {
                            if (viewModel.isLoading) {
                                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                            } else {
                                Text("Connect Student", fontWeight = FontWeight.Bold)
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                        
                        TextButton(onClick = { viewModel.fetchChildren() }) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Refresh List", color = SageGreen)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        TextButton(
                            onClick = onLogout
                        ) {
                            Text("Sign Out", color = LightText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentDashboardOverview(
    viewModel: ParentViewModel,
    onLogout: () -> Unit,
    onNavigateToTab: (Int) -> Unit
) {
    val selectedChild = viewModel.children.find { it.id == viewModel.selectedChildId }

    Column(modifier = Modifier.fillMaxSize()) {
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
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.mymy.R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Parent Dashboard", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                            Text("Monitoring: ${selectedChild?.name ?: "No Child Selected"}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                IconButton(
                    onClick = onLogout,
                    modifier = Modifier.background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
                ) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            item {
                // Child Selector (Tabs) - Moved inside the content area
                ScrollableTabRow(
                    selectedTabIndex = viewModel.children.indexOfFirst { it.id == viewModel.selectedChildId }.coerceAtLeast(0),
                    containerColor = Color.Transparent,
                    contentColor = SageGreen,
                    edgePadding = 0.dp,
                    divider = {},
                    indicator = { tabPositions ->
                        val index = viewModel.children.indexOfFirst { it.id == viewModel.selectedChildId }.coerceAtLeast(0)
                        if (index < tabPositions.size) {
                            TabRowDefaults.SecondaryIndicator(
                                modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                                color = SageGreen
                            )
                        }
                    }
                ) {
                    viewModel.children.forEach { child ->
                        Tab(
                            selected = viewModel.selectedChildId == child.id,
                            onClick = {
                                viewModel.selectedChildId = child.id
                                viewModel.fetchChildData(child.id)
                            },
                            text = {
                                Text(
                                    child.name ?: "Unknown",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (viewModel.selectedChildId == child.id) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }
            }

            item {
                val attendanceRate = viewModel.attendanceRate
                val averageGrade = viewModel.averageGrade

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SageGreen),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(24.dp)) {
                        Text("Current Attendance", color = Color.White, style = MaterialTheme.typography.labelMedium)
                        Text(
                            "$attendanceRate%",
                            color = Color.White,
                            style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val statusText = when {
                            attendanceRate >= 90 -> "Excellent attendance!"
                            attendanceRate >= 75 -> "Good, but try to be more consistent."
                            attendanceRate == 0 && viewModel.childAttendance.isEmpty() -> "No attendance records yet."
                            else -> "Attendance needs improvement."
                        }
                        Text(statusText, color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                    }
                }
            }

            item {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ParentStatCard(
                        title = "Avg Grade",
                        value = if (viewModel.childGrades.isNotEmpty()) String.format("%.1f", viewModel.averageGrade) else "N/A",
                        subValue = "${viewModel.childGrades.size} Subjects Recorded",
                        icon = Icons.Default.AutoGraph,
                        modifier = Modifier.weight(1f)
                    )
                    ParentStatCard(
                        title = "Classes Today",
                        value = "${viewModel.childSchedule.size}",
                        subValue = "Full Schedule",
                        icon = Icons.Default.Event,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            item {
                Text(
                    "RECENT ATTENDANCE",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
            }

            if (viewModel.childAttendance.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventNote, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No recent attendance records.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            items(viewModel.childAttendance.sortedByDescending { it.date }.take(3)) { att ->
                AttendanceListItem(att)
            }

            item {
                val selectedChild = viewModel.children.find { it.id == viewModel.selectedChildId }
                Button(
                    onClick = { onNavigateToTab(3) },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = DeepGreen),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                ) {
                    Text("View Detailed Reports", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun ParentStatCard(title: String, value: String, subValue: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Icon(icon, contentDescription = null, tint = SageGreen, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(title, style = MaterialTheme.typography.labelSmall, color = LightText)
            Text(value, style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = DarkText)
            Text(subValue, style = MaterialTheme.typography.bodySmall, color = LightText, maxLines = 1)
        }
    }
}

@Composable
fun ChildScheduleList(viewModel: ParentViewModel) {
    CalendarScheduleView(
        schedules = viewModel.childSchedule,
        title = "Child's Schedule",
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
fun ChildGradesList(viewModel: ParentViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Academic Grades", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    val selectedChild = viewModel.children.find { it.id == viewModel.selectedChildId }
                    Text("Performance report for ${selectedChild?.name ?: "Child"}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                "PERFORMANCE SUMMARY",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color = DeepGreen
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            if (viewModel.childGrades.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.HistoryEdu, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No grades recorded yet.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    items(viewModel.childGrades) { grade ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White,
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
                        ) {
                            Row(
                                modifier = Modifier.padding(20.dp).fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(grade.subject, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = DeepGreen)
                                    Text(grade.remarks ?: "Final Grade", style = MaterialTheme.typography.bodySmall, color = LightText)
                                    grade.createdAt?.let {
                                        Text("Posted on ${it.split("T")[0]}", style = MaterialTheme.typography.labelSmall, color = LightText.copy(alpha = 0.7f))
                                    }
                                }
                                Surface(
                                    color = if (grade.score >= 75) LightGreen else ErrorColor.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text(
                                        text = String.format("%.1f", grade.score),
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = if (grade.score >= 75) DeepGreen else ErrorColor
                                    )
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
fun ChildAttendanceList(viewModel: ParentViewModel) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Curved Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize(), contentAlignment = Alignment.CenterStart) {
                Column {
                    Text("Attendance Logs", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    val selectedChild = viewModel.children.find { it.id == viewModel.selectedChildId }
                    Text("Monitoring daily presence for ${selectedChild?.name ?: "Child"}", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 24.dp)
        ) {
            if (viewModel.childAttendance.isEmpty()) {
                item {
                    Box(modifier = Modifier.fillMaxWidth().padding(40.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventNote, null, modifier = Modifier.size(48.dp), tint = LightGreen)
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("No attendance records found.", color = LightText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }
            items(viewModel.childAttendance.sortedByDescending { it.date }) { att ->
                AttendanceListItem(att)
            }
        }
    }
}

@Composable
fun AttendanceListItem(att: Attendance) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceColor)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(att.date, fontWeight = FontWeight.SemiBold)
                Text(att.subject ?: "General", style = MaterialTheme.typography.bodySmall, color = LightText)
            }
            val isPresent = att.status == "Present"
            val isLate = att.status == "Late"
            SuggestionChip(
                onClick = { },
                label = { Text(att.status) },
                colors = SuggestionChipDefaults.suggestionChipColors(
                    containerColor = when {
                        isPresent -> LightGreen.copy(alpha = 0.2f)
                        isLate -> Color(0xFFFFF3E0)
                        else -> ErrorColor.copy(alpha = 0.1f)
                    },
                    labelColor = when {
                        isPresent -> SageGreen
                        isLate -> Color(0xFFE65100)
                        else -> ErrorColor
                    }
                ),
                border = null,
                shape = RoundedCornerShape(12.dp)
            )
        }
    }
}
