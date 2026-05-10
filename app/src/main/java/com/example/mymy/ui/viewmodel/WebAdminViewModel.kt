package com.example.mymy.ui.viewmodel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mymy.data.model.Schedule
import com.example.mymy.data.model.User
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.launch

class WebAdminViewModel : ViewModel() {
    var allUsers by mutableStateOf<List<User>>(emptyList())
    var allSchedules by mutableStateOf<List<Schedule>>(emptyList())
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    var searchQuery by mutableStateOf("")
    var roleFilter by mutableStateOf<UserRole?>(null)

    val filteredUsers: List<User>
        get() {
            return allUsers.filter { user ->
                val matchesSearch = (user.name?.contains(searchQuery, ignoreCase = true) ?: false) ||
                                  (user.email?.contains(searchQuery, ignoreCase = true) ?: false)
                val matchesRole = roleFilter == null || user.role == roleFilter
                matchesSearch && matchesRole
            }
        }

    val parentsCount: Int get() = allUsers.count { it.role == UserRole.PARENT }
    val teachersCount: Int get() = allUsers.count { it.role == UserRole.TEACHER }
    val studentsCount: Int get() = allUsers.count { it.role == UserRole.STUDENT }

    fun fetchData() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            
            // Fetch users and schedules in parallel
            val usersJob = launch { fetchAllUsersInternal() }
            val schedulesJob = launch { fetchAllSchedulesInternal() }
            
            usersJob.join()
            schedulesJob.join()
            isLoading = false
        }
    }

    private suspend fun fetchAllUsersInternal() {
        try {
            allUsers = SupabaseConfig.client.postgrest["profiles"]
                .select()
                .decodeList<User>()
            Log.d("WebAdminVM", "Fetched ${allUsers.size} users")
        } catch (e: Exception) {
            Log.e("WebAdminVM", "Error fetching users", e)
            // Don't overwrite errorMessage if schedules already set one
            if (errorMessage == null) errorMessage = "Users: ${e.message}"
        }
    }

    private suspend fun fetchAllSchedulesInternal() {
        try {
            allSchedules = SupabaseConfig.client.postgrest["schedules"]
                .select()
                .decodeList<Schedule>()
            Log.d("WebAdminVM", "Fetched ${allSchedules.size} schedules")
        } catch (e: Exception) {
            Log.e("WebAdminVM", "Error fetching schedules", e)
            errorMessage = "Schedules: ${e.message}"
        }
    }

    fun fetchAllUsers() {
        viewModelScope.launch {
            isLoading = true
            fetchAllUsersInternal()
            isLoading = false
        }
    }

    fun fetchAllSchedules() {
        viewModelScope.launch {
            isLoading = true
            fetchAllSchedulesInternal()
            isLoading = false
        }
    }

    fun upsertSchedule(schedule: Schedule) {
        viewModelScope.launch {
            try {
                if (schedule.id == null) {
                    SupabaseConfig.client.postgrest["schedules"].insert(schedule)
                } else {
                    SupabaseConfig.client.postgrest["schedules"].update(schedule) {
                        filter { eq("id", schedule.id) }
                    }
                }
                fetchAllSchedulesInternal()
            } catch (e: Exception) {
                Log.e("WebAdminVM", "Save failed", e)
                errorMessage = "Save failed: ${e.localizedMessage}"
            }
        }
    }

    fun deleteSchedule(id: Long) {
        viewModelScope.launch {
            try {
                SupabaseConfig.client.postgrest["schedules"].delete {
                    filter { eq("id", id) }
                }
                fetchAllSchedulesInternal()
            } catch (e: Exception) {
                errorMessage = "Delete failed: ${e.localizedMessage}"
            }
        }
    }

    fun updateUser(user: User) {
        viewModelScope.launch {
            try {
                SupabaseConfig.client.postgrest["profiles"].update(user) {
                    filter { eq("id", user.id) }
                }
                fetchAllUsersInternal()
            } catch (e: Exception) {
                errorMessage = "User update failed: ${e.localizedMessage}"
            }
        }
    }

    fun deleteUser(userId: String) {
        viewModelScope.launch {
            try {
                SupabaseConfig.client.postgrest["profiles"].delete {
                    filter { eq("id", userId) }
                }
                fetchAllUsersInternal()
            } catch (e: Exception) {
                errorMessage = "User deletion failed: ${e.localizedMessage}"
            }
        }
    }
}
