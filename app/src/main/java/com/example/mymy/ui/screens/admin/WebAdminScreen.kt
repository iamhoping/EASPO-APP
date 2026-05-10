package com.example.mymy.ui.screens.admin

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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.ui.theme.*
import com.example.mymy.ui.viewmodel.WebAdminViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebAdminScreen(
    viewModel: WebAdminViewModel = viewModel(),
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    var showUserDialog by remember { mutableStateOf<User?>(null) }
    var showScheduleDialog by remember { mutableStateOf<Schedule?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchData()
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        androidx.compose.foundation.Image(
                            painter = androidx.compose.ui.res.painterResource(id = com.example.mymy.R.drawable.logo),
                            contentDescription = "Logo",
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                when (selectedTab) {
                                    0 -> "Web Admin Console"
                                    1 -> "User Management"
                                    2 -> "Schedule Control"
                                    else -> "Admin"
                                },
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                "System Oversight",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightText
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Logout", tint = ErrorColor)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundColor,
                    scrolledContainerColor = SurfaceColor
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = SurfaceColor,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Home, "Home") },
                    label = { Text("Home") },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SageGreen,
                        selectedTextColor = SageGreen,
                        indicatorColor = PaleOrange
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, "Users") },
                    label = { Text("Users") },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SageGreen,
                        selectedTextColor = SageGreen,
                        indicatorColor = PaleOrange
                    )
                )
                NavigationBarItem(
                    icon = { Icon(Icons.AutoMirrored.Filled.List, "Schedules") },
                    label = { Text("Schedules") },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = SageGreen,
                        selectedTextColor = SageGreen,
                        indicatorColor = PaleOrange
                    )
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == 2) {
                ExtendedFloatingActionButton(
                    onClick = { showScheduleDialog = Schedule(subject = "", day = "", startTime = "", endTime = "", room = "") },
                    containerColor = SageGreen,
                    contentColor = Color.White,
                    icon = { Icon(Icons.Default.Add, null) },
                    text = { Text("New Schedule") },
                    shape = RoundedCornerShape(16.dp)
                )
            }
        },
        containerColor = BackgroundColor
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(listOf(BackgroundColor, PaleOrange.copy(alpha = 0.2f)))
                )
        ) {
            Column(
                modifier = Modifier
                    .padding(padding)
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
            ) {
                if (viewModel.isLoading) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(), color = SageGreen)
                    Spacer(modifier = Modifier.height(16.dp))
                }

                viewModel.errorMessage?.let {
                    Text(it, color = MaterialTheme.colorScheme.error)
                }

                when (selectedTab) {
                    0 -> WebAdminHome(viewModel)
                    1 -> UserManagementContent(viewModel, onEditUser = { showUserDialog = it })
                    2 -> ScheduleManagementContent(viewModel, onEditSchedule = { showScheduleDialog = it })
                }
            }
        }

        if (showUserDialog != null) {
            EditUserDialog(
                user = showUserDialog!!,
                onDismiss = { showUserDialog = null },
                onSave = { updated ->
                    viewModel.updateUser(updated)
                    showUserDialog = null
                },
                onDelete = {
                    viewModel.deleteUser(showUserDialog!!.id)
                    showUserDialog = null
                }
            )
        }

        if (showScheduleDialog != null) {
            EditScheduleDialog(
                schedule = showScheduleDialog!!,
                onDismiss = { showScheduleDialog = null },
                onSave = { updated ->
                    viewModel.upsertSchedule(updated)
                    showScheduleDialog = null
                },
                onDelete = { id ->
                    viewModel.deleteSchedule(id)
                    showScheduleDialog = null
                }
            )
        }
    }
}

