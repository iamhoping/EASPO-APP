package com.example.mymy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

class ForgotPasswordViewModel : ViewModel() {
    var email by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var message by mutableStateOf<String?>(null)
    var isError by mutableStateOf(false)

    fun sendResetEmail(onSuccess: () -> Unit) {
        if (email.isBlank()) {
            message = "Please enter your email"
            isError = true
            return
        }

        viewModelScope.launch {
            isLoading = true
            message = null
            isError = false
            try {
                SupabaseConfig.client.auth.resetPasswordForEmail(email.trim())
                message = "Password reset email sent. Please check your inbox."
                isError = false
                // Optional: delay then call onSuccess to navigate back
            } catch (e: Exception) {
                message = e.message ?: "Failed to send reset email"
                isError = true
            } finally {
                isLoading = false
            }
        }
    }
}
