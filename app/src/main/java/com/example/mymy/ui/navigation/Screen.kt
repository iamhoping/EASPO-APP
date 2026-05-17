package com.example.mymy.ui.navigation

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object SignUp : Screen("signup")
    object ForgotPassword : Screen("forgot_password")
    object ResetPassword : Screen("reset_password")
    object StudentDashboard : Screen("student_dashboard")
    object TeacherDashboard : Screen("teacher_dashboard")
    object SchoolAdminDashboard : Screen("school_admin_dashboard")
    object WebAdminDashboard : Screen("web_admin_dashboard")
    object ParentDashboard : Screen("parent_dashboard")
}
