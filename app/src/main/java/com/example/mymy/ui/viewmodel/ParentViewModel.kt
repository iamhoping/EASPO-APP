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
import com.example.mymy.data.model.UserRole
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
    var teachersList by mutableStateOf<List<User>>(emptyList())
    var selectedChildSection by mutableStateOf<com.example.mymy.data.model.Section?>(null)
    var sectionNames by mutableStateOf<Map<Long, String>>(emptyMap())

    var attendanceRate by mutableStateOf(0)
    var averageGrade by mutableStateOf(0.0)
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun fetchChildren() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            var currentUser = SupabaseConfig.client.auth.currentUserOrNull()
            if (currentUser == null) {
                Log.d("ParentVM", "Current user null, waiting for session...")
                kotlinx.coroutines.delay(1000)
                currentUser = SupabaseConfig.client.auth.currentUserOrNull()
            }
            
            val userId = currentUser?.id
            if (userId == null) {
                Log.e("ParentVM", "No user session found for Parent")
                isLoading = false
                return@launch
            }

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
        Log.d("ParentVM", "Starting Auto-Link check for Parent ID: $userId")
        
        // 1. Get Parent Profile
        val parentProfile = try {
            val profile = SupabaseConfig.client.postgrest["profiles"]
                .select { filter { eq("id", userId) } }
                .decodeSingleOrNull<User>()
            Log.d("ParentVM", "Parent Profile: ${profile?.name}, Email in DB: ${profile?.email}")
            profile
        } catch (e: Exception) { 
            Log.e("ParentVM", "Error fetching parent profile", e)
            null 
        }

        // 2. Get Parent Email (Verified Auth email is most reliable)
        val authUser = SupabaseConfig.client.auth.currentUserOrNull()
        val parentEmail = (authUser?.email ?: parentProfile?.email)?.trim()?.lowercase()
        
        Log.d("ParentVM", "Matching Key: '$parentEmail'")

        if (parentEmail == null) {
            Log.e("ParentVM", "No email found for parent account. Cannot proceed with linking.")
            children = emptyList()
            return
        }

        // 3. Find Children (Search by parent_id, guardian_email, or child_id)
        val matchedChildren = try {
            Log.d("ParentVM", "Querying potential students for Parent ID: $userId, Email: '$parentEmail', or ChildID: '${parentProfile?.childId}'...")
            
            // We search by:
            // 1. parent_id (already linked via UUID)
            // 2. guardian_email (automatic link candidate)
            // 3. student_id (matches parent's child_id field if set)
            val dbMatches = SupabaseConfig.client.postgrest["profiles"]
                .select {
                    filter {
                        or {
                            eq("parent_id", userId)
                            ilike("guardian_email", "%$parentEmail%")
                            parentProfile?.childId?.let { eq("student_id", it) }
                        }
                    }
                }.decodeList<User>()
            
            Log.d("ParentVM", "Database returned ${dbMatches.size} candidate profiles.")

            dbMatches.filter { profile ->
                // Flexible check for student status (role enum, role string, or studentNo presence)
                val isStudent = profile.role == UserRole.STUDENT || 
                                !profile.studentNo.isNullOrBlank() ||
                                profile.role?.name?.equals("STUDENT", ignoreCase = true) == true

                // Strict exact match after trimming whitespace in Kotlin
                val gEmail = profile.guardianEmail?.trim()?.lowercase()
                val isMatch = gEmail == parentEmail
                
                if (isStudent) {
                    Log.d("ParentVM", "Candidate Found: ${profile.name}, GuardianEmail: '${profile.guardianEmail}', Match: $isMatch")
                }
                isStudent && isMatch
            }
        } catch (e: Exception) {
            Log.e("ParentVM", "Linking search failed: ${e.message}", e)
            emptyList()
        }

        // 4. Update state with unique results
        children = matchedChildren.distinctBy { it.id }
        Log.d("ParentVM", "Final count of linked children: ${children.size}")

        if (children.isNotEmpty()) {
            // Safety check: ensure selectedChildId is still valid in the new list
            val childToSelectId = if (children.any { it.id == selectedChildId }) {
                selectedChildId!!
            } else {
                children[0].id
            }
            selectedChildId = childToSelectId
            fetchChildDataInternal(childToSelectId)
            
            // 5. Background sync for parent_id consistency
            children.forEach { student ->
                if (student.parentId != userId) {
                    viewModelScope.launch {
                        try {
                            Log.d("ParentVM", "Syncing parent_id in background for ${student.name}")
                            SupabaseConfig.client.postgrest["profiles"].update(
                                mapOf("parent_id" to userId)
                            ) {
                                filter { eq("id", student.id) }
                            }
                        } catch (e: Exception) {
                            Log.w("ParentVM", "Background auto-link update failed (check RLS): ${e.message}")
                        }
                    }
                }
            }
        } else {
            Log.w("ParentVM", "No students were linked. Possible reasons:")
            Log.w("ParentVM", "1. guardian_email in DB does not match '$parentEmail'")
            Log.w("ParentVM", "2. RLS policy on 'profiles' table blocks SELECT for this parent.")
            Log.w("ParentVM", "   REQUIRED SQL POLICY: CREATE POLICY \"Parents can view their children\" ON profiles FOR SELECT USING (auth.email() = guardian_email OR parent_id = auth.uid()::text);")
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
        
        Log.d("ParentVM", "Fetching detailed data for Student: ${student?.name} [UUID: $studentId]")

        // 0. Fetch Section Details
        student?.sectionId?.let { sId ->
            try {
                selectedChildSection = SupabaseConfig.client.postgrest["sections"]
                    .select { filter { eq("id", sId) } }
                    .decodeSingleOrNull<com.example.mymy.data.model.Section>()
            } catch (e: Exception) {
                Log.e("ParentVM", "Section fetch failed", e)
                selectedChildSection = null
            }
        } ?: run { selectedChildSection = null }

        // 1. Fetch Attendance
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

        // 3. Fetch Schedule via Enrollments AND Section
        childSchedule = try {
            val enrollments = SupabaseConfig.client.postgrest["enrollments"]
                .select {
                    filter { eq("student_id", studentId) }
                }.decodeList<Enrollment>()
            
            val scheduleIds = enrollments.map { it.scheduleId }
            val sectionId = student?.sectionId

            val allSchedules = SupabaseConfig.client.postgrest["schedules"]
                .select().decodeList<Schedule>()
            
            val res = allSchedules.filter { 
                it.id in scheduleIds || (sectionId != null && it.sectionId == sectionId)
            }
            
            Log.d("ParentVM", "Schedule found (Enr+Sec): ${res.size} items for UUID $studentId")
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

        // Fetch Teachers for Schedule
        teachersList = try {
            SupabaseConfig.client.postgrest["profiles"]
                .select {
                    filter { eq("role", "TEACHER") }
                }.decodeList<User>()
        } catch (e: Exception) { emptyList() }

        // Fetch Sections for name mapping
        try {
            val sections = SupabaseConfig.client.postgrest["sections"]
                .select().decodeList<com.example.mymy.data.model.Section>()
            sectionNames = sections.associate { (it.id ?: 0L) to (it.name ?: "Unknown Section") }
        } catch (e: Exception) {
            Log.e("ParentVM", "Error fetching sections", e)
        }
        
        Log.d("ParentVM", "Sync Complete: Att=${childAttendance.size}, Grades=${childGrades.size}, Sched=${childSchedule.size}")
    }
}
