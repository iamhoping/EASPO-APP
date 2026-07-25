package com.example.mymy.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

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
                    email = this@LoginViewModel.email
                    password = this@LoginViewModel.password
                }

                // 2. Get user
                val userId = SupabaseConfig.client.auth.currentUserOrNull()?.id

                if (userId == null) {
                    errorMessage = "Login failed. No user session."
                    return@launch
                }

                // 3. Get profile safely
                val result = SupabaseConfig.client.postgrest["profiles"]
                    .select {
                        filter { eq("id", userId) }
                    }

                val profile = result.decodeList<com.example.mymy.data.model.User>().firstOrNull()

                if (profile != null) {
                    onSuccess(profile.role ?: UserRole.STUDENT)
                } else {
                    errorMessage = "Profile not found"
                }

            } catch (e: Exception) {
                errorMessage = "Incorrect email or password. Please try again."
            } finally {
                isLoading = false
            }
        }

    }
}
