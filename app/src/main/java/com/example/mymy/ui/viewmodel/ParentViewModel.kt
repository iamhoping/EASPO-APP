package com.example.mymy.ui.viewmodel

import android.util.Log
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
import kotlinx.coroutines.launch

class ParentViewModel : ViewModel() {
    var children by mutableStateOf<List<User>>(emptyList())
    var selectedChildId by mutableStateOf<String?>(null)
    
    var childAttendance by mutableStateOf<List<Attendance>>(emptyList())
    var childGrades by mutableStateOf<List<Grade>>(emptyList())
    var childSchedule by mutableStateOf<List<Schedule>>(emptyList())

    var attendanceRate by mutableStateOf(0)
    var averageGrade by mutableStateOf(0.0)
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun fetchChildren() {
        val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id ?: return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                loadChildrenAndData(userId)
            } catch (e: Exception) {
                Log.e("ParentVM", "Error in fetchChildren", e)
                errorMessage = e.message ?: "Failed to fetch children"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun loadChildrenAndData(userId: String) {
        Log.d("ParentVM", "Loading children for parent: $userId")
        
        val parentProfile = try {
            SupabaseConfig.client.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<User>()
        } catch (e: Exception) { 
            Log.e("ParentVM", "Error fetching parent profile", e)
            null 
        }

        val linkedChildren = try {
            SupabaseConfig.client.postgrest["profiles"]
                .select {
                    filter { eq("parent_id", userId) }
                }.decodeList<User>()
        } catch (e: Exception) { 
            Log.e("ParentVM", "Error decoding linked children", e)
            errorMessage = "Data error: ${e.localizedMessage}"
            emptyList() 
        }

        val specificChild = if (parentProfile?.childId != null) {
            try {
                val student = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("student_id", parentProfile.childId) }
                    }.decodeSingleOrNull<User>()
                
                if (student != null && student.parentId != userId) {
                    SupabaseConfig.client.postgrest["profiles"].update(
                        mapOf("parent_id" to userId)
                    ) {
                        filter { eq("id", student.id) }
                    }
                }
                student
            } catch (e: Exception) { 
                Log.e("ParentVM", "Error fetching specific child", e)
                null 
            }
        } else null

