package com.example.mymy.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.Attendance
import com.example.mymy.data.model.Enrollment
import com.example.mymy.data.model.RegisterUserRequest
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.functions.functions
import io.github.jan.supabase.postgrest.postgrest
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.coroutines.launch
import java.util.UUID

class SchoolAdminViewModel : ViewModel() {
    var allStudents by mutableStateOf<List<User>>(emptyList())
    var allTeachers by mutableStateOf<List<User>>(emptyList())
    var allParents by mutableStateOf<List<User>>(emptyList())
    var allUsers by mutableStateOf<List<User>>(emptyList())
    var allSchedules by mutableStateOf<List<Schedule>>(emptyList())
    var allEnrollments by mutableStateOf<List<Enrollment>>(emptyList())
    var allAttendance by mutableStateOf<List<Attendance>>(emptyList())
    
    var searchQuery by mutableStateOf("")
    var roleFilter by mutableStateOf<UserRole?>(null)

    val filteredUsers: List<User>
        get() = allUsers.filter { user ->
            val matchesQuery = (user.name ?: "").contains(searchQuery, ignoreCase = true) || 
                             (user.email ?: "").contains(searchQuery, ignoreCase = true)
            val matchesRole = roleFilter == null || user.role == roleFilter
            matchesQuery && matchesRole
        }
        
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch all profiles at once to reduce calls
                val allProfiles = SupabaseConfig.client.postgrest["profiles"]
                    .select()
                    .decodeList<User>()
                    .sortedBy { it.name }

                allUsers = allProfiles
                allStudents = allProfiles.filter { it.role == UserRole.STUDENT }
                allTeachers = allProfiles.filter { it.role == UserRole.TEACHER }
                allParents = allProfiles.filter { it.role == UserRole.PARENT }

                allSchedules = try {
                    SupabaseConfig.client.postgrest["schedules"].select().decodeList<Schedule>()
                } catch (e: Exception) {
                    Log.e("SchoolAdminVM", "Error fetching schedules", e)
                    emptyList()
                }

                allEnrollments = try {
                    SupabaseConfig.client.postgrest["enrollments"].select().decodeList<Enrollment>()
                } catch (e: Exception) {
                    Log.e("SchoolAdminVM", "Error fetching enrollments", e)
                    emptyList()
                }

                allAttendance = try {
                    SupabaseConfig.client.postgrest["attendance"].select().decodeList<Attendance>()
                } catch (e: Exception) {
                    Log.e("SchoolAdminVM", "Error fetching attendance", e)
                    emptyList()
                }
                    
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Fetch data failed", e)
                errorMessage = e.message ?: "Failed to fetch data"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveSchedules(schedules: List<Schedule>) {
        viewModelScope.launch {
            try {
                // Conflict detection check
                for (s in schedules) {
                    val conflict = allSchedules.find { existing ->
                        existing.day == s.day &&
                        existing.room == s.room &&
                        existing.id != s.id &&
                        isOverlapping(existing.startTime, existing.endTime, s.startTime, s.endTime)
                    }
                    if (conflict != null) {
                        errorMessage = "Conflict: Room ${s.room} is already booked on ${s.day} at ${conflict.startTime}-${conflict.endTime}"
                        return@launch
                    }
                    
                    val teacherConflict = allSchedules.find { existing ->
                        existing.day == s.day &&
                        existing.teacherId == s.teacherId &&
                        existing.id != s.id &&
                        isOverlapping(existing.startTime, existing.endTime, s.startTime, s.endTime)
                    }
                    if (teacherConflict != null) {
                        errorMessage = "Conflict: Teacher is already teaching on ${s.day} at ${teacherConflict.startTime}-${teacherConflict.endTime}"
                        return@launch
                    }
                }

                SupabaseConfig.client.postgrest["schedules"].insert(schedules)
                fetchData() 
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to save schedules"
            }
        }
    }

    private fun isOverlapping(s1: String?, e1: String?, s2: String?, e2: String?): Boolean {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false
        
        // Simple string comparison for HH:mm:ss
        // Standard overlap: (StartA < EndB) and (EndA > StartB)
        // This handles:
        // 1. A contains B
        // 2. B contains A
        // 3. A starts before B and ends after B starts
        // 4. B starts before A and ends after A starts
        return s1 < e2 && e1 > s2
    }

