package com.example.students.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.students.ui.components.AppBottomBar
import com.example.students.ui.components.AppTopBar
import com.example.students.ui.navigation.Screen
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.students.ui.screens.addschedule.AddScheduleScreen
import com.example.students.ui.screens.addstudent.AddStudentScreen
import com.example.students.ui.screens.login.LoginScreen
import com.example.students.ui.screens.attendancedetail.AttendanceDetailScreen
import com.example.students.ui.screens.activestudentsdetail.ActiveStudentsDetailScreen
import com.example.students.ui.screens.calendar.CalendarScreen
import com.example.students.ui.screens.classdetail.ClassDetailScreen
import com.example.students.ui.screens.consumedhoursdetail.ConsumedHoursDetailScreen
import com.example.students.ui.screens.dashboard.DashboardScreen
import com.example.students.ui.screens.lowlessonreminders.LowLessonRemindersScreen
import com.example.students.ui.screens.manualscheduleadjust.ManualScheduleAdjustScreen
import com.example.students.ui.screens.notifications.NotificationsScreen
import com.example.students.ui.screens.pastlessonsoverview.PastLessonsOverviewScreen
import com.example.students.ui.screens.renewalfollowup.RenewalFollowupScreen
import com.example.students.ui.screens.reports.ReportsScreen
import com.example.students.ui.screens.revenuedetail.RevenueDetailScreen
import com.example.students.ui.screens.scheduleconfirmation.ScheduleConfirmationScreen
import com.example.students.ui.screens.settings.SettingsScreen
import com.example.students.ui.screens.studentdetail.StudentDetailScreen
import com.example.students.ui.screens.students.StudentsScreen
import com.example.students.ui.screens.todaylessons.TodayLessonsScreen
import com.example.students.ui.screens.weeklyschedule.WeeklyScheduleScreen
import com.example.students.ui.viewmodel.AppViewModel

@Composable
fun MainApp() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var loginMode by remember { mutableStateOf("") }

    if (!isLoggedIn) {
        LoginScreen(
            onOfflineEnter = {
                loginMode = "offline"
                isLoggedIn = true
            }
        )
    } else {
        MainAppContent(
            loginMode = loginMode,
            onLogout = {
                isLoggedIn = false
                loginMode = ""
            }
        )
    }
}

@Composable
private fun MainAppContent(loginMode: String, onLogout: () -> Unit) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val viewModel: AppViewModel = viewModel()

    val bottomNavScreens = listOf(
        Screen.Dashboard, Screen.Students, Screen.Calendar,
        Screen.Reports, Screen.Settings
    )

    val showBottomBar = bottomNavScreens.any { screen ->
        navBackStackEntry?.destination?.hierarchy?.any { it.route == screen.route } == true
    }

    val showTopBar = showBottomBar

    val topBarTitle = when {
        currentRoute == Screen.Dashboard.route -> "首页总览"
        currentRoute == Screen.Students.route -> "学生列表"
        currentRoute == Screen.Calendar.route -> "日历主界面"
        currentRoute == Screen.Reports.route -> "数据统计中心"
        currentRoute == Screen.Settings.route -> "设置"
        else -> ""
    }

    Scaffold(
        topBar = {
            if (showTopBar) {
                AppTopBar(
                    title = topBarTitle,
                    onNotificationsClick = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                AppBottomBar(
                    currentRoute = currentRoute,
                    onNavigate = { screen ->
                        navController.navigate(screen.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(
                    viewModel = viewModel,
                    navigateTo = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Students.route) {
                StudentsScreen(
                    viewModel = viewModel,
                    navigateTo = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Calendar.route) {
                CalendarScreen(
                    viewModel = viewModel,
                    isStudentCalendar = false,
                    studentId = null,
                    onAddSchedule = { navController.navigate(Screen.AddSchedule.route) },
                    onDayClick = { date ->
                        viewModel.setSelectedDate(date)
                        navController.navigate(Screen.TodayLessons.route)
                    },
                    onLessonClick = { scheduleId ->
                        navController.navigate(Screen.ClassDetail.createRoute(scheduleId))
                    }
                )
            }
            composable(Screen.Reports.route) {
                ReportsScreen(
                    viewModel = viewModel,
                    onNavigate = { route -> navController.navigate(route) }
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(viewModel = viewModel, onLogout = onLogout)
            }
            composable(
                Screen.ClassDetail.route,
                arguments = listOf(navArgument("scheduleId") { type = NavType.LongType })
            ) { backStackEntry ->
                val scheduleId = backStackEntry.arguments?.getLong("scheduleId") ?: 0L
                ClassDetailScreen(
                    viewModel = viewModel,
                    scheduleId = scheduleId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.LowLessonReminders.route) {
                LowLessonRemindersScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.TodayLessons.route) {
                TodayLessonsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { scheduleId ->
                        navController.navigate(Screen.ClassDetail.createRoute(scheduleId))
                    }
                )
            }
            composable(
                Screen.StudentDetail.route,
                arguments = listOf(navArgument("studentId") { type = NavType.LongType })
            ) { backStackEntry ->
                val studentId = backStackEntry.arguments?.getLong("studentId") ?: 0L
                StudentDetailScreen(
                    viewModel = viewModel,
                    studentId = studentId,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ScheduleConfirmation.route) {
                ScheduleConfirmationScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onManualAdjust = { navController.navigate(Screen.ManualScheduleAdjust.route) }
                )
            }
            composable(Screen.RenewalFollowup.route) {
                RenewalFollowupScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onStudentClick = { studentId ->
                        navController.navigate(Screen.StudentDetail.createRoute(studentId))
                    }
                )
            }
            composable(Screen.ManualScheduleAdjust.route) {
                ManualScheduleAdjustScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.WeeklySchedule.route) {
                WeeklyScheduleScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onLessonClick = { scheduleId ->
                        navController.navigate(Screen.ClassDetail.createRoute(scheduleId))
                    }
                )
            }
            composable(Screen.PastLessons.route) {
                PastLessonsOverviewScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddSchedule.route) {
                AddScheduleScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
            composable(Screen.AttendanceDetail.route) {
                AttendanceDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ConsumedHoursDetail.route) {
                ConsumedHoursDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.RevenueDetail.route) {
                RevenueDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.ActiveStudentsDetail.route) {
                ActiveStudentsDetailScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(Screen.AddStudent.route) {
                AddStudentScreen(
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onSuccess = { navController.popBackStack() }
                )
            }
        }
    }
}