        val matchedByGuardianName = if (linkedChildren.isEmpty() && specificChild == null && parentProfile?.name != null) {
            try {
                SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { 
                            and {
                                eq("role", "STUDENT")
                                eq("guardian_name", parentProfile.name)
                            }
                        }
                    }.decodeList<User>()
            } catch (e: Exception) { emptyList() }
        } else emptyList()

        val allChildren = (linkedChildren + listOfNotNull(specificChild) + matchedByGuardianName).distinctBy { it.id }
        
        Log.d("ParentVM", "Found ${allChildren.size} children. Current count: ${children.size}")
        
        // Only update if we actually found children OR if we are explicitly trying to clear (which we aren't here)
        // This prevents the UI from flickering back to "Connect Student" if a background refresh temporarily returns empty
        if (allChildren.isNotEmpty() || children.isEmpty()) {
            children = allChildren
            if (children.isNotEmpty()) {
                val childToSelectId = selectedChildId ?: children[0].id
                selectedChildId = childToSelectId
                fetchChildDataInternal(childToSelectId)
            }
        } else {
            Log.w("ParentVM", "Background refresh returned empty children list, but we already have ${children.size} linked. Ignoring empty result.")
        }
    }

    fun linkStudent(studentCustomId: String) {
        val currentUser = SupabaseConfig.client.auth.currentUserOrNull()
        val userId = currentUser?.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // 1. Find the student
                val student = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("student_id", studentCustomId) }
                    }.decodeSingleOrNull<User>()

                if (student == null) {
                    errorMessage = "Student with ID $studentCustomId not found."
                    return@launch
                }

                // 2. Link the student to this parent
                SupabaseConfig.client.postgrest["profiles"].update(
                    mapOf("parent_id" to userId)
                ) {
                    filter { eq("id", student.id) }
                }

                // 3. Update the parent profile's child_id to match this ID (for future auto-linking)
                SupabaseConfig.client.postgrest["profiles"].update(
                    mapOf("child_id" to studentCustomId)
                ) {
                    filter { eq("id", userId) }
                }

                successMessage = "Successfully linked to ${student.name}!"
                
                // 4. Update local state immediately for instant UI transition
                val updatedChildren = (children + student).distinctBy { it.id }
                children = updatedChildren
                selectedChildId = student.id
                
                // 5. Fetch their data (Grades, Schedule, etc.)
                fetchChildDataInternal(student.id)
                
                // 6. Background refresh to ensure everything is in sync
                viewModelScope.launch {
                    kotlinx.coroutines.delay(1000)
                    loadChildrenAndData(userId)
                }

            } catch (e: Exception) {
                errorMessage = "Link failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun fetchChildData(studentId: String) {
        // Only skip if we are already viewing this child and have loaded data
        if (selectedChildId == studentId && (childSchedule.isNotEmpty() || childAttendance.isNotEmpty())) return
        
        selectedChildId = studentId
        
        // Reset state for new child to prevent showing stale data
        childAttendance = emptyList()
        childGrades = emptyList()
        childSchedule = emptyList()
        attendanceRate = 0
        averageGrade = 0.0

        viewModelScope.launch {
            isLoading = true
            try {
                fetchChildDataInternal(studentId)
            } catch (e: Exception) {
                errorMessage = e.message
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun fetchChildDataInternal(studentId: String) {
        // studentId here is the UUID (e.g., b2a37148...)
        val student = children.find { it.id == studentId }
        
        Log.d("ParentVM", "Fetching data for Student: ${student?.name} [UUID: $studentId]")

        // 1. Fetch Attendance - Use ONLY the UUID since the DB column is type UUID
        childAttendance = try {
            val res = SupabaseConfig.client.postgrest["attendance"]
                .select { 
                    filter { eq("student_id", studentId) } 
                }.decodeList<Attendance>()
            res.sortedByDescending { it.date }
        } catch (e: Exception) {
            Log.e("ParentVM", "Attendance fetch failed for UUID $studentId", e)
            emptyList()
        }

        // 2. Fetch Grades - Use ONLY the UUID
        childGrades = try {
            val res = SupabaseConfig.client.postgrest["grades"]
                .select {
                    filter { eq("student_id", studentId) }
                }.decodeList<Grade>()
            res
        } catch (e: Exception) {
            Log.e("ParentVM", "Grades fetch failed for UUID $studentId", e)
            emptyList()
        }

        // 3. Fetch Schedule via Enrollments
        childSchedule = try {
            val enrollments = SupabaseConfig.client.postgrest["enrollments"]
                .select {
                    filter { eq("student_id", studentId) }
                }.decodeList<Enrollment>()
            
            val scheduleIds = enrollments.map { it.scheduleId }
            val res = if (scheduleIds.isNotEmpty()) {
                val allSchedules = SupabaseConfig.client.postgrest["schedules"]
                    .select().decodeList<Schedule>()
                allSchedules.filter { it.id in scheduleIds }
            } else {
                emptyList()
            }
            
            Log.d("ParentVM", "Schedule found via enrollments: ${res.size} items for UUID $studentId")
            res
        } catch (e: Exception) {
            Log.e("ParentVM", "Schedule fetch failed for UUID $studentId", e)
            emptyList()
        }

        // Update stats
        attendanceRate = if (childAttendance.isNotEmpty()) {
            (childAttendance.count { it.status == "Present" }.toDouble() / childAttendance.size * 100).toInt()
        } else 0
        
        averageGrade = if (childGrades.isNotEmpty()) {
            childGrades.map { it.score }.average()
        } else 0.0
        
        Log.d("ParentVM", "Sync Complete: Att=${childAttendance.size}, Grades=${childGrades.size}, Sched=${childSchedule.size}")
    }
}
