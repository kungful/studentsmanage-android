package com.example.students.ui.navigation

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object Dashboard : Screen("dashboard")
    data object Students : Screen("students")
    data object Calendar : Screen("calendar")
    data object Reports : Screen("reports")
    data object Settings : Screen("settings")
    data object ClassDetail : Screen("class_detail/{scheduleId}") {
        fun createRoute(scheduleId: Long = 0) = "class_detail/$scheduleId"
    }
    data object LowLessonReminders : Screen("low_lesson_reminders")
    data object TodayLessons : Screen("today_lessons")
    data object StudentDetail : Screen("student_detail/{studentId}") {
        fun createRoute(studentId: Long = 0) = "student_detail/$studentId"
    }
    data object ScheduleConfirmation : Screen("schedule_confirmation")
    data object RenewalFollowup : Screen("renewal_followup")
    data object ManualScheduleAdjust : Screen("manual_schedule_adjust")
    data object WeeklySchedule : Screen("weekly_schedule")
    data object PastLessons : Screen("past_lessons")
    data object AddSchedule : Screen("add_schedule")
    data object AttendanceDetail : Screen("attendance_detail")
    data object ConsumedHoursDetail : Screen("consumed_hours_detail")
    data object RevenueDetail : Screen("revenue_detail")
    data object ActiveStudentsDetail : Screen("active_students_detail")
    data object Notifications : Screen("notifications")
    data object AddStudent : Screen("add_student")
}

val bottomNavItems = listOf(
    Screen.Dashboard,
    Screen.Students,
    Screen.Calendar,
    Screen.Reports,
    Screen.Settings,
)
