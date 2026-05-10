package com.example.students.ui.screens.todaylessons

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.viewmodel.AppViewModel

@Composable
fun TodayLessonsScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onLessonClick: (Long) -> Unit
) {
    val defaultDate = viewModel.todayDate.collectAsState().value
    val selectedDate by viewModel.selectedDate.collectAsState()

    val displayDate = selectedDate.ifBlank { defaultDate }
    val todaySchedules by viewModel.getSchedulesByDate(displayDate).collectAsState(initial = emptyList())

    val isToday = displayDate == defaultDate

    DisposableEffect(Unit) {
        onDispose {
            viewModel.setSelectedDate("")
        }
    }

    var scheduleStudents by remember { mutableStateOf<Map<Long, List<Student>>>(emptyMap()) }

    LaunchedEffect(todaySchedules) {
        val map = mutableMapOf<Long, List<Student>>()
        for (schedule in todaySchedules) {
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

    val validSchedules = remember(todaySchedules, scheduleStudents) {
        todaySchedules.filter { schedule ->
            (scheduleStudents[schedule.id] ?: emptyList()).isNotEmpty()
        }
    }

    val title = if (isToday) "今日课程" else {
        displayDate.split("-").let { "${it[0]}年${it[1]}月${it[2]}日 课程" }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DetailTopBar(title = title, onBack = onBack)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "共 ${validSchedules.size} 节课程",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }

            items(validSchedules.sortedBy { it.startTime }) { schedule ->
                val students = scheduleStudents[schedule.id] ?: emptyList()
                TodayScheduleCard(
                    schedule = schedule,
                    students = students,
                    onClick = { onLessonClick(schedule.id) }
                )
            }

            if (validSchedules.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (todaySchedules.isEmpty()) "该日暂无课程安排"
                            else "该日课程暂无关联学生",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TodayScheduleCard(
    schedule: Schedule,
    students: List<Student>,
    onClick: () -> Unit
) {
    val (statusColor, statusText) = when (schedule.status) {
        "completed" -> MaterialTheme.colorScheme.primary to "已完成"
        "active" -> MaterialTheme.colorScheme.tertiary to "进行中"
        "pending" -> MaterialTheme.colorScheme.outline to "待上课"
        "leave" -> MaterialTheme.colorScheme.error to "已请假"
        else -> MaterialTheme.colorScheme.outlineVariant to "未知"
    }

    val studentNames = if (students.isNotEmpty()) {
        if (students.size <= 3) students.joinToString("、") { it.name }
        else students.take(3).joinToString("、") { it.name } + " 等${students.size}人"
    } else {
        "${schedule.students}名学生"
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.width(50.dp)
            ) {
                Text(
                    text = schedule.startTime,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = schedule.endTime,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = schedule.title,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
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
                            text = statusText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = statusColor,
                            maxLines = 1
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = studentNames,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(
                        text = schedule.location,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}