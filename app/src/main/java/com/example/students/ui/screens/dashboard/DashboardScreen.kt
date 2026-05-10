package com.example.students.ui.screens.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.ui.navigation.Screen
import com.example.students.ui.viewmodel.AppViewModel

@Composable
fun DashboardScreen(viewModel: AppViewModel, navigateTo: (String) -> Unit) {
    val todayDate = viewModel.todayDate.collectAsState().value
    val lowLessonList by viewModel.lowLessonStudents.collectAsState(initial = emptyList())
    val schedules by viewModel.getSchedulesByDate(todayDate).collectAsState(initial = emptyList())

    var scheduleStudents by remember { mutableStateOf<Map<Long, List<Student>>>(emptyMap()) }

    LaunchedEffect(schedules) {
        val map = mutableMapOf<Long, List<Student>>()
        for (schedule in schedules) {
            val students = when {
                schedule.studentId != null -> {
                    val student = viewModel.getStudentById(schedule.studentId)
                    if (student != null) listOf(student) else emptyList()
                }
                schedule.courseId != null -> {
                    viewModel.getStudentsByCourseId(schedule.courseId)
                }
                else -> emptyList()
            }
            map[schedule.id] = students
        }
        scheduleStudents = map
    }

    val displayDate = todayDate.split("-").let { parts ->
        "${parts[0]}年${parts[1]}月${parts[2]}日"
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { OverviewSection(displayDate, lowLessonList.size, navigateTo) }
        item { RemindersSection(navigateTo) }
        if (schedules.isNotEmpty()) {
            item { ScheduleSection(schedules, scheduleStudents, navigateTo) }
        }
        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun OverviewSection(
    displayDate: String,
    lowLessonCount: Int,
    navigateTo: (String) -> Unit
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "今日概况",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                displayDate,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.errorContainer)
                .clickable { navigateTo(Screen.LowLessonReminders.route) }
                .padding(16.dp)
        ) {
            Column {
                Text(
                    "课时提醒",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    lowLessonCount.toString().padStart(2, '0'),
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    "位学生剩余课时不足",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun RemindersSection(navigateTo: (String) -> Unit) {
    Column {
        Text(
            "待办提醒",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))
        LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            items(
                listOf(
                    Pair("确认下周课表", "12个冲突待处理") to Screen.ScheduleConfirmation.route,
                    Pair("续费待跟进", "王同学课时即将到期") to Screen.RenewalFollowup.route
                )
            ) { (item, target) ->
                Row(
                    modifier = Modifier
                        .width(260.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .clickable { navigateTo(target) }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Warning,
                            "reminder",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            item.first,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            item.second,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ScheduleSection(
    schedules: List<Schedule>,
    scheduleStudents: Map<Long, List<Student>>,
    navigateTo: (String) -> Unit
) {
    val validSchedules = schedules.filter { schedule ->
        val students = scheduleStudents[schedule.id] ?: emptyList()
        students.isNotEmpty()
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "今日课程",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "今日排课",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navigateTo(Screen.TodayLessons.route) }
                )
                Text(
                    "周表",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable { navigateTo(Screen.WeeklySchedule.route) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))

        if (validSchedules.isEmpty()) {
            Text(
                "今日暂无带学生的课程",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        validSchedules.sortedBy { it.startTime }.forEach { schedule ->
            val students = scheduleStudents[schedule.id] ?: emptyList()
            val (statusColor, statusLabel) = when (schedule.status) {
                "completed" -> MaterialTheme.colorScheme.primary to "已完成"
                "active" -> MaterialTheme.colorScheme.tertiary to "进行中"
                "pending" -> MaterialTheme.colorScheme.outline to "待上课"
                "leave" -> MaterialTheme.colorScheme.error to "已请假"
                else -> MaterialTheme.colorScheme.outlineVariant to "未知"
            }
            val studentNames = students.joinToString("、") { it.name }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .clickable { navigateTo(Screen.ClassDetail.createRoute(schedule.id)) }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(50.dp)
                ) {
                    Text(
                        schedule.startTime,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        schedule.endTime,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            schedule.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(statusColor.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                statusLabel,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = statusColor,
                                maxLines = 1
                            )
                        }
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(
                        studentNames,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (schedule.room.isNotEmpty()) {
                        Text(
                            schedule.room,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}