package com.example.mymy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class ResetPasswordViewModel : ViewModel() {
    var newPassword by mutableStateOf("")
    var confirmPassword by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var isError by mutableStateOf(false)
    var isSuccess by mutableStateOf(false)

    fun updatePassword(onSuccess: () -> Unit) {
        if (newPassword.isBlank()) {
            message = "Please enter a new password"
            isError = true
            return
        }
        if (newPassword != confirmPassword) {
            message = "Passwords do not match"
            isError = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            isError = false
            try {
                // Ensure we have a session (handled by handleDeeplinks in MainActivity)
                // We might need to wait for the session to be active
                val sessionStatus = SupabaseConfig.client.auth.sessionStatus.value
                if (sessionStatus !is SessionStatus.Authenticated) {
                    message = "Session not found. Please try the reset link again."
                    isError = true
                    return@launch
                }

                SupabaseConfig.client.auth.updateUser {
                    password = newPassword
                }
                
                message = "Password updated successfully!"
                isSuccess = true
                isError = false
                // Success!
            } catch (e: Exception) {
                message = e.message ?: "Failed to update password"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}
