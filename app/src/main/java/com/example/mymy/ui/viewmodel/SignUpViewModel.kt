package com.example.mymy.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.exceptions.HttpRequestException
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch
import java.util.UUID

class SignUpViewModel : ViewModel() {
    var email by mutableStateOf("")
    var password by mutableStateOf("")
    var name by mutableStateOf("")
    var role by mutableStateOf(UserRole.STUDENT)
    var gradeLevel by mutableStateOf("")
    
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)
    var isSuccess by mutableStateOf(false)

    fun signUp() {
        viewModelScope.launch {
            if (email.isBlank() || password.isBlank() || name.isBlank()) {
                errorMessage = "Please fill in all fields"
                return@launch
            }

            isLoading = true
            errorMessage = null

            try {
                // 1. Sign up
                SupabaseConfig.client.auth.signUpWith(Email) {
                    email = this@SignUpViewModel.email.trim()
                    password = this@SignUpViewModel.password
                }

                // 2. Get user session
                val user = SupabaseConfig.client.auth.currentUserOrNull()

                if (user == null) {
                    errorMessage = "Check your email to confirm your account."
                    return@launch
                }

                // 3. Insert profile
                val newUser = User(
                    id = user.id,
                    name = name,
                    email = email,
                    role = role,
                    studentNo = if (role == UserRole.STUDENT) "STD-${UUID.randomUUID().toString().substring(0, 8).uppercase()}" else null,
                    teacherId = if (role == UserRole.TEACHER) "TCH-${UUID.randomUUID().toString().substring(0, 8).uppercase()}" else null,
                    gradeLevel = if (role == UserRole.STUDENT) gradeLevel.takeIf { it.isNotBlank() } else null
                )

                SupabaseConfig.client.postgrest["profiles"].insert(newUser)

                isSuccess = true

            } catch (e: Exception) {
                Log.e("SignUp", "Error: ${e.message}", e)
                errorMessage = when (e) {
                    is HttpRequestException -> "Network error. Please check your internet connection."
                    is AuthRestException -> {
                        when (e.error) {
                            "user_already_exists" -> "This email is already registered."
                            "over_email_send_rate_limit" -> "Too many requests. Please try again in a few minutes."
                            "weak_password" -> "Password is too weak."
                            else -> "Registration failed: ${e.description ?: "Unknown error"}"
                        }
                    }
                    else -> {
                        if (e.message?.contains("User already registered", ignoreCase = true) == true) {
                            "This email is already registered. Please login instead."
                        } else if (e.message?.contains("network", ignoreCase = true) == true || 
                                   e.message?.contains("Unable to resolve host", ignoreCase = true) == true) {
                            "Network error. Please check your internet connection."
                        } else {
                            "Registration failed. Please try again later."
                        }
                    }
                }
            } finally {
                isLoading = false
            }
        }
    }
}
