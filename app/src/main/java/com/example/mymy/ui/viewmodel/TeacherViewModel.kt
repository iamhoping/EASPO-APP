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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TeacherViewModel : ViewModel() {
    var students by mutableStateOf<List<User>>(emptyList())
    var scheduleList by mutableStateOf<List<Schedule>>(emptyList())
    var enrollments by mutableStateOf<List<Enrollment>>(emptyList())
    var userProfile by mutableStateOf<User?>(null)
    var studentGrades by mutableStateOf<Map<String, List<Grade>>>(emptyMap()) // studentId -> list of grades
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)

    fun fetchData() {
        val currentUser = SupabaseConfig.client.auth.currentUserOrNull()
        val userId = currentUser?.id ?: return
        
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                Log.d("TeacherVM", "[v3] Fetching data for teacher: $userId")
                
                // 1. Fetch Teacher Profile
                userProfile = try {
                    val profile = SupabaseConfig.client.postgrest["profiles"]
                        .select {
                            filter { eq("id", userId) }
                        }.decodeSingleOrNull<User>()
                    Log.d("TeacherVM", "Profile fetch success: ${profile?.name}, Role: ${profile?.role}")
                    profile
                } catch (e: Exception) { 
                    Log.e("TeacherVM", "Error fetching teacher profile", e)
                    null 
                }

                val customTeacherId = userProfile?.teacherId
                Log.d("TeacherVM", "Teacher custom ID: $customTeacherId")

                // 2. Fetch Teacher's Schedule
                val fetchedSchedules = try {
                    Log.d("TeacherVM", "Querying schedules...")
                    // Fetch all and filter locally to avoid 22P02 UUID/String mismatch errors on server
                    val allSchedules = SupabaseConfig.client.postgrest["schedules"]
                        .select().decodeList<Schedule>()
                    
                    allSchedules.filter { 
                        it.teacherId == userId || (customTeacherId != null && it.teacherId == customTeacherId)
                    }
                } catch (e: Exception) { 
                    Log.e("TeacherVM", "Error fetching schedules", e)
                    emptyList<Schedule>() 
                }
                scheduleList = fetchedSchedules
                Log.d("TeacherVM", "Matched ${fetchedSchedules.size} schedules")

                // 3. Fetch Enrollments for these schedules (Plan A)
                val scheduleIds = fetchedSchedules.mapNotNull { it.id }
                val enrollments = if (scheduleIds.isNotEmpty()) {
                    try {
                        Log.d("TeacherVM", "Fetching enrollments for schedule IDs: $scheduleIds")
                        SupabaseConfig.client.postgrest["enrollments"]
                            .select().decodeList<Enrollment>()
                            .filter { it.scheduleId in scheduleIds }
                    } catch (e: Exception) {
                        Log.e("TeacherVM", "Error fetching enrollments", e)
                        emptyList()
                    }
                } else emptyList()
                
                val studentIdsFromEnrollments = enrollments.map { it.studentId }.toSet()
                Log.d("TeacherVM", "Found ${enrollments.size} enrollments, unique student IDs: $studentIdsFromEnrollments")

                // 4. Fetch Student Profiles
                val finalStudents = try {
                    Log.d("TeacherVM", "Querying profiles for enrolled students...")
                    
                    val enrolledStudents = if (studentIdsFromEnrollments.isNotEmpty()) {
                        SupabaseConfig.client.postgrest["profiles"]
                            .select {
                                filter { eq("role", "STUDENT") }
                            }.decodeList<User>().filter { student ->
                                student.id in studentIdsFromEnrollments
                            }
                    } else emptyList()

                    enrolledStudents.distinctBy { it.id }
                } catch (e: Exception) { 
                    Log.e("TeacherVM", "Error fetching students", e)
                    emptyList<User>() 
                }
                
                students = finalStudents
                Log.d("TeacherVM", "Resolved ${students.size} assigned students")
                
                if (students.isNotEmpty()) {
                    fetchGradesForStudents(students.map { it.id })
                }

            } catch (e: Exception) {
                Log.e("TeacherVM", "Global fetch error", e)
                errorMessage = "Failed to load: ${e.message}"
            } finally {
                isLoading = false
            }
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

    fun markAttendance(studentId: String, status: String, subject: String) {
        val teacherUuid = userProfile?.id ?: return
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val currentDate = sdf.format(Date())
                val attendance = Attendance(
                    studentId = studentId,
                    teacherId = teacherUuid,
                    date = currentDate,
                    status = status,
                    subject = subject
                )
                SupabaseConfig.client.postgrest["attendance"].insert(attendance)
                successMessage = "Attendance marked: $status"
            } catch (e: Exception) {
                Log.e("TeacherVM", "Error marking attendance", e)
                errorMessage = e.message ?: "Failed to mark attendance"
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchGradesForStudents(studentIds: List<String>) {
        if (studentIds.isEmpty()) return
        viewModelScope.launch {
            try {
                val allGrades = SupabaseConfig.client.postgrest["grades"]
                    .select().decodeList<Grade>()
                
                studentGrades = allGrades.filter { it.studentId in studentIds }
                    .groupBy { it.studentId }
            } catch (e: Exception) {
                Log.e("TeacherVM", "Error fetching grades", e)
            }
        }
    }

    fun uploadGrade(studentId: String, subject: String, score: Double, remarks: String? = null) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                performGradeUpload(studentId, subject, score, remarks)
                successMessage = "Grade saved successfully!"
                fetchGradesForStudents(students.map { it.id })
            } catch (e: Exception) {
                Log.e("TeacherVM", "Error uploading grade", e)
                errorMessage = e.message ?: "Failed to upload grade"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun performGradeUpload(studentId: String, subject: String, score: Double, remarks: String?) {
        val teacherId = userProfile?.id ?: throw Exception("Teacher profile not loaded")
        
        val grade = Grade(
            studentId = studentId,
            teacherId = teacherId,
            subject = subject,
            score = score,
            remarks = remarks,
            createdAt = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )
        
        // Check if grade already exists for this student and subject
        val existingGrades = studentGrades[studentId] ?: emptyList()
        val existingGrade = existingGrades.find { it.subject == subject }

        if (existingGrade != null) {
            SupabaseConfig.client.postgrest["grades"].update(
                mapOf(
                    "score" to score,
                    "remarks" to remarks,
                    "teacher_id" to teacherId,
                    "created_at" to grade.createdAt
                )
            ) {
                filter { eq("id", existingGrade.id ?: -1) }
            }
        } else {
            SupabaseConfig.client.postgrest["grades"].insert(grade)
        }
    }

    fun bulkSaveGrades(gradesToSave: List<Grade>) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                gradesToSave.forEach { grade ->
                    performGradeUpload(grade.studentId, grade.subject, grade.score, grade.remarks)
                }
                successMessage = "All ${gradesToSave.size} grades saved successfully!"
                fetchGradesForStudents(students.map { it.id })
            } catch (e: Exception) {
                Log.e("TeacherVM", "Bulk save error", e)
                errorMessage = "Bulk save failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
