package com.example.mymy.data.model
//latest
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

enum class UserRole {
    STUDENT, TEACHER, SCHOOL_ADMIN, WEB_ADMIN, PARENT
}

@Serializable
data class User(
    val id: String,
    val name: String? = null,
    val role: UserRole? = null,
    val email: String? = null,
    @SerialName("student_id") val studentNo: String? = null,
    @SerialName("teacher_id") val teacherId: String? = null,
    @SerialName("child_id") val childId: String? = null,
    val contact: String? = null,
    val address: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("guardian_email") val guardianEmail: String? = null,
    val gender: String? = null,
    @SerialName("grade_level") val gradeLevel: String? = null,
    val department: String? = null,
    val status: String? = null,
    @SerialName("section_id") val sectionId: Long? = null
)

@Serializable
data class RegisterUserRequest(
    val email: String,
    val password: String,
    val name: String,
    val role: String,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("teacher_id") val teacherId: String? = null,
    @SerialName("child_id") val childId: String? = null,
    @SerialName("parent_id") val parentId: String? = null,
    @SerialName("guardian_email") val guardianEmail: String? = null,
    val gender: String? = null,
    val contact: String? = null,
    val address: String? = null,
    @SerialName("grade_level") val gradeLevel: String? = null
)

@Serializable
data class Attendance(
    val id: Long? = null,
    @SerialName("student_id") val studentId: String,
    @SerialName("section_id") val sectionId: Long? = null,
    @SerialName("schedule_id") val scheduleId: Long? = null,
    @SerialName("attendance_date") val date: String,
    @SerialName("time_in") val timeIn: String? = null,
    @SerialName("time_out") val timeOut: String? = null,
    val status: String,
    @SerialName("created_at") val createdAt: String? = null,
    @SerialName("updated_at") val updatedAt: String? = null,
    val subject: String? = null,
    @SerialName("subject_id") val subjectId: Int? = null,
    @SerialName("teacher_id") val teacherId: String? = null
)

@Serializable
data class Grade(
    val id: Long? = null,
    @SerialName("student_id") val studentId: String,
    @SerialName("teacher_id") val teacherId: String? = null,
    val subject: String,
    @SerialName("subject_id") val subjectId: Int? = null,
    val score: Double,
    val remarks: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Enrollment(
    val id: Long? = null,
    @SerialName("schedule_id") val scheduleId: Long,
    @SerialName("student_id") val studentId: String
)

@Serializable
data class Section(
    val id: Long? = null,
    val name: String,
    @SerialName("grade_level") val gradeLevel: String,
    @SerialName("adviser_id") val adviserId: String? = null,
    val status: String? = "Pending",
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Subject(
    val id: Int? = null,
    val code: String? = null,
    val name: String,
    @SerialName("grade_level") val gradeLevel: String? = null,
    val units: Int? = null,
    val status: String? = null,
    @SerialName("created_at") val createdAt: String? = null
)

@Serializable
data class Schedule(
    val id: Long? = null,
    @SerialName("student_id") val studentId: String? = null,
    @SerialName("section_id") val sectionId: Long? = null,
    @SerialName("teacher_id") val teacherId: String? = null,
    @SerialName("subject_id") val subjectId: Int? = null,
    val day: String = "",
    @SerialName("start_time") val startTime: String? = null,
    @SerialName("end_time") val endTime: String? = null,
    val subject: String? = null,
    val room: String = "",
    val time: String? = null
)
