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
import com.example.mymy.data.model.User
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.filter.FilterOperation
import io.github.jan.supabase.postgrest.query.filter.FilterOperator
import io.github.jan.supabase.realtime.PostgresAction
import io.github.jan.supabase.realtime.channel
import io.github.jan.supabase.realtime.postgresChangeFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class StudentViewModel : ViewModel() {
    var attendanceList by mutableStateOf<List<Attendance>>(emptyList())
    var gradesList by mutableStateOf<List<Grade>>(emptyList())
    var scheduleList by mutableStateOf<List<Schedule>>(emptyList())
    var teachersList by mutableStateOf<List<User>>(emptyList())
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var userProfile by mutableStateOf<User?>(null)

    fun fetchData() {
        val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id ?: return
        android.util.Log.d("StudentVM", "fetchData started for userId: $userId")
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch Profile
                userProfile = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<User>()
                android.util.Log.d("StudentVM", "Fetched profile: $userProfile")

                // Fetch Attendance
                attendanceList = try {
                    SupabaseConfig.client.postgrest["attendance"]
                        .select {
                            filter { eq("student_id", userId) }
                        }.decodeList<Attendance>()
                } catch (e: Exception) { emptyList() }

                // Fetch Grades
                fetchGrades(userId)
                subscribeToGrades(userId)

                // Fetch Schedule via Enrollments AND Section
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
                } catch (e: Exception) { 
                    android.util.Log.e("StudentVM", "Error fetching schedule", e)
                    emptyList() 
                }

                // Fetch Teachers for Schedule
                teachersList = try {
                    SupabaseConfig.client.postgrest["profiles"]
                        .select {
                            filter { eq("role", "TEACHER") }
                        }.decodeList<User>()
                } catch (e: Exception) { emptyList() }
                    
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to fetch data"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchGrades(userId: String) {
        try {
            val grades = SupabaseConfig.client.postgrest["grades"]
                .select {
                    filter { eq("student_id", userId) }
                }.decodeList<Grade>()
            android.util.Log.d("StudentVM", "Fetched ${grades.size} grades for $userId")
            grades.forEach { android.util.Log.d("StudentVM", "Grade: ${it.subject} = ${it.score}, student_id: ${it.studentId}") }
            
            gradesList = grades.sortedByDescending { it.createdAt }
        } catch (e: Exception) {
            android.util.Log.e("StudentVM", "Error fetching grades", e)
        }
    }

    private fun subscribeToGrades(userId: String) {
        android.util.Log.d("StudentVM", "Subscribing to grades for student_id: $userId")
        val channel = SupabaseConfig.client.channel("grades_changes")
        val flow = channel.postgresChangeFlow<PostgresAction>(schema = "public") {
            table = "grades"
            filter(FilterOperation("student_id", FilterOperator.EQ, userId))
        }
        
        flow.onEach { action ->
            android.util.Log.d("StudentVM", "REALTIME EVENT RECEIVED: $action")
            when (action) {
                is PostgresAction.Insert -> android.util.Log.d("StudentVM", "Insert: ${action.record}")
                is PostgresAction.Update -> android.util.Log.d("StudentVM", "Update: ${action.oldRecord} -> ${action.record}")
                is PostgresAction.Delete -> android.util.Log.d("StudentVM", "Delete: ${action.oldRecord}")
                else -> {}
            }
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
}
