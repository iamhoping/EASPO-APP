package com.example.mymy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mymy.data.model.UserRole
import com.example.mymy.data.remote.SupabaseConfig
import com.example.mymy.ui.navigation.Screen
import com.example.mymy.ui.screens.admin.SchoolAdminScreen
import com.example.mymy.ui.screens.admin.WebAdminScreen
import com.example.mymy.ui.screens.login.ForgotPasswordScreen
import com.example.mymy.ui.screens.login.LoginScreen
import com.example.mymy.ui.screens.login.SignUpScreen
import com.example.mymy.ui.screens.parent.ParentScreen
import com.example.mymy.ui.screens.student.StudentScreen
import com.example.mymy.ui.screens.teacher.TeacherScreen
import com.example.mymy.ui.theme.MymyTheme
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MymyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppNavigation()
                }
            }
        }
    }
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Login.route) {
        composable(Screen.Login.route) {
            LoginScreen(
                onForgotPasswordClick = { navController.navigate(Screen.ForgotPassword.route) },
                onLogin = { role ->
                    when (role) {
                        UserRole.STUDENT -> navController.navigate(Screen.StudentDashboard.route)
                        UserRole.TEACHER -> navController.navigate(Screen.TeacherDashboard.route)
                        UserRole.SCHOOL_ADMIN -> navController.navigate(Screen.SchoolAdminDashboard.route)
                        UserRole.WEB_ADMIN -> navController.navigate(Screen.WebAdminDashboard.route)
                        UserRole.PARENT -> navController.navigate(Screen.ParentDashboard.route)
                    }
                }
            )
        }
        composable(Screen.SignUp.route) {
            SignUpScreen(
                onSignUpSuccess = { navController.navigate(Screen.Login.route) { popUpTo(Screen.Login.route) { inclusive = true } } },
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
        composable(Screen.ForgotPassword.route) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }
        composable(Screen.StudentDashboard.route) { StudentScreen(onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }) }
        composable(Screen.TeacherDashboard.route) { TeacherScreen(onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }) }
        composable(Screen.SchoolAdminDashboard.route) { SchoolAdminScreen(onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }) }
        composable(Screen.WebAdminDashboard.route) { WebAdminScreen(onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }) }
        composable(Screen.ParentDashboard.route) { ParentScreen(onLogout = { navController.navigate(Screen.Login.route) { popUpTo(0) } }) }
    }
}
