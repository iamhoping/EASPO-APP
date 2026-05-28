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
import com.example.mymy.data.model.Section
import com.example.mymy.data.model.Subject
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
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
    var allSections by mutableStateOf<List<Section>>(emptyList())
    var allSubjects by mutableStateOf<List<Subject>>(emptyList())
    var userProfile by mutableStateOf<User?>(null)
    
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
    var successMessage by mutableStateOf<String?>(null)

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                // Fetch current user profile
                val currentUser = SupabaseConfig.client.auth.retrieveUserForCurrentSession(false)
                userProfile = SupabaseConfig.client.postgrest["profiles"]
                    .select { filter { eq("id", currentUser.id) } }
                    .decodeSingleOrNull<User>()

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

                allSections = try {
                    SupabaseConfig.client.postgrest["sections"].select().decodeList<Section>().sortedBy { it.name }
                } catch (e: Exception) {
                    Log.e("SchoolAdminVM", "Error fetching sections", e)
                    emptyList()
                }

                allSubjects = try {
                    SupabaseConfig.client.postgrest["subjects"].select().decodeList<Subject>().sortedBy { it.name }
                } catch (e: Exception) {
                    Log.e("SchoolAdminVM", "Error fetching subjects", e)
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

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // Find the schedule first
                val scheduleToDelete = allSchedules.find { it.id == id }
                
                // 1. Delete related attendance
                try {
                    SupabaseConfig.client.postgrest["attendance"].delete {
                        filter { eq("schedule_id", id) }
                    }
                } catch (e: Exception) {
                    Log.w("SchoolAdminVM", "Could not delete attendance for schedule $id", e)
                }

                // 2. Delete related enrollments
                try {
                    SupabaseConfig.client.postgrest["enrollments"].delete {
                        filter { eq("schedule_id", id) }
                    }
                } catch (e: Exception) {
                    Log.w("SchoolAdminVM", "Could not delete enrollments for schedule $id", e)
                }

                // 3. Delete the schedule
                SupabaseConfig.client.postgrest["schedules"].delete { filter { eq("id", id) } }

                // 3. Update section/student status if needed
                scheduleToDelete?.sectionId?.let { sId ->
                    val otherSchedules = SupabaseConfig.client.postgrest["schedules"]
                        .select { filter { eq("section_id", sId) } }
                        .decodeList<Schedule>()
                    
                    if (otherSchedules.isEmpty()) {
                        SupabaseConfig.client.postgrest["sections"].update(mapOf("status" to "Pending")) {
                            filter { eq("id", sId) }
                        }
                        SupabaseConfig.client.postgrest["profiles"].update(mapOf("status" to "Enrolled")) {
                            filter { eq("section_id", sId) }
                        }
                    }
                }

                successMessage = "Schedule deleted successfully"
                fetchData()
            } catch (e: Exception) {
                errorMessage = e.message ?: "Failed to delete schedule"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteUser(id: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // 1. Delete Attendance
                try {
                    SupabaseConfig.client.postgrest["attendance"].delete {
                        filter {
                            or {
                                eq("student_id", id)
                                eq("teacher_id", id)
                            }
                        }
                    }
                } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup attendance failed", e) }
                
                // 2. Delete Grades
                try {
                    SupabaseConfig.client.postgrest["grades"].delete {
                        filter {
                            or {
                                eq("student_id", id)
                                eq("teacher_id", id)
                            }
                        }
                    }
                } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup grades failed", e) }
                
                // 3. Delete Enrollments
                try {
                    SupabaseConfig.client.postgrest["enrollments"].delete {
                        filter { eq("student_id", id) }
                    }
                } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup enrollments failed", e) }

                // 4. Nullify Teacher/Adviser/Parent/Child references
                try {
                    SupabaseConfig.client.postgrest["schedules"].update(mapOf("teacher_id" to null)) {
                        filter { eq("teacher_id", id) }
                    }
                    SupabaseConfig.client.postgrest["sections"].update(mapOf("adviser_id" to null)) {
                        filter { eq("adviser_id", id) }
                    }
                    SupabaseConfig.client.postgrest["profiles"].update(mapOf("child_id" to null)) {
                        filter { eq("child_id", id) }
                    }
                    SupabaseConfig.client.postgrest["profiles"].update(mapOf("parent_id" to null)) {
                        filter { eq("parent_id", id) }
                    }
                } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup references failed", e) }
                
                // 5. Delete Profile
                SupabaseConfig.client.postgrest["profiles"].delete { filter { eq("id", id) } }
                
                successMessage = "User deleted successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Delete user failed", e)
                errorMessage = "Failed to delete user: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun createSection(name: String, gradeLevel: String, adviserId: String? = null, studentIds: List<String> = emptyList()) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val section = Section(name = name, gradeLevel = gradeLevel, adviserId = adviserId)
                val response = SupabaseConfig.client.postgrest["sections"].insert(section) {
                    select()
                }.decodeSingle<Section>()
                
                val newId = response.id
                if (newId != null && studentIds.isNotEmpty()) {
                    SupabaseConfig.client.postgrest["profiles"].update(mapOf("section_id" to newId)) {
                        filter { isIn("id", studentIds) }
                    }
                }
                successMessage = "Section created successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Create section failed", e)
                errorMessage = "Failed to create section: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateSection(section: Section, studentIds: List<String>) {
        val sectionId = section.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                SupabaseConfig.client.postgrest["sections"].update(section) {
                    filter { eq("id", sectionId) }
                }
                
                SupabaseConfig.client.postgrest["profiles"].update(mapOf("section_id" to null)) {
                    filter { eq("section_id", sectionId) }
                }

                if (studentIds.isNotEmpty()) {
                    SupabaseConfig.client.postgrest["profiles"].update(mapOf("section_id" to sectionId)) {
                        filter { isIn("id", studentIds) }
                    }
                }
                successMessage = "Section updated successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Update section failed", e)
                errorMessage = "Failed to update section: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteSection(sectionId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // 1. Detach all students
                SupabaseConfig.client.postgrest["profiles"].update(mapOf("section_id" to null)) {
                    filter { eq("section_id", sectionId) }
                }
                
                // 2. Find and Delete related schedules and their enrollments/attendance
                val schedules = SupabaseConfig.client.postgrest["schedules"]
                    .select { filter { eq("section_id", sectionId) } }
                    .decodeList<Schedule>()
                
                val scheduleIds = schedules.mapNotNull { it.id }
                if (scheduleIds.isNotEmpty()) {
                    try {
                        SupabaseConfig.client.postgrest["attendance"].delete {
                            filter { isIn("schedule_id", scheduleIds) }
                        }
                    } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup section attendance failed", e) }

                    SupabaseConfig.client.postgrest["enrollments"].delete {
                        filter { isIn("schedule_id", scheduleIds) }
                    }
                    SupabaseConfig.client.postgrest["schedules"].delete {
                        filter { isIn("id", scheduleIds) }
                    }
                }

                // 3. Delete the section
                SupabaseConfig.client.postgrest["sections"].delete {
                    filter { eq("id", sectionId) }
                }
                
                successMessage = "Section deleted successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Delete section failed", e)
                errorMessage = "Failed to delete section: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun createSubject(subject: Subject) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                SupabaseConfig.client.postgrest["subjects"].insert(subject)
                successMessage = "Subject created successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Create subject failed", e)
                errorMessage = "Failed to create subject: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateSubject(subject: Subject) {
        val id = subject.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                SupabaseConfig.client.postgrest["subjects"].update(subject) {
                    filter { eq("id", id) }
                }
                successMessage = "Subject updated successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Update subject failed", e)
                errorMessage = "Failed to update subject: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deleteSubject(id: Int) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // 1. Find and Delete related schedules and their enrollments/attendance
                val schedules = SupabaseConfig.client.postgrest["schedules"]
                    .select { filter { eq("subject_id", id) } }
                    .decodeList<Schedule>()
                
                val scheduleIds = schedules.mapNotNull { it.id }
                if (scheduleIds.isNotEmpty()) {
                    try {
                        SupabaseConfig.client.postgrest["attendance"].delete {
                            filter { isIn("schedule_id", scheduleIds) }
                        }
                    } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup subject schedule attendance failed", e) }

                    SupabaseConfig.client.postgrest["enrollments"].delete {
                        filter { isIn("schedule_id", scheduleIds) }
                    }
                    SupabaseConfig.client.postgrest["schedules"].delete {
                        filter { isIn("id", scheduleIds) }
                    }
                }

                // 2. Cleanup attendance and grades tied directly to subject_id
                try {
                    SupabaseConfig.client.postgrest["attendance"].delete { filter { eq("subject_id", id) } }
                    SupabaseConfig.client.postgrest["grades"].delete { filter { eq("subject_id", id) } }
                } catch (e: Exception) { Log.e("SchoolAdminVM", "Cleanup subject records failed", e) }

                // 3. Delete the subject
                SupabaseConfig.client.postgrest["subjects"].delete {
                    filter { eq("id", id) }
                }
                successMessage = "Subject deleted successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Delete subject failed", e)
                errorMessage = "Failed to delete subject: ${e.message}"
            } finally {
                isLoading = false
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
        guardianEmail: String? = null,
        gradeLevel: String? = null
    ) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
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
                    guardianEmail = guardianEmail,
                    gradeLevel = gradeLevel
                )
                
                SupabaseConfig.client.functions.invoke("rapid-processor", request)
                successMessage = "User registered successfully"
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
            successMessage = null
            try {
                // Conflict Detection
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

                savedSchedule.sectionId?.let { sId ->
                    SupabaseConfig.client.postgrest["sections"].update(mapOf("status" to "Scheduled")) {
                        filter { eq("id", sId) }
                    }
                    SupabaseConfig.client.postgrest["profiles"].update(mapOf("status" to "Assigned")) {
                        filter { eq("section_id", sId) }
                    }
                }

                SupabaseConfig.client.postgrest["enrollments"].delete {
                    filter { eq("schedule_id", scheduleId) }
                }
                
                if (studentIds.isNotEmpty()) {
                    val enrollments = studentIds.map { Enrollment(scheduleId = scheduleId, studentId = it) }
                    SupabaseConfig.client.postgrest["enrollments"].insert(enrollments)
                }
                
                successMessage = if (schedule.id == null) "Schedule created successfully" else "Schedule updated successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Bulk enrollment failed", e)
                errorMessage = "Bulk enrollment failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private fun isOverlapping(s1: String?, e1: String?, s2: String?, e2: String?): Boolean {
        if (s1 == null || e1 == null || s2 == null || e2 == null) return false
        return s1 < e2 && e1 > s2
    }

    fun addStudentToSection(studentId: String, sectionId: Long) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                SupabaseConfig.client.postgrest["profiles"].update(mapOf("section_id" to sectionId)) {
                    filter { eq("id", studentId) }
                }
                successMessage = "Student added to section successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Add student failed", e)
                errorMessage = "Failed to add student: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun removeStudentFromSection(studentId: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                SupabaseConfig.client.postgrest["profiles"].update(mapOf("section_id" to null)) {
                    filter { eq("id", studentId) }
                }
                successMessage = "Student removed from section successfully"
                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Remove student failed", e)
                errorMessage = "Failed to remove student: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updateProfile(updatedUser: User) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // Use a Map for the update to ensure null values (like section_id) are 
                // explicitly sent to Supabase, bypassing potential null-skipping in serialization.
                val updates = mutableMapOf<String, Any?>(
                    "name" to updatedUser.name,
                    "email" to updatedUser.email,
                    "contact" to updatedUser.contact,
                    "address" to updatedUser.address,
                    "gender" to updatedUser.gender,
                    "grade_level" to updatedUser.gradeLevel,
                    "department" to updatedUser.department,
                    "status" to updatedUser.status,
                    "section_id" to updatedUser.sectionId,
                    "guardian_email" to updatedUser.guardianEmail,
                    "student_id" to updatedUser.studentNo,
                    "teacher_id" to updatedUser.teacherId,
                    "parent_id" to updatedUser.parentId,
                    "child_id" to updatedUser.childId
                )
                
                updatedUser.role?.let { updates["role"] = it.name }

                SupabaseConfig.client.postgrest["profiles"].update(updates) {
                    filter { eq("id", updatedUser.id) }
                }
                
                // Only update local userProfile if it's the admin's own profile being edited
                if (userProfile?.id == updatedUser.id) {
                    userProfile = updatedUser
                    successMessage = "Profile updated successfully"
                } else {
                    successMessage = "Record for ${updatedUser.name ?: "user"} updated"
                }

                fetchData()
            } catch (e: Exception) {
                Log.e("SchoolAdminVM", "Update profile failed", e)
                errorMessage = "Failed to update profile: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
