package com.example.students.ui.screens.weeklyschedule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.students.data.model.Schedule
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

@Composable
fun WeeklyScheduleScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit,
    onLessonClick: (Long) -> Unit
) {
    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())

    val weekLessons = allSchedules.mapNotNull { schedule ->
        try {
            val parts = schedule.date.split("-")
            val cal = Calendar.getInstance().apply {
                set(Calendar.YEAR, parts[0].toInt())
                set(Calendar.MONTH, parts[1].toInt() - 1)
                set(Calendar.DAY_OF_MONTH, parts[2].toInt())
            }
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1
            val startHour = schedule.startTime.split(":")[0].toIntOrNull() ?: 8
            val endHour = schedule.endTime.split(":")[0].toIntOrNull() ?: 9
            WeeklyLesson(schedule.id, schedule.title, dayOfWeek, startHour, endHour)
        } catch (_: Exception) {
            null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DetailTopBar(title = "每周课表", onBack = onBack)

        val scrollState = rememberScrollState()

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .horizontalScroll(scrollState)
                .padding(start = 8.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
        ) {
            Column {
                TimeHeader()
                Spacer(modifier = Modifier.height(4.dp))
                (8..20).forEach { hour ->
                    TimeSlot(hour = hour)
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
            days.forEachIndexed { index, day ->
                Column(modifier = Modifier.width(100.dp)) {
                    DayHeader(day = day)
                    Spacer(modifier = Modifier.height(4.dp))
                    (8..20).forEach { hour ->
                        val lessonForSlot = weekLessons.firstOrNull {
                            it.dayOfWeek == index && it.startHour <= hour && it.endHour > hour
                        }
                        LessonCell(
                            lesson = lessonForSlot,
                            hour = hour,
                            onLessonClick = onLessonClick
                        )
                    }
                }
                if (index < days.size - 1) {
                    Spacer(modifier = Modifier.width(2.dp))
                }
            }
        }
    }
}

private data class WeeklyLesson(
    val id: Long,
    val title: String,
    val dayOfWeek: Int,
    val startHour: Int,
    val endHour: Int
)

@Composable
private fun TimeHeader() {
    Box(
        modifier = Modifier.width(48.dp).height(40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "时间",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TimeSlot(hour: Int) {
    Box(
        modifier = Modifier.width(48.dp).height(60.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Text(
            text = "${hour}:00",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 14.sp
        )
    }
}

@Composable
private fun DayHeader(day: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primaryContainer),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun LessonCell(
    lesson: WeeklyLesson?,
    hour: Int,
    onLessonClick: (Long) -> Unit
) {
    val isStartOfLesson = lesson != null && lesson.startHour == hour
    val isWithinLesson = lesson != null && lesson.startHour <= hour && lesson.endHour > hour

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(60.dp)
            .then(
                if (isWithinLesson && !isStartOfLesson) {
                    Modifier.background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                } else {
                    Modifier.background(MaterialTheme.colorScheme.surface)
                }
            )
            .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        contentAlignment = Alignment.Center
    ) {
        if (isStartOfLesson && lesson != null) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
                    .clickable { onLessonClick(lesson.id) },
                shape = RoundedCornerShape(6.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Text(
                    text = lesson.title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
