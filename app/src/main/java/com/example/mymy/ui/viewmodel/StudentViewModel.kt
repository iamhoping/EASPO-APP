package com.example.mymy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.Attendance
import com.example.mymy.data.model.Enrollment
import com.example.mymy.data.model.Grade
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.Section
import com.example.mymy.data.model.StudentQrSession
import com.example.mymy.data.model.User
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import java.util.TimeZone

class StudentViewModel : ViewModel() {
    var attendanceList by mutableStateOf<List<Attendance>>(emptyList())
    var gradesList by mutableStateOf<List<Grade>>(emptyList())
    var scheduleList by mutableStateOf<List<Schedule>>(emptyList())
    var teachersList by mutableStateOf<List<User>>(emptyList())
    var sectionNames by mutableStateOf<Map<Long, String>>(emptyMap())
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var userProfile by mutableStateOf<User?>(null)

    // Dynamic QR Session
    var qrSession by mutableStateOf<StudentQrSession?>(null)
    var qrCountdown by mutableStateOf(0)
    var qrError by mutableStateOf<String?>(null)
    private var qrJob: Job? = null
    private var countdownJob: Job? = null

    val attendanceBySchedule: Map<Long, List<Attendance>>
        get() = attendanceList.groupBy { it.scheduleId ?: -1L }

    val overallAttendance: String
        get() {
            if (attendanceList.isEmpty()) return "0%"
            val validRecords = attendanceList.filter { it.status != "Scheduled" } 
            if (validRecords.isEmpty()) return "0%"
            
            val presentCount = validRecords.count { 
                it.status.equals("Present", ignoreCase = true) || 
                it.status.equals("Late", ignoreCase = true) 
            }
            val percentage = (presentCount.toDouble() / validRecords.size) * 100
            return "${percentage.toInt()}%"
        }

    fun fetchData() {
        val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id ?: return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                userProfile = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<User>()

                fetchAttendance(userId)
                subscribeToAttendance(userId)
                fetchGrades(userId)
                subscribeToGrades(userId)

                scheduleList = try {
                    val enrollments = SupabaseConfig.client.postgrest["enrollments"]
                        .select {
                            filter { eq("student_id", userId) }
                        }.decodeList<Enrollment>()
                    
                    val scheduleIds = enrollments.map { it.scheduleId }
                    val sectionId = userProfile?.sectionId

                    val allSchedules = SupabaseConfig.client.postgrest["schedules"]
                        .select().decodeList<Schedule>()
                    
                    allSchedules.filter { 
                        it.id in scheduleIds || (sectionId != null && it.sectionId == sectionId)
                    }
                } catch (e: Exception) { emptyList() }

                teachersList = try {
                    SupabaseConfig.client.postgrest["profiles"]
                        .select {
                            filter { eq("role", "TEACHER") }
                        }.decodeList<User>()
                } catch (e: Exception) { emptyList() }

                try {
                    val sections = SupabaseConfig.client.postgrest["sections"]
                        .select().decodeList<Section>()
                    sectionNames = sections.associate { (it.id ?: 0L) to (it.name ?: "Unknown Section") }
                } catch (e: Exception) { }
                    
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch data"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchAttendance(userId: String) {
        try {
            attendanceList = SupabaseConfig.client.postgrest["attendance"]
                .select {
                    filter { eq("student_id", userId) }
                }.decodeList<Attendance>()
        } catch (e: Exception) { }
    }

    private fun subscribeToAttendance(userId: String) {
        val channel = SupabaseConfig.client.channel("attendance_changes")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "attendance"
            filter(FilterOperation("student_id", FilterOperator.EQ, userId))
        }

        flow.onEach { 
            fetchAttendance(userId)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            channel.subscribe()
        }
    }

    private suspend fun fetchGrades(userId: String) {
        try {
            val grades = SupabaseConfig.client.postgrest["grades"]
                .select {
                    filter { eq("student_id", userId) }
                }.decodeList<Grade>()
            gradesList = grades.sortedByDescending { it.createdAt }
        } catch (e: Exception) { }
    }

    private fun subscribeToGrades(userId: String) {
        val channel = SupabaseConfig.client.channel("grades_changes")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "grades"
            filter(FilterOperation("student_id", FilterOperator.EQ, userId))
        }
        
        flow.onEach { 
            fetchGrades(userId)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            channel.subscribe()
        }
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            isLoading = true
            try {
                SupabaseConfig.client.postgrest["profiles"].update(updatedUser) {
                    filter { eq("id", updatedUser.id) }
                }
                userProfile = updatedUser
            } catch (e: Exception) {
                errorMessage = "Update failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun startQrSession() {
        if (qrJob?.isActive == true) return
        qrError = null
        generateNewQrSession()
    }

    fun stopQrSession() {
        qrJob?.cancel()
        countdownJob?.cancel()
        qrSession = null
        qrCountdown = 0
    }

    private fun generateNewQrSession() {
        qrJob?.cancel()
        qrJob = viewModelScope.launch {
            try {
                // First check current user status
                val currentUser = SupabaseConfig.client.auth.currentUserOrNull()
                if (currentUser == null) {
                    qrError = "Authentication failed. Please log in again."
                    return@launch
                }
                
                android.util.Log.d("StudentVM", "Generating QR for user: ${currentUser.id}")
                
                val result = SupabaseConfig.client.postgrest.rpc("generate_student_qr_session")
                    .decodeAs<StudentQrSession>()
                
                qrSession = result
                qrError = null
                
                startCountdown(result)
            } catch (e: Exception) {
                android.util.Log.e("StudentVM", "QR Generation failed", e)
                val detailedError = when {
                    e.message?.contains("42501") == true -> "Unauthorized: Ensure you are logged in as an active student."
                    e.message?.contains("connection") == true -> "Network error. Please check your internet."
                    else -> e.message ?: "Unknown error"
                }
                qrError = "Failed to generate QR Code: $detailedError"
                qrSession = null
            }
        }
    }

    private fun startCountdown(session: StudentQrSession) {
        countdownJob?.cancel()
        countdownJob = viewModelScope.launch {
            try {
                val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                sdf.timeZone = TimeZone.getTimeZone("UTC")
                
                val cleanExp = session.expiresAt.substring(0, 19)
                val expiryDate = sdf.parse(cleanExp) ?: return@launch
                
                val cleanCreated = session.createdAt?.substring(0, 19)
                val createdDate = cleanCreated?.let { sdf.parse(it) }
                
                // Use server-provided timestamps to determine the duration
                // This avoids reliance on local device time for the authority of the session length
                val durationSeconds = if (createdDate != null) {
                    ((expiryDate.time - createdDate.time) / 1000).toInt()
                } else {
                    30 // fallback
                }

                var secondsLeft = durationSeconds
                while (secondsLeft >= 0) {
                    qrCountdown = secondsLeft
                    if (secondsLeft == 0) {
                        generateNewQrSession()
                        break
                    }
                    delay(1000)
                    secondsLeft--
                }
            } catch (e: Exception) {
                android.util.Log.e("StudentVM", "Countdown error", e)
                delay(2000)
                generateNewQrSession()
            }
        }
    }
}
