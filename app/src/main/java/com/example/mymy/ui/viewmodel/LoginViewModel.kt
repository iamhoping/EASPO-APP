package com.example.mymy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.exceptions.RestException

class LoginViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    fun fillAdminCredentials() {
        email = "admin@example.com"
        password = "admin123"
    }

    fun login(onSuccess: (UserRole) -> Unit) {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank()) {
                errorMessage = "Please enter email and password"
                return@launch
            }

            isLoading = true
            errorMessage = null

            try {
                // 1. Login
                SupabaseConfig.client.auth.signInWith(Email) {
                    email = this@LoginViewModel.email.trim()
                    password = this@LoginViewModel.password
                }

                // 2. Get user
                val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id

                if (userId == null) {
                    errorMessage = "Login failed. No user session."
                    return@launch
                }

                // 3. Get profile safely
                val profile = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                    }.decodeSingleOrNull<User>()

                if (profile != null) {
                    onSuccess(profile.role ?: UserRole.STUDENT)
                } else {
                    errorMessage = "Profile not found"
                }

            } catch (e: Exception) {
                Log.e("Login", "Login error: ${e.message}", e)
                errorMessage = when (e) {
                    is HttpRequestException -> "Network error. Please check your internet connection."
                    is AuthRestException -> {
                        when (e.error) {
                            "invalid_credentials" -> "Incorrect email or password."
                            "user_not_found" -> "Account not found."
                            else -> "Authentication failed: ${e.description ?: "Unknown error"}"
                        }
                    }
                    is RestException -> "Database error: ${e.message}"
                    else -> {
                        if (e.message?.contains("network", ignoreCase = true) == true || 
                            e.message?.contains("Unable to resolve host", ignoreCase = true) == true) {
                            "Network error. Please check your internet connection."
                        } else {
                            "An unexpected error occurred: ${e.localizedMessage}"
                        }
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}