@Composable
fun WebAdminHome(viewModel: WebAdminViewModel) {
    LazyColumn(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(vertical = 16.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SageGreen),
                shape = RoundedCornerShape(24.dp)
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    Text("System Status", color = Color.White, style = MaterialTheme.typography.labelMedium)
                    Text(
                        "Operational",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineLarge.copy(fontWeight = FontWeight.Bold)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("All services are running normally.", color = Color.White.copy(alpha = 0.8f), fontSize = 12.sp)
                }
            }
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WebAdminStatCard(
                        title = "Total Users",
                        value = "${viewModel.allUsers.size}",
                        icon = Icons.Default.Person,
                        modifier = Modifier.weight(1f)
                    )
                    WebAdminStatCard(
                        title = "Active Schedules",
                        value = "${viewModel.allSchedules.size}",
                        icon = Icons.AutoMirrored.Filled.List,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    WebAdminStatCard(
                        title = "Total Students",
                        value = "${viewModel.studentsCount}",
                        icon = Icons.Default.Groups,
                        modifier = Modifier.weight(1f)
                    )
                    WebAdminStatCard(
                        title = "Total Teachers",
                        value = "${viewModel.teachersCount}",
                        icon = Icons.Default.School,
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    WebAdminStatCard(
                        title = "Total Parents",
                        value = "${viewModel.parentsCount}",
                        icon = Icons.Default.FamilyRestroom,
                        modifier = Modifier.fillMaxWidth(0.5f)
                    )
                }
            }
        }

        item {
            Text(
                "Quick Actions",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = DarkText)
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceColor)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PaleOrange, contentColor = SageGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("System Logs", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { /* TODO */ },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PaleOrange, contentColor = SageGreen),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Database Backup", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun WebAdminStatCard(title: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier = Modifier) {
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
        }
    }
}

@Composable
fun UserManagementContent(viewModel: WebAdminViewModel, onEditUser: (User) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = viewModel.searchQuery,
            onValueChange = { viewModel.searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            placeholder = { Text("Search by name or email...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = SageGreen,
                unfocusedBorderColor = LightText.copy(alpha = 0.5f)
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = viewModel.roleFilter == null,
                onClick = { viewModel.roleFilter = null },
                label = { Text("All") }
            )
            FilterChip(
                selected = viewModel.roleFilter == UserRole.STUDENT,
                onClick = { viewModel.roleFilter = UserRole.STUDENT },
                label = { Text("Students") }
            )
            FilterChip(
                selected = viewModel.roleFilter == UserRole.TEACHER,
                onClick = { viewModel.roleFilter = UserRole.TEACHER },
                label = { Text("Teachers") }
            )
            FilterChip(
                selected = viewModel.roleFilter == UserRole.PARENT,
                onClick = { viewModel.roleFilter = UserRole.PARENT },
                label = { Text("Parents") }
            )
        }

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(bottom = 16.dp),
            modifier = Modifier.weight(1f)
        ) {
            val filtered = viewModel.filteredUsers
            if (filtered.isEmpty() && !viewModel.isLoading) {
                item {
                    Box(modifier = Modifier.fillParentMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.SearchOff, null, modifier = Modifier.size(64.dp), tint = LightText.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No users match your criteria", color = LightText)
                        }
                    }
                }
            } else {
                items(filtered) { user ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = SageGreen.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.size(48.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.Person, contentDescription = null, tint = SageGreen)
                                }
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(user.name ?: "Unknown", style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold))
                                Text("${user.role} • ${user.email}", style = MaterialTheme.typography.bodySmall, color = LightText)
                            }
                            IconButton(onClick = { onEditUser(user) }) {
                                Icon(Icons.Default.Edit, "Edit", tint = SageGreen)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ScheduleManagementContent(viewModel: WebAdminViewModel, onEditSchedule: (Schedule) -> Unit) {
    val schedules = viewModel.allSchedules

    if (schedules.isEmpty() && !viewModel.isLoading) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    Icons.AutoMirrored.Filled.List,
                    contentDescription = null,
                    modifier = Modifier.size(64.dp),
                    tint = LightText.copy(alpha = 0.3f)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("No schedules found", color = LightText)
                TextButton(onClick = { viewModel.fetchAllSchedules() }) {
                    Text("Tap to refresh", color = SageGreen)
                }
            }
        }
    } else {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(vertical = 16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(schedules) { schedule ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceColor),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = PaleOrange,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    (schedule.subject ?: "S").take(1),
                                    fontWeight = FontWeight.Bold,
                                    color = SageGreen
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                schedule.subject ?: "Untitled Subject",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                            )
                            Text(
                                "${schedule.day} • ${schedule.startTime ?: "--"} to ${schedule.endTime ?: "--"} • Room ${schedule.room}",
                                style = MaterialTheme.typography.bodySmall,
                                color = LightText
                            )
                            if (!schedule.time.isNullOrBlank()) {
                                Text("Legacy Time: ${schedule.time}", style = MaterialTheme.typography.labelSmall, color = LightText)
                            }
                        }
                        IconButton(onClick = { onEditSchedule(schedule) }) {
                            Icon(Icons.Default.Edit, "Edit", tint = SageGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun EditUserDialog(user: User, onDismiss: () -> Unit, onSave: (User) -> Unit, onDelete: () -> Unit) {
    var name by remember { mutableStateOf(user.name ?: "") }
    var role by remember { mutableStateOf(user.role) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit User Profile", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name ?: "",
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedCard(
                        onClick = { expanded = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Role: ${role?.name ?: "NONE"}", fontWeight = FontWeight.Medium)
                            Icon(Icons.Default.ArrowDropDown, null)
                        }
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        modifier = Modifier.fillMaxWidth(0.7f)
                    ) {
                        UserRole.entries.forEach { r ->
                            DropdownMenuItem(
                                text = { Text(r.name) },
                                onClick = { role = r; expanded = false }
                            )
                        }
                    }
                }
                
                TextButton(
                    onClick = onDelete,
                    colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor),
                    modifier = Modifier.align(Alignment.Start)
                ) {
                    Icon(Icons.Default.Delete, "Delete")
                    Spacer(Modifier.width(8.dp))
                    Text("Remove User Access")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(user.copy(name = name, role = role)) },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = SurfaceColor
    )
}

