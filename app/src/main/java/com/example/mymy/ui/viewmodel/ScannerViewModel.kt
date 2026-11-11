package com.example.mymy.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.Attendance
import com.example.mymy.data.model.ScanResponse
import com.example.mymy.data.model.User
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
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

    fun processScan(sessionCode: String) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            successMessage = null
            try {
                val response = SupabaseConfig.client.postgrest.rpc(
                    function = "process_attendance_scan",
                    parameters = buildJsonObject {
                        put("p_session_code", sessionCode)
                        put("p_scan_mode", scanMode)
                    }
                ).decodeAs<ScanResponse>()

                if (response.success) {
                    successMessage = response.message
                } else {
                    errorMessage = response.message
                }
            } catch (e: Exception) {
                Log.e("ScannerVM", "Error processing scan", e)
                errorMessage = "Scan failed: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
