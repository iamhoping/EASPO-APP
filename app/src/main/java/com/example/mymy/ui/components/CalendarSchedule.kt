package com.example.mymy.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.UserRole
import com.example.mymy.ui.theme.DeepGreen
import com.example.mymy.ui.theme.LightGreen
import com.example.mymy.ui.theme.LightText
import com.example.mymy.ui.theme.SageGreen
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScheduleView(
    schedules: List<Schedule>,
    title: String = "Schedule",
    subtitle: String? = null,
    onBackClick: (() -> Unit)? = null,
    userImage: @Composable (() -> Unit)? = null,
    userRole: UserRole = UserRole.STUDENT,
    teacherNames: Map<String, String> = emptyMap(),
    sectionNames: Map<Long, String> = emptyMap()
) {
    val calendar = remember { Calendar.getInstance() }
    val todayShort = remember {
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: "MON"
    }

    val mondayCalendar = remember {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            val daysFromMonday = (get(Calendar.DAY_OF_WEEK) - Calendar.MONDAY + 7) % 7
            add(Calendar.DAY_OF_YEAR, -daysFromMonday)
        }
    }

    val weekDays = remember {
        (0..4).map { offset -> // Monday to Friday only
            val d = (mondayCalendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            DayInfo(
                name = d.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: "",
                number = d.get(Calendar.DAY_OF_MONTH).toString(),
                fullDate = d.time
            )
        }
    }

    var selectedDay by remember {
        mutableStateOf(
            if (todayShort == "SAT" || todayShort == "SUN") "MON" else todayShort
        )
    }

    var selectedSchedule by remember { mutableStateOf<Schedule?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FBFB))) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(if (selectedSchedule == null) 160.dp else 140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (selectedSchedule != null || onBackClick != null) {
                        IconButton(onClick = {
                            if (selectedSchedule != null) selectedSchedule = null
                            else onBackClick?.invoke()
                        }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                    Column {
                        Text(
                            if (selectedSchedule != null) selectedSchedule!!.subject ?: "Details" else title,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        if (selectedSchedule == null && subtitle != null) {
                            Text(subtitle, color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        } else if (selectedSchedule != null) {
                            Text("Schedule Details", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                if (selectedSchedule == null) {
                    Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                        userImage?.invoke() ?: Surface(
                            modifier = Modifier.size(48.dp),
                            shape = RoundedCornerShape(24.dp),
                            color = Color.White.copy(alpha = 0.2f)
                        ) {
                            Icon(Icons.Default.Person, null, tint = Color.White, modifier = Modifier.padding(8.dp))
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedSchedule == null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(24.dp))
                // Day Selector
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(weekDays) { day ->
                        val isSelected = selectedDay == day.name
                        val isToday = todayShort == day.name
                        
                        Surface(
                            onClick = { selectedDay = day.name },
                            modifier = Modifier.width(64.dp).height(80.dp),
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) SageGreen else Color.White,
                            border = if (isToday && !isSelected) androidx.compose.foundation.BorderStroke(2.dp, SageGreen) else null,
                            shadowElevation = if (isSelected) 4.dp else 0.dp
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center,
                                modifier = Modifier.padding(8.dp)
                            ) {
                                Text(
                                    day.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (isSelected) Color.White else Color.Gray,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    day.number,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = if (isSelected) Color.White else DeepGreen
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = "${selectedDay}'S CLASSES",
                    modifier = Modifier.padding(horizontal = 24.dp),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                val dailySchedules = schedules.filter { 
                    it.day.uppercase().startsWith(selectedDay.take(3)) 
                }.sortedBy { it.startTime }

                if (dailySchedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize().padding(top = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.EventBusy, null, modifier = Modifier.size(64.dp), tint = Color.LightGray)
                            Spacer(modifier = Modifier.height(16.dp))
                            Text("No classes scheduled for this day", color = Color.Gray)
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 32.dp)
                    ) {
                        items(dailySchedules) { schedule ->
                            ScheduleCard(
                                schedule = schedule,
                                userRole = userRole,
                                teacherNames = teacherNames,
                                sectionNames = sectionNames,
                                onClick = { selectedSchedule = schedule }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = selectedSchedule != null,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            if (selectedSchedule != null) {
                ScheduleDetailsView(
                    schedule = selectedSchedule!!,
                    userRole = userRole,
                    teacherNames = teacherNames,
                    sectionNames = sectionNames
                )
            }
        }
    }
}

@Composable
fun ScheduleCard(
    schedule: Schedule,
    userRole: UserRole,
    teacherNames: Map<String, String>,
    sectionNames: Map<Long, String>,
    onClick: () -> Unit
) {
    val displayTime = remember(schedule.startTime, schedule.endTime) {
        formatTimeRange(schedule.startTime, schedule.endTime)
    }
    
    val teacherName = schedule.teacherId?.let { teacherNames[it] } ?: "Not assigned"
    val sectionName = schedule.sectionId?.let { sectionNames[it] } ?: "Not assigned"

    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0)),
        shadowElevation = 2.dp
    ) {
        Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(40.dp)
                    .background(SageGreen, RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    schedule.subject ?: "Untitled",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen
                )
                Text(
                    displayTime,
                    style = MaterialTheme.typography.bodySmall,
                    color = DeepGreen,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Place, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Room ${schedule.room}", style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    
                    if (userRole == UserRole.TEACHER || userRole == UserRole.SCHOOL_ADMIN) {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Groups, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(sectionName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    } else {
                        Spacer(modifier = Modifier.width(12.dp))
                        Icon(Icons.Default.Person, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(teacherName, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    }
                }
            }
            Icon(Icons.AutoMirrored.Filled.ArrowForward, null, tint = SageGreen.copy(alpha = 0.5f))
        }
    }
}

@Composable
fun ScheduleDetailsView(
    schedule: Schedule,
    userRole: UserRole,
    teacherNames: Map<String, String>,
    sectionNames: Map<Long, String>
) {
    val teacherName = schedule.teacherId?.let { teacherNames[it] } ?: "Not assigned"
    val sectionName = schedule.sectionId?.let { sectionNames[it] } ?: "Not assigned"
    val displayTime = formatTimeRange(schedule.startTime, schedule.endTime)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        DetailItem(icon = Icons.AutoMirrored.Filled.MenuBook, label = "Subject", value = schedule.subject ?: "N/A")
        DetailItem(icon = Icons.Default.Today, label = "Day", value = schedule.day.replaceFirstChar { it.uppercase() })
        DetailItem(icon = Icons.Default.AccessTime, label = "Time", value = displayTime)
        DetailItem(icon = Icons.Default.Place, label = "Room", value = "Room ${schedule.room}")
        
        if (userRole == UserRole.TEACHER || userRole == UserRole.SCHOOL_ADMIN) {
            DetailItem(icon = Icons.Default.Groups, label = "Section", value = sectionName)
        }
        
        if (userRole != UserRole.TEACHER) {
            DetailItem(icon = Icons.Default.Person, label = "Teacher", value = teacherName)
        }
        
        if (userRole == UserRole.TEACHER) {
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = { /* Navigate to class list if needed */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = DeepGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.List, null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("View Class List")
            }
        }
    }
}

@Composable
fun DetailItem(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF0F0F0))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(
                modifier = Modifier.size(40.dp),
                color = LightGreen,
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(icon, null, tint = DeepGreen, modifier = Modifier.padding(8.dp))
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(label, style = MaterialTheme.typography.labelSmall, color = LightText)
                Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = DeepGreen)
            }
        }
    }
}

fun formatTimeRange(start: String?, end: String?): String {
    fun format(time: String?): String {
        if (time == null) return "--:--"
        val p = time.split(":")
        if (p.size < 2) return time
        var h = p[0].toIntOrNull() ?: 0
        val m = p[1].toIntOrNull() ?: 0
        val ampm = if (h >= 12) "PM" else "AM"
        if (h > 12) h -= 12
        if (h == 0) h = 12
        return "%d:%02d %s".format(h, m, ampm)
    }
    return "${format(start)} – ${format(end)}"
}

data class DayInfo(val name: String, val number: String, val fullDate: java.util.Date)
