package com.example.mymy.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.Attendance
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.User
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AttendanceViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    /**
     * Rules 2 & 3: Automatic Attendance Initialization & Student Check-In
     * This should be called when a student attempts to check in.
     */
    fun checkIn(student: User, schedule: Schedule) {
        val studentId = student.id
        val sectionId = schedule.sectionId ?: student.sectionId ?: return
        val scheduleId = schedule.id ?: return
        val currentDate = dateFormat.format(Date())

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // Rule 2 & 3 implementation: 
                // Any check-in (before, at, or after start time) results in "Present".
                // We ensure records are initialized if they don't exist.
                
                val existingRecord = SupabaseConfig.client.postgrest["attendance"]
                    .select {
                        filter {
                            eq("student_id", studentId)
                            eq("schedule_id", scheduleId)
                            eq("attendance_date", currentDate)
                        }
                    }.decodeSingleOrNull<Attendance>()

                if (existingRecord != null) {
                    if (existingRecord.status == "Present") {
                        // 6. Duplicate Protection
                        successMessage = "Attendance already recorded."
                        return@launch
                    } else {
                        // 3. Student Check-In (After start time / Updating existing Absent)
                        updateToPresent(existingRecord.id!!, studentId)
                    }
                } else {
                    // 3. Student Check-In (Before or Exactly at start time / record not yet initialized)
                    // We can just create/update it directly as Present.
                    initializeAndCheckIn(studentId, sectionId, scheduleId, currentDate)
                }
            } catch (e: Exception) {
                Log.e("AttendanceVM", "Check-in error", e)
                errorMessage = "Check-in failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun initializeAndCheckIn(studentId: String, sectionId: Long, scheduleId: Long, date: String) {
        // Rule 2: Automatic Attendance Initialization
        // In a real system, a cron job/trigger would do this.
        // Here, we check if initialization is needed for the section.
        val sectionAttendance = SupabaseConfig.client.postgrest["attendance"]
            .select {
                filter {
                    eq("section_id", sectionId)
                    eq("schedule_id", scheduleId)
                    eq("attendance_date", date)
                }
            }.decodeList<Attendance>()

        if (sectionAttendance.isEmpty()) {
            // Initialize for all students in the section
            val studentsInSection = SupabaseConfig.client.postgrest["profiles"]
                .select {
                    filter {
                        eq("section_id", sectionId)
                        eq("role", "STUDENT")
                    }
                }.decodeList<User>()

            val initialRecords = studentsInSection.map { s ->
                Attendance(
                    studentId = s.id,
                    sectionId = sectionId,
                    scheduleId = scheduleId,
                    date = date,
                    status = "Absent",
                    timeIn = null
                )
            }
            
            if (initialRecords.isNotEmpty()) {
                SupabaseConfig.client.postgrest["attendance"].insert(initialRecords)
            }
        }

        // Now perform the check-in for the current student
        val record = SupabaseConfig.client.postgrest["attendance"]
            .select {
                filter {
                    eq("student_id", studentId)
                    eq("schedule_id", scheduleId)
                    eq("attendance_date", date)
                }
            }.decodeSingleOrNull<Attendance>()

        record?.id?.let { updateToPresent(it, studentId) }
    }

    private suspend fun updateToPresent(attendanceId: Long, studentId: String) {
        val now = Date()
        val timestamp = dateTimeFormat.format(now)
        
        // 3. Student Check-In & 5. Real-Time Database Updates
        val updates = mapOf(
            "status" to "Present",
            "time_in" to timestamp,
            "updated_at" to timestamp
        )

        SupabaseConfig.client.postgrest["attendance"].update(updates) {
            filter { eq("id", attendanceId) }
        }
        successMessage = "Attendance marked: Present"
    }

    /**
     * 5. Real-Time Database Updates
     * Subscribes to attendance changes for a specific teacher's section/schedule.
     */
    fun subscribeToAttendance(sectionId: Long, scheduleId: Long, onUpdate: () -> Unit) {
        val channel = SupabaseConfig.client.channel("attendance_$scheduleId")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "attendance"
            // Filter is optional here as we might want to see all changes in the table 
            // and filter locally if Supabase doesn't support complex filters in realtime easily
        }

        flow.onEach {
            onUpdate()
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            channel.subscribe()
        }
    }
}
