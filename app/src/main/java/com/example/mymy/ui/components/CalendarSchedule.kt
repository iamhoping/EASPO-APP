package com.example.mymy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.mymy.ui.theme.DeepGreen
import com.example.mymy.ui.theme.LightGreen
import com.example.mymy.ui.theme.SageGreen
import java.util.Calendar
import java.util.Locale

@Composable
fun CalendarScheduleView(
    schedules: List<Schedule>,
    title: String = "Schedule",
    onBackClick: (() -> Unit)? = null,
    userImage: @Composable (() -> Unit)? = null
) {
    // Calculate dates for the current week starting from Monday
    val calendar = remember { Calendar.getInstance() }
    val todayShort = remember { 
        calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: "MON" 
    }
    
    // Adjusted Monday logic: find the most recent Monday
    val mondayCalendar = remember {
        (calendar.clone() as Calendar).apply {
            set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
            if (calendar.get(Calendar.DAY_OF_WEEK) == Calendar.SUNDAY) {
                add(Calendar.DAY_OF_YEAR, -6)
            }
        }
    }

    val weekDays = remember {
        (0..4).map { offset -> // Changed from 0..5 to 0..4 for Mon-Fri only
            val d = (mondayCalendar.clone() as Calendar).apply {
                add(Calendar.DAY_OF_YEAR, offset)
            }
            DayInfo(
                name = d.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH)?.uppercase() ?: "",
                number = d.get(Calendar.DAY_OF_MONTH).toString()
            )
        }
    }

    var selectedDay by remember { 
        mutableStateOf(todayShort.let { 
            if (it == "SUN" || it == "SAT") "MON" else it 
        }) 
    }
    var viewMode by remember { mutableStateOf("Week") }

    Column(modifier = Modifier.fillMaxSize().background(Color(0xFFF8FBFB))) {
        // Header
        Surface(
            modifier = Modifier.fillMaxWidth().height(140.dp),
            color = DeepGreen,
            shape = RoundedCornerShape(bottomStart = 32.dp, bottomEnd = 32.dp)
        ) {
            Box(modifier = Modifier.padding(24.dp).fillMaxSize()) {
                Row(
                    modifier = Modifier.align(Alignment.CenterStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (onBackClick != null) {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                        }
                    }
                    Text(
                        title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
                
                Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                    if (userImage != null) {
                        userImage()
                    } else {
                        Surface(
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

        Spacer(modifier = Modifier.height(24.dp))

        // Week/Day Selector & Class Filter
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFF0F4F4)
            ) {
                Row(modifier = Modifier.padding(4.dp)) {
                    TabButton("Week", viewMode == "Week") { viewMode = "Week" }
                    TabButton("Day", viewMode == "Day") { viewMode = "Day" }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE0E0E0))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All Classes", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                    Icon(Icons.Default.KeyboardArrowDown, null, modifier = Modifier.size(20.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Calendar Strip
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.width(48.dp)) // Offset for time column
            weekDays.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable { selectedDay = day.name }
                ) {
                    Text(day.name, style = MaterialTheme.typography.labelSmall, color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        day.number,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedDay == day.name) DeepGreen else Color.Black
                    )
                    if (selectedDay == day.name) {
                        Box(modifier = Modifier.size(4.dp).background(DeepGreen, RoundedCornerShape(2.dp)))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        HorizontalDivider(color = Color(0xFFF0F4F4), thickness = 1.dp)

        // Time Grid
        Box(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
            Column {
                (8..18).forEach { hour ->
                    TimeRow(hour)
                }
            }
            
            // Schedule Items (Visual representation)
            // This is a simplified grid placement for demo
            schedules.forEach { schedule ->
                // Basic logic: if day matches selectedDay (or in week view, we'd place them horizontally)
                // For this UI, we'll just show them in the grid if they match the selected day
                val scheduleDayShort = schedule.day.uppercase().take(3)
                if (scheduleDayShort == selectedDay || viewMode == "Week") {
                   ScheduleGridItem(schedule, selectedDay, viewMode == "Week")
                }
            }
        }
    }
}

@Composable
fun TabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) DeepGreen else Color.Transparent,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            color = if (isSelected) Color.White else Color.Gray,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
fun TimeRow(hour: Int) {
    Row(
        modifier = Modifier.fillMaxWidth().height(80.dp),
        verticalAlignment = Alignment.Top
    ) {
        val displayHour = remember(hour) {
            val h = if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
            val ampm = if (hour >= 12) "PM" else "AM"
            String.format(Locale.getDefault(), "%d:00 %s", h, ampm)
        }
        Text(
            displayHour,
            modifier = Modifier.width(80.dp).padding(top = 8.dp),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.labelSmall,
            color = Color.Gray
        )
        Box(modifier = Modifier.fillMaxSize().padding(end = 16.dp)) {
            HorizontalDivider(color = Color(0xFFF0F4F4), modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
fun ScheduleGridItem(schedule: Schedule, selectedDay: String, isWeekView: Boolean) {
    // Use start_time/end_time if available
    fun parseTime(schedule: Schedule): Pair<Float, Float> {
        try {
            val startParts = (schedule.startTime ?: "08:00:00").split(":")
            val endParts = (schedule.endTime ?: "09:30:00").split(":")
            val start = startParts[0].toFloat() + startParts[1].toFloat() / 60f
            val end = endParts[0].toFloat() + endParts[1].toFloat() / 60f
            return start to (end - start)
        } catch (e: Exception) {
            return 8f to 1.5f
        }
    }

    val (startTime, duration) = parseTime(schedule)

    
    // Grid starts at 7 AM
    val topOffset = 80.dp * (startTime - 7f)
    val height = 80.dp * duration
    
    val dayIndex = when(schedule.day.uppercase()) {
        "MONDAY" -> 0
        "TUESDAY" -> 1
        "WEDNESDAY" -> 2
        "THURSDAY" -> 3
        "FRIDAY" -> 4
        "SATURDAY" -> 5
        else -> 0
    }
    
    val isHighlighted = (schedule.subject ?: "").contains("Calc") // Mocking the highlighted state in image
    
    Box(
        modifier = Modifier
            .padding(start = 80.dp)
            .offset(
                x = if(isWeekView) (dayIndex * 60).dp else 0.dp,
                y = topOffset
            )
            .width(if(isWeekView) 55.dp else 260.dp)
            .height(height)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isHighlighted) DeepGreen else Color(0xFFE8F3F1))
            .padding(8.dp)
    ) {
        Column {
            Text(
                schedule.subject ?: "Untitled",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = if (isHighlighted) Color.White else DeepGreen,
                maxLines = 1
            )
            Text(
                "Room ${schedule.room}",
                style = MaterialTheme.typography.labelSmall,
                color = if (isHighlighted) Color.White.copy(alpha = 0.7f) else Color.Gray
            )
            if (!isWeekView || isHighlighted) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.AccessTime, 
                        null, 
                        modifier = Modifier.size(10.dp), 
                        tint = if (isHighlighted) Color.White else DeepGreen
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    
                    val displayTime = remember(schedule.startTime, schedule.endTime) {
                        fun format(time: String?): String {
                            if (time == null) return "--:--"
                            val p = time.split(":")
                            var h = p[0].toInt()
                            val m = p[1].toInt()
                            val ampm = if (h >= 12) "PM" else "AM"
                            if (h > 12) h -= 12
                            if (h == 0) h = 12
                            return "%d:%02d%s".format(h, m, ampm)
                        }
                        "${format(schedule.startTime)}-${format(schedule.endTime)}"
                    }
                    
                    Text(
                        displayTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isHighlighted) Color.White else DeepGreen,
                        fontSize = 8.sp
                    )
                }
            }
        }
    }
}

data class DayInfo(val name: String, val number: String)