    fun saveSchedule(schedule: Schedule) {
        saveSchedules(listOf(schedule))
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            try {
                // Also delete related enrollments
                try {
                    SupabaseConfig.client.postgrest["enrollments"].delete {
                        filter { eq("schedule_id", id) }
                    }
                } catch (e: Exception) {
                    Log.w("SchoolAdminVM", "Could not delete enrollments for schedule $id", e)
                }

                SupabaseConfig.client.postgrest["schedules"].delete { filter { eq("id", id) } }
                fetchData()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to delete schedule"
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            try {
                SupabaseConfig.client.postgrest["profiles"].delete { filter { eq("id", id) } }
                fetchData()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to delete user"
            }
        }
    }

    fun registerUser(
        name: String,
        email: String,
        password: String,
        role: UserRole,
        studentId: String? = null,
        teacherId: String? = null,
        childId: String? = null,
        parentId: String? = null,
        gender: String? = null,
        contact: String? = null,
        address: String? = null,
        guardianName: String? = null,
        gradeLevel: String? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Automatically generate unique IDs if not provided
                val finalStudentId = if (role == UserRole.STUDENT) {
                    if (studentId.isNullOrBlank()) "STD-${UUID.randomUUID().toString().substring(0, 8).uppercase()}" else studentId
                } else null

                val finalTeacherId = if (role == UserRole.TEACHER) {
                    if (teacherId.isNullOrBlank()) "TCH-${UUID.randomUUID().toString().substring(0, 8).uppercase()}" else teacherId
                } else null

                val finalParentId = if (role == UserRole.PARENT && parentId.isNullOrBlank()) {
                    "PRN-${UUID.randomUUID().toString().substring(0, 8).uppercase()}"
                } else {
                    parentId
                }

                val request = RegisterUserRequest(
                    email = email,
                    password = password,
                    name = name,
                    role = role.name,
                    studentId = finalStudentId,
                    teacherId = finalTeacherId,
                    childId = if (role == UserRole.PARENT) childId else null,
                    parentId = finalParentId,
                    gender = gender,
                    contact = contact,
                    address = address,
                    guardianName = guardianName,
                    gradeLevel = gradeLevel
                )
                
                // Debug log to verify payload
                val jsonPayload = Json.encodeToString(request)
                Log.d("SchoolAdminVM", "Sending Registration Payload: $jsonPayload")
                
                // Call the Supabase Edge Function
                Log.d("SchoolAdminVM", "Invoking rapid-processor for: ${request.email}")
                val response = SupabaseConfig.client.functions.invoke("rapid-processor", request)
                Log.d("SchoolAdminVM", "Edge function response: ${response.bodyAsText()}")
                
                fetchData()
            } catch (e: Exception) {
                errorMessage = "Failed to create user: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun saveScheduleWithEnrollments(schedule: Schedule, studentIds: List<String>) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // 1. Conflict Detection
                val conflict = allSchedules.find { existing ->
                    existing.day == schedule.day &&
                    existing.room == schedule.room &&
                    existing.id != schedule.id &&
                    isOverlapping(existing.startTime, existing.endTime, schedule.startTime, schedule.endTime)
                }
                if (conflict != null) {
                    errorMessage = "Room Conflict: ${schedule.room} is occupied on ${schedule.day} at ${conflict.startTime}-${conflict.endTime}"
                    isLoading = false
                    return@launch
                }

                val teacherConflict = allSchedules.find { existing ->
                    existing.day == schedule.day &&
                    existing.teacherId == schedule.teacherId &&
                    existing.id != schedule.id &&
                    isOverlapping(existing.startTime, existing.endTime, schedule.startTime, schedule.endTime)
                }
                if (teacherConflict != null) {
                    errorMessage = "Teacher Conflict: Assigned teacher has another class at this time (${teacherConflict.startTime}-${teacherConflict.endTime})"
                    isLoading = false
                    return@launch
                }

                // 2. Save Schedule Record
                val masterSchedule = schedule
                val response = if (masterSchedule.id == null) {
                    SupabaseConfig.client.postgrest["schedules"].insert(masterSchedule) { select() }
                } else {
                    SupabaseConfig.client.postgrest["schedules"].update(masterSchedule) {
                        filter { eq("id", masterSchedule.id) }
                        select()
                    }
                }
                
                val savedSchedule = response.decodeSingle<Schedule>()
                val scheduleId = savedSchedule.id ?: throw Exception("Failed to retrieve saved schedule ID")

                // 3. Update Enrollments
                // Delete existing ones
                SupabaseConfig.client.postgrest["enrollments"].delete {
                    filter { eq("schedule_id", scheduleId) }
                }
                
                // Insert new ones
                if (studentIds.isNotEmpty()) {
                    val enrollments = studentIds.map { Enrollment(scheduleId = scheduleId, studentId = it) }
                    SupabaseConfig.client.postgrest["enrollments"].insert(enrollments)
                }
                
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Bulk enrollment failed", e)
                errorMessage = "Bulk enrollment failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
