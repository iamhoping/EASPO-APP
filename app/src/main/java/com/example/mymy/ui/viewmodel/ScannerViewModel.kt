package com.example.mymy.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.Attendance
import com.example.mymy.data.model.User
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class ScannerViewModel : ViewModel() {
    var userProfile by mutableStateOf<User?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var successMessage by mutableStateOf<String?>(null)
    var scanMode by mutableStateOf("Entrance") // "Entrance" or "Exit"

    init {
        fetchProfile()
    }

    private fun fetchProfile() {
        val currentUser = SupabaseConfig.client.auth.currentUserOrNull()
        val userId = currentUser?.id ?: return

        viewModelScope.launch {
            try {
                userProfile = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<User>()
            } catch (e: Exception) {
                Log.e("ScannerVM", "Error fetching profile", e)
            }
        }
    }

    fun processScan(studentNo: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                // 1. Find student by studentNo
                val student = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter {
                            eq("student_id", studentNo)
                            eq("role", "STUDENT")
                        }
                    }.decodeSingleOrNull<User>()

                if (student == null) {
                    errorMessage = "Student with ID $studentNo not found"
                    return@launch
                }

                val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

                // 2. Check for existing attendance today
                val existing = SupabaseConfig.client.postgrest["attendance"]
                    .select {
                        filter {
                            eq("student_id", student.id)
                            eq("attendance_date", today)
                        }
                    }.decodeList<Attendance>().find { it.scheduleId == null }

                if (scanMode == "Entrance") {
                    if (existing != null && existing.timeIn != null) {
                        errorMessage = "${student.name} already scanned IN today at ${existing.timeIn}"
                    } else if (existing != null) {
                        // Update existing (maybe it was marked absent by a teacher auto-init)
                        SupabaseConfig.client.postgrest["attendance"].update(
                            mapOf(
                                "status" to "Present",
                                "time_in" to now,
                                "updated_at" to now
                            )
                        ) {
                            filter { eq("id", existing.id!!) }
                        }
                        successMessage = "Entrance recorded for ${student.name}"
                    } else {
                        // Create new record
                        val attendance = Attendance(
                            studentId = student.id,
                            sectionId = student.sectionId,
                            date = today,
                            status = "Present",
                            timeIn = now
                        )
                        SupabaseConfig.client.postgrest["attendance"].insert(attendance)
                        successMessage = "Entrance recorded for ${student.name}"
                    }
                } else { // Exit mode
                    if (existing == null) {
                        errorMessage = "No entrance record found for ${student.name} today"
                    } else if (existing.timeOut != null) {
                        errorMessage = "${student.name} already scanned OUT today at ${existing.timeOut}"
                    } else {
                        SupabaseConfig.client.postgrest["attendance"].update(
                            mapOf(
                                "time_out" to now,
                                "updated_at" to now
                            )
                        ) {
                            filter { eq("id", existing.id!!) }
                        }
                        successMessage = "Exit recorded for ${student.name}"
                    }
                }
            } catch (e: Exception) {
                Log.e("ScannerVM", "Error processing scan", e)
                errorMessage = "Failed to process scan: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
