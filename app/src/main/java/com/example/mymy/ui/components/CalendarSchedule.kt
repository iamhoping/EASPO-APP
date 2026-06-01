package com.example.mymy.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.mymy.data.model.Schedule
import com.example.mymy.ui.theme.DeepGreen
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
    
    // Explicit Monday logic: find the Monday of the current week
    val mondayCalendar = remember {
        Calendar.getInstance().apply {
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            
            // Set to Monday of the current week regardless of locale
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
                number = d.get(Calendar.DAY_OF_MONTH).toString()
            )
        }
    }/**/

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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.width(80.dp)) // Match the time column width
            weekDays.forEach { day ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f).clickable { selectedDay = day.name }
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
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            val gridWidth = maxWidth - 80.dp
            val columnWidth = gridWidth / 5

            Column(modifier = Modifier.padding(top = 24.dp, bottom = 32.dp)) {
                (7..18).forEach { hour ->
                    TimeRow(hour)
                }
            }
            
            // Schedule Items (Visual representation)
            schedules.forEach { schedule ->
                val dayOfWeekIndex = getDayIndex(schedule.day)
                val scheduleDayShort = schedule.day?.uppercase()?.take(3) ?: ""
                
                // Only show Monday-Friday in Week view, or the specific day in Day view
                if ((viewMode == "Day" && scheduleDayShort == selectedDay) || 
                    (viewMode == "Week" && dayOfWeekIndex in 0..4)) {
                   ScheduleGridItem(
                       schedule = schedule, 
                       isWeekView = viewMode == "Week",
                       columnWidth = columnWidth,
                       gridWidth = gridWidth,
                       modifier = Modifier.padding(top = 24.dp)
                   )
                }
            }
        }
    }
}

private fun getDayIndex(day: String?): Int {
    val d = day?.uppercase() ?: return -1
    return when {
        d.startsWith("MON") -> 0
        d.startsWith("TUE") -> 1
        d.startsWith("WED") -> 2
        d.startsWith("THU") -> 3
        d.startsWith("FRI") -> 4
        else -> -1
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
            color = Color.DarkGray,
            fontWeight = FontWeight.Medium
        )
        Box(modifier = Modifier.fillMaxSize()) {
            HorizontalDivider(color = Color(0xFFE0E0E0), modifier = Modifier.align(Alignment.TopStart))
        }
    }
}

@Composable
fun ScheduleGridItem(
    schedule: Schedule, 
    isWeekView: Boolean,
    modifier: Modifier = Modifier,
    columnWidth: Dp = 60.dp,
    gridWidth: Dp = 300.dp
) {
    // Use start_time/end_time if available
    fun parseTime(schedule: Schedule): Pair<Float, Float> {
        try {
            val startTimeStr = schedule.startTime ?: "07:00:00"
            val endTimeStr = schedule.endTime ?: "08:30:00"
            
            fun toFloatHour(time: String): Float {
                val parts = time.split(":")
                val h = parts[0].toFloatOrNull() ?: 7f
                val m = if (parts.size > 1) parts[1].toFloatOrNull() ?: 0f else 0f
                return h + m / 60f
            }
            
            val start = toFloatHour(startTimeStr)
            val end = toFloatHour(endTimeStr)
            return start to (end - start)
        } catch (_: Exception) {
            return 7f to 1.5f
        }
    }

    val (startTime, duration) = parseTime(schedule)

    
    // Grid starts at 7 AM (to match the TimeRow loop)
    val topOffset = 80.dp * (startTime - 7f)
    val height = 80.dp * duration
    
    val dayIndex = getDayIndex(schedule.day)
    
    val isHighlighted = (schedule.subject ?: "").contains("Calc") // Mocking the highlighted state in image
    
    Box(
        modifier = modifier
            .padding(start = 80.dp)
            .offset(
                x = if(isWeekView && dayIndex != -1) (columnWidth * dayIndex) else 0.dp,
                y = topOffset
            )
            .width(if(isWeekView) (columnWidth - 2.dp) else gridWidth - 4.dp)
            .height(height)
            .padding(2.dp)
            .clip(RoundedCornerShape(8.dp))
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
                            if (p.size < 2) return time
                            var h = p[0].toIntOrNull() ?: 0
                            val m = p[1].toIntOrNull() ?: 0
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