@Composable
fun EditScheduleDialog(schedule: Schedule, onDismiss: () -> Unit, onSave: (Schedule) -> Unit, onDelete: (Long) -> Unit) {
    var subject by remember { mutableStateOf(schedule.subject ?: "") }
    var day by remember { mutableStateOf(schedule.day) }
    var startTime by remember { mutableStateOf(schedule.startTime ?: "") }
    var endTime by remember { mutableStateOf(schedule.endTime ?: "") }
    var room by remember { mutableStateOf(schedule.room) }
    var studentId by remember { mutableStateOf(schedule.studentId ?: "") }
    var teacherId by remember { mutableStateOf(schedule.teacherId ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (schedule.id == null) "New Schedule Entry" else "Modify Schedule", fontWeight = FontWeight.Bold) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject Name") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = day, onValueChange = { day = it }, label = { Text("Day") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = startTime, onValueChange = { startTime = it }, label = { Text("Start (HH:mm:ss)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                        OutlinedTextField(value = endTime, onValueChange = { endTime = it }, label = { Text("End (HH:mm:ss)") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f))
                    }
                }
                item {
                    OutlinedTextField(value = room, onValueChange = { room = it }, label = { Text("Room / Location") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = studentId, onValueChange = { studentId = it }, label = { Text("Student UID") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                }
                item {
                    OutlinedTextField(value = teacherId, onValueChange = { teacherId = it }, label = { Text("Teacher UID") }, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth())
                }
                
                if (schedule.id != null) {
                    item {
                        TextButton(
                            onClick = { schedule.id?.let { onDelete(it) } },
                            colors = ButtonDefaults.textButtonColors(contentColor = ErrorColor)
                        ) {
                            Icon(Icons.Default.Delete, "Delete")
                            Spacer(Modifier.width(8.dp))
                            Text("Delete Schedule Record")
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(schedule.copy(subject = subject, day = day, startTime = startTime, endTime = endTime, room = room, studentId = studentId.ifEmpty { null }, teacherId = teacherId.ifEmpty { null })) },
                colors = ButtonDefaults.buttonColors(containerColor = SageGreen),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Commit Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = SurfaceColor
    )
}
