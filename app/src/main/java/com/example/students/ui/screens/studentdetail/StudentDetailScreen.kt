package com.example.students.ui.screens.studentdetail

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Course
import com.example.students.data.model.Attendance
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.data.model.StudentCourse
import com.example.students.data.model.StudentParent
import com.example.students.data.dao.StudentCourseWithName
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import java.util.Calendar

private data class LearningRecord(
    val date: String, val topic: String, val status: String, val teacher: String
)

private data class CalendarDay(
    val day: Int, val status: String = ""
)

private data class StudentDayLesson(
    val scheduleId: Long,
    val time: String,
    val topic: String,
    val status: String,
    val room: String,
    val startTime: String,
    val endTime: String,
    val courseId: Long?,
    val date: String
)

private data class PerformanceRating(
    val label: String, val value: Float
)

@Composable
fun StudentDetailScreen(viewModel: AppViewModel, studentId: Long, onBack: () -> Unit) {
    var currentSubView by remember { mutableStateOf("main") }
    var subViewStack by remember { mutableStateOf(listOf("main")) }
    var selectedParentId by remember { mutableStateOf(0L) }
    var selectedDay by remember { mutableStateOf(0) }
    var selectedYearMonth by remember { mutableStateOf("") }
    var selectedScheduleId by remember { mutableStateOf(0L) }

    fun navigateTo(view: String) {
        subViewStack = subViewStack + view
        currentSubView = view
    }

    val goBack: () -> Unit = {
        if (subViewStack.size > 1) {
            subViewStack = subViewStack.dropLast(1)
            currentSubView = subViewStack.last()
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            when (currentSubView) {
                "main" -> DetailTopBar("学生详情", onBack)
                "edit" -> DetailTopBar("编辑资料", goBack)
                "parents" -> DetailTopBar("家长信息", goBack)
                "parent_detail" -> DetailTopBar("家长详情", goBack)
                "calendar" -> DetailTopBar("课程日历", goBack)
                "day_view" -> DetailTopBar("课程列表", goBack)
                "lesson_detail" -> DetailTopBar("课程内容", goBack)
                "performance_report" -> DetailTopBar("学习报告", goBack)
            }
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            when (currentSubView) {
                "main" -> MainView(
                    viewModel = viewModel,
                    studentId = studentId,
                    onNavigate = ::navigateTo,
                    onDayClick = { day, yearMonth ->
                        selectedDay = day
                        selectedYearMonth = yearMonth
                        navigateTo("day_view")
                    }
                )
                "edit" -> EditView(viewModel = viewModel, studentId = studentId, onBack = goBack)
                "parents" -> ParentsView(
                    viewModel = viewModel,
                    studentId = studentId,
                    onParentClick = { id ->
                        selectedParentId = id
                        navigateTo("parent_detail")
                    }
                )
                "parent_detail" -> ParentDetailView(
                    viewModel = viewModel,
                    studentId = studentId,
                    parentId = selectedParentId
                )
                "calendar" -> CalendarView(
                    viewModel = viewModel,
                    studentId = studentId,
                    onDayClick = { day, yearMonth ->
                        selectedDay = day
                        selectedYearMonth = yearMonth
                        navigateTo("day_view")
                    }
                )
                "day_view" -> DayView(
                    viewModel = viewModel,
                    studentId = studentId,
                    day = selectedDay,
                    yearMonth = selectedYearMonth,
                    onLessonClick = { scheduleId ->
                        selectedScheduleId = scheduleId
                        navigateTo("lesson_detail")
                    }
                )
                "lesson_detail" -> LessonDetailView(
                    viewModel = viewModel,
                    studentId = studentId,
                    scheduleId = selectedScheduleId,
                    onViewReport = { navigateTo("performance_report") }
                )
                "performance_report" -> PerformanceReportView(
                    viewModel = viewModel,
                    studentId = studentId
                )
            }
        }
    }
}

@Composable
private fun MainView(
    viewModel: AppViewModel,
    studentId: Long,
    onNavigate: (String) -> Unit,
    onDayClick: (day: Int, yearMonth: String) -> Unit
) {
    var student by remember { mutableStateOf<Student?>(null) }
    var attendanceRecords by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var studentCourses by remember { mutableStateOf<List<StudentCourseWithName>>(emptyList()) }
    val currentYearMonth = remember { viewModel.currentYearMonth.value }
    val todayDate = viewModel.todayDate.value
    val todayParts = remember(todayDate) {
        val parts = todayDate.split("-")
        Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }

    val miniSchedules by remember(currentYearMonth) {
        viewModel.getSchedulesByStudentCoursesAndMonth(studentId, currentYearMonth)
    }.collectAsState(initial = emptyList())

    val allStudentSchedules by remember {
        viewModel.getSchedulesByStudentCourses(studentId)
    }.collectAsState(initial = emptyList())

    LaunchedEffect(studentId) {
        student = viewModel.getStudentById(studentId)
        attendanceRecords = viewModel.getAttendanceByStudent(studentId)
        studentCourses = viewModel.getStudentCoursesWithName(studentId)
    }

    val completedSchedules = allStudentSchedules.filter { it.status == "completed" }
    val completedCount = completedSchedules.size
    val presentCount = attendanceRecords.count { it.status == "present" }
    val totalAttendance = attendanceRecords.size
    val attendanceRate = if (totalAttendance > 0) (presentCount.toFloat() / totalAttendance * 100).toInt() else 0

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { ProfileHeader(student = student, courses = studentCourses, onNavigate = onNavigate) }
        item {
            if (studentCourses.isNotEmpty()) {
                CoursesEnrolledCard(studentCourses)
            }
        }
        item {
            StatsRow(
                remainingHours = studentCourses.sumOf { it.remainingHours },
                completedCount = completedCount,
                attendanceRate = attendanceRate
            )
        }
        item {
            MiniCalendar(
                schedules = miniSchedules,
                yearMonth = currentYearMonth,
                todayParts = todayParts,
                onDayClick = { day -> onDayClick(day, currentYearMonth) },
                onViewFull = { onNavigate("calendar") }
            )
        }
        item {
            SectionHeader("学习记录  >", onClick = { onNavigate("performance_report") })
        }
        item { LearningHistory(completedSchedules = completedSchedules) }
    }
}

@Composable
private fun ProfileHeader(student: Student?, courses: List<StudentCourseWithName>, onNavigate: (String) -> Unit) {
    val name = student?.name ?: ""
    val courseText = if (courses.isNotEmpty()) courses.joinToString(" / ") { it.courseName } else (student?.course ?: "")
    val initial = name.firstOrNull()?.toString() ?: "?"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        initial,
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        "edit",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(
                name.ifEmpty { "未命名" },
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                courseText.ifEmpty { "未分配课程" },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ProfileAction("编辑", Icons.Outlined.Edit) { onNavigate("edit") }
                ProfileAction("家长", Icons.Filled.People) { onNavigate("parents") }
                ProfileAction("日历", Icons.Outlined.DateRange) { onNavigate("calendar") }
                ProfileAction("报告", Icons.Outlined.Edit) { onNavigate("performance_report") }
            }
        }
    }
}

@Composable
private fun ProfileAction(label: String, icon: ImageVector, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon,
                label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun CoursesEnrolledCard(courses: List<StudentCourseWithName>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                "已报课程",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(8.dp))
            courses.forEach { course ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            course.courseName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "共${course.totalHours}节 · ¥${course.price}/节",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${course.remainingHours}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = if (course.remainingHours <= 2) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.primary
                        )
                        Text(
                            "剩余节",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatsRow(remainingHours: Int, completedCount: Int, attendanceRate: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        StatItem(
            label = "剩余课时",
            value = remainingHours.toString(),
            unit = "节",
            color = MaterialTheme.colorScheme.primary,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = "已完成",
            value = completedCount.toString(),
            unit = "节",
            color = MaterialTheme.colorScheme.tertiary,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            modifier = Modifier.weight(1f)
        )
        StatItem(
            label = "出勤率",
            value = "${attendanceRate}%",
            unit = "",
            color = MaterialTheme.colorScheme.secondary,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    unit: String,
    color: Color,
    containerColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(containerColor)
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = color.copy(alpha = 0.8f)
        )
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                value,
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = color
            )
            if (unit.isNotEmpty()) {
                Text(
                    unit,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = color,
                    modifier = Modifier.padding(bottom = 2.dp)
                )
            }
        }
    }
}

private fun buildMonthGrid(yearMonth: String): List<List<Int>> {
    val parts = yearMonth.split("-")
    val year = parts[0].toInt()
    val month = parts[1].toInt()
    val cal = Calendar.getInstance()
    cal.set(year, month - 1, 1)
    val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val weeks = mutableListOf<List<Int>>()
    var currentWeek = mutableListOf<Int>()
    for (i in 1 until firstDayOfWeek) {
        currentWeek.add(0)
    }
    for (day in 1..daysInMonth) {
        currentWeek.add(day)
        if (currentWeek.size == 7) {
            weeks.add(currentWeek.toList())
            currentWeek.clear()
        }
    }
    if (currentWeek.isNotEmpty()) {
        while (currentWeek.size < 7) {
            currentWeek.add(0)
        }
        weeks.add(currentWeek.toList())
    }
    return weeks
}

private fun buildDayStatusMap(schedules: List<Schedule>): Map<Int, String> {
    val result = mutableMapOf<Int, String>()
    for (s in schedules) {
        val day = s.date.substringAfterLast("-").toIntOrNull() ?: continue
        val existing = result[day]
        val priority = when (s.status) {
            "completed" -> 1
            "pending", "active" -> 2
            "leave" -> 3
            else -> 4
        }
        val existingPriority = when (existing) {
            "已完成" -> 1
            "待上课" -> 2
            "已请假" -> 3
            "异常" -> 4
            else -> 5
        }
        if (existing == null || priority < existingPriority) {
            result[day] = when (s.status) {
                "completed" -> "已完成"
                "pending", "active" -> "待上课"
                "leave" -> "已请假"
                else -> "异常"
            }
        }
    }
    return result
}

@Composable
private fun MiniCalendar(
    schedules: List<Schedule>,
    yearMonth: String,
    todayParts: Triple<Int, Int, Int>,
    onDayClick: (Int) -> Unit,
    onViewFull: () -> Unit
) {
    val dayStatusMap = remember(schedules) { buildDayStatusMap(schedules) }
    val weeks = remember(yearMonth) { buildMonthGrid(yearMonth) }
    val monthLabel = remember(yearMonth) {
        val parts = yearMonth.split("-")
        "${parts[0]}年${parts[1]}月"
    }

    val todayYear = todayParts.first
    val todayMonth = todayParts.second
    val todayDay = todayParts.third
    val displayYear = yearMonth.split("-")[0].toInt()
    val displayMonthNum = yearMonth.split("-")[1].toInt()
    val isCurrentMonth = displayYear == todayYear && displayMonthNum == todayMonth

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "学习课历",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    "详情视图",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onViewFull)
                )
            }
            Spacer(Modifier.height(12.dp))
            val dayHeaders = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                dayHeaders.forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
            weeks.forEach { week ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                    week.forEach { day ->
                        val hasClass = day > 0 && dayStatusMap.containsKey(day)
                        val statusLabel = dayStatusMap[day] ?: ""
                        val statusColor = when (statusLabel) {
                            "已完成" -> MaterialTheme.colorScheme.primary
                            "待上课" -> MaterialTheme.colorScheme.secondary
                            "已请假" -> MaterialTheme.colorScheme.outline
                            "异常" -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                        val statusBg = when (statusLabel) {
                            "已完成" -> MaterialTheme.colorScheme.primaryContainer
                            "待上课" -> MaterialTheme.colorScheme.secondaryContainer
                            "已请假" -> MaterialTheme.colorScheme.surfaceContainerHigh
                            "异常" -> MaterialTheme.colorScheme.errorContainer
                            else -> Color.Transparent
                        }
                        Box(
                            modifier = Modifier.size(36.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day > 0) {
                                val isToday = isCurrentMonth && day == todayDay
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .then(
                                            if (hasClass) Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(statusBg.copy(alpha = 0.6f))
                                                .clickable { onDayClick(day) }
                                                .padding(horizontal = 3.dp, vertical = 1.dp)
                                            else Modifier.padding(horizontal = 3.dp, vertical = 1.dp)
                                        )
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .then(
                                                if (isToday) Modifier
                                                    .clip(CircleShape)
                                                    .background(MaterialTheme.colorScheme.primary)
                                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                                                else Modifier
                                            )
                                    ) {
                                        Text(
                                            day.toString(),
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isToday || hasClass) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                                else if (hasClass) statusColor
                                                else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    if (hasClass) {
                                        Text(
                                            statusLabel,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = statusColor,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                LegendDot(MaterialTheme.colorScheme.primaryContainer, "已完成")
                LegendDot(MaterialTheme.colorScheme.secondaryContainer, "待上课")
                LegendDot(MaterialTheme.colorScheme.surfaceContainerHigh, "已请假")
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge
        )
    }
}

@Composable
private fun LearningHistory(completedSchedules: List<Schedule>) {
    val records = remember(completedSchedules) {
        completedSchedules
            .sortedByDescending { it.date }
            .take(5)
            .map { schedule ->
                LearningRecord(
                    date = schedule.date.takeLast(5),
                    topic = schedule.title,
                    status = "已完成",
                    teacher = ""
                )
            }
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.padding(16.dp)) {
            if (records.isEmpty()) {
                Text(
                    "暂无学习记录",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                records.forEachIndexed { index, record ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                record.topic,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text(
                                    record.date,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.outline
                                )
                            }
                        }
                        Text(
                            record.status,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    if (index < records.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditView(viewModel: AppViewModel, studentId: Long, onBack: () -> Unit) {
    var student by remember { mutableStateOf<Student?>(null) }
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var studentCourses by remember { mutableStateOf<List<StudentCourseWithName>>(emptyList()) }
    var showAddCourse by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var newCourseHours by remember { mutableStateOf("") }
    var newCoursePrice by remember { mutableStateOf("") }
    var courseDropdownExpanded by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    val allCourses by viewModel.allCourses.collectAsState(initial = emptyList())

    LaunchedEffect(studentId) {
        val loaded = viewModel.getStudentById(studentId)
        student = loaded
        loaded?.let {
            name = it.name
            phone = it.phone
        }
        studentCourses = viewModel.getStudentCoursesWithName(studentId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("姓名") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("联系电话") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        HorizontalDivider(
            modifier = Modifier.padding(vertical = 4.dp),
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
        )

        Text("已选课程", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)

        if (studentCourses.isEmpty()) {
            Text("暂无课程", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        studentCourses.forEach { sc ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(sc.courseName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Text("${sc.remainingHours}/${sc.totalHours} 节", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("¥${sc.price}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    IconButton(onClick = {
                        scope.launch {
                            val scRecord = viewModel.getStudentCourses(studentId).find { it.id == sc.id }
                            if (scRecord != null) {
                                viewModel.deleteStudentCourse(scRecord)
                            }
                            studentCourses = viewModel.getStudentCoursesWithName(studentId)
                        }
                    }) {
                        Icon(
                            Icons.Filled.Delete,
                            "删除",
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        if (showAddCourse) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("添加新课", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(8.dp))

                    ExposedDropdownMenuBox(
                        expanded = courseDropdownExpanded,
                        onExpandedChange = { courseDropdownExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCourse?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("选择课程") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseDropdownExpanded) },
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        ExposedDropdownMenu(
                            expanded = courseDropdownExpanded,
                            onDismissRequest = { courseDropdownExpanded = false }
                        ) {
                            allCourses.forEach { course ->
                                val alreadyAdded = studentCourses.any { it.courseId == course.id }
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                course.name,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (alreadyAdded) {
                                                Text(
                                                    "已添加",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    },
                                    onClick = {
                                        if (!alreadyAdded) {
                                            selectedCourse = course
                                            newCoursePrice = if (course.price > 0) course.price.toInt().toString() else ""
                                            newCourseHours = "20"
                                        }
                                        courseDropdownExpanded = false
                                    },
                                    enabled = !alreadyAdded
                                )
                            }
                            if (allCourses.isEmpty()) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            "暂无课程，请在日历主界面添加",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    },
                                    onClick = { courseDropdownExpanded = false },
                                    enabled = false
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = newCourseHours,
                            onValueChange = { newCourseHours = it },
                            label = { Text("课时") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = newCoursePrice,
                            onValueChange = { newCoursePrice = it },
                            label = { Text("¥费用/节") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                showAddCourse = false
                                selectedCourse = null
                                newCourseHours = ""
                                newCoursePrice = ""
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("取消") }
                        Button(
                            onClick = {
                                val course = selectedCourse
                                if (course != null) {
                                    scope.launch {
                                        viewModel.addStudentCourse(
                                            studentId, course.name,
                                            newCourseHours.toIntOrNull() ?: 10,
                                            newCoursePrice.toDoubleOrNull() ?: course.price
                                        )
                                        studentCourses = viewModel.getStudentCoursesWithName(studentId)
                                        selectedCourse = null
                                        newCourseHours = ""
                                        newCoursePrice = ""
                                        showAddCourse = false
                                    }
                                }
                            },
                            enabled = selectedCourse != null,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) { Text("确认添加") }
                    }
                }
            }
        } else {
            OutlinedButton(
                onClick = {
                    showAddCourse = true
                    selectedCourse = null
                    newCourseHours = ""
                    newCoursePrice = ""
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("+ 添加课程")
            }
        }

        Spacer(Modifier.height(8.dp))
        Button(
            onClick = {
                val courseName = if (studentCourses.isNotEmpty()) studentCourses.joinToString(" / ") { it.courseName } else ""
                viewModel.updateStudentInfo(studentId, name, courseName)
                onBack()
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.Done, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("保存", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun ParentsView(
    viewModel: AppViewModel,
    studentId: Long,
    onParentClick: (Long) -> Unit
) {
    var parents by remember { mutableStateOf<List<StudentParent>>(emptyList()) }

    LaunchedEffect(studentId) {
        parents = viewModel.getParentsByStudent(studentId)
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (parents.isEmpty()) {
            item {
                Text(
                    "暂无家长信息",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }
        items(parents) { parent ->
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onParentClick(parent.id) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            parent.name.firstOrNull()?.toString() ?: "?",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            parent.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            "${parent.relation} | ${parent.phone}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        "detail",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ParentDetailView(
    viewModel: AppViewModel,
    studentId: Long,
    parentId: Long
) {
    var parents by remember { mutableStateOf<List<StudentParent>>(emptyList()) }

    LaunchedEffect(studentId) {
        parents = viewModel.getParentsByStudent(studentId)
    }

    val parent = parents.find { it.id == parentId }

    if (parent == null) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("家长信息未找到", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                Modifier.fillMaxWidth().padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        parent.name.take(1),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    parent.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.headlineSmall
                )
                Text(
                    parent.relation,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                InfoRow(Icons.Outlined.Phone, "电话", parent.phone)
                if (parent.address.isNotEmpty()) {
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                    )
                    InfoRow(Icons.Outlined.DateRange, "地址", parent.address)
                }
            }
        }
        OutlinedButton(
            onClick = {},
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Filled.People, null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("拨打电话", style = MaterialTheme.typography.titleLarge)
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            icon,
            null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            "$label: ",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun CalendarView(
    viewModel: AppViewModel,
    studentId: Long,
    onDayClick: (day: Int, yearMonth: String) -> Unit
) {
    var calendarYearMonth by remember { mutableStateOf(viewModel.currentYearMonth.value) }
    val todayDate = viewModel.todayDate.value
    val todayParts = remember(todayDate) {
        val parts = todayDate.split("-")
        Triple(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
    }
    val todayYear = todayParts.first
    val todayMonth = todayParts.second
    val todayDay = todayParts.third
    val displayYear = calendarYearMonth.split("-")[0].toInt()
    val displayMonthNum = calendarYearMonth.split("-")[1].toInt()
    val isViewingCurrentMonth = displayYear == todayYear && displayMonthNum == todayMonth

    val schedules by remember(calendarYearMonth) {
        viewModel.getSchedulesByStudentCoursesAndMonth(studentId, calendarYearMonth)
    }.collectAsState(initial = emptyList())

    val dayStatusMap = remember(schedules) { buildDayStatusMap(schedules) }
    val weeks = remember(calendarYearMonth) { buildMonthGrid(calendarYearMonth) }
    val monthLabel = remember(calendarYearMonth) {
        val parts = calendarYearMonth.split("-")
        "${parts[0]}年${parts[1]}月"
    }

    fun changeMonth(delta: Int) {
        val parts = calendarYearMonth.split("-")
        val year = parts[0].toInt()
        val month = parts[1].toInt()
        val cal = Calendar.getInstance()
        cal.set(year, month - 1 + delta, 1)
        val newYear = cal.get(Calendar.YEAR)
        val newMonth = cal.get(Calendar.MONTH) + 1
        calendarYearMonth = "${newYear}-${newMonth.toString().padStart(2, '0')}"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
            item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { changeMonth(-1) }) {
                    Icon(Icons.Filled.ChevronLeft, "prev")
                }
                Text(
                    monthLabel,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    if (!isViewingCurrentMonth) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable {
                                    calendarYearMonth = viewModel.currentYearMonth.value
                                }
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                "今天",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    IconButton(onClick = { changeMonth(1) }) {
                        Icon(Icons.Filled.ChevronRight, "next")
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            val headers = listOf("日", "一", "二", "三", "四", "五", "六")
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                headers.forEach {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        items(weeks.size) { weekIndex ->
            val week = weeks[weekIndex]
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceAround) {
                week.forEach { day ->
                    val hasClass = day > 0 && dayStatusMap.containsKey(day)
                    val isToday = day > 0 && isViewingCurrentMonth && day == todayDay
                    val statusLabel = dayStatusMap[day] ?: ""
                    val (bgColor, textCol, label) = when (statusLabel) {
                        "已完成" -> Triple(MaterialTheme.colorScheme.primaryContainer, MaterialTheme.colorScheme.primary, "已完成")
                        "待上课" -> Triple(MaterialTheme.colorScheme.secondaryContainer, MaterialTheme.colorScheme.secondary, "待上课")
                        "已请假" -> Triple(MaterialTheme.colorScheme.surfaceContainerHigh, MaterialTheme.colorScheme.outline, "已请假")
                        "异常" -> Triple(MaterialTheme.colorScheme.errorContainer, MaterialTheme.colorScheme.error, "异常")
                        else -> Triple(Color.Transparent, MaterialTheme.colorScheme.onSurface, "")
                    }
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .then(
                                if (hasClass || isToday) Modifier
                                    .clip(CircleShape)
                                    .then(
                                        if (isToday && !hasClass) Modifier.background(
                                            MaterialTheme.colorScheme.primaryContainer
                                        )
                                        else if (hasClass) Modifier.background(bgColor)
                                        else Modifier
                                    )
                                    .then(
                                        if (hasClass) Modifier.clickable { onDayClick(day, calendarYearMonth) }
                                        else Modifier
                                    )
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (day > 0) {
                            Box(
                                modifier = if (isToday) Modifier
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                                    .padding(horizontal = if (hasClass) 7.dp else 5.dp, vertical = 2.dp)
                                else Modifier,
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        day.toString(),
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = if (isToday || hasClass) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isToday) MaterialTheme.colorScheme.onPrimary
                                            else if (hasClass) textCol
                                            else MaterialTheme.colorScheme.onSurface
                                    )
                                    if (hasClass) {
                                        Text(
                                            label,
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = textCol,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                LegendDot(MaterialTheme.colorScheme.primaryContainer, "已完成")
                LegendDot(MaterialTheme.colorScheme.secondaryContainer, "待上课")
                LegendDot(MaterialTheme.colorScheme.surfaceContainerHigh, "已请假")
                LegendDot(MaterialTheme.colorScheme.errorContainer, "异常")
            }
        }
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DayView(
    viewModel: AppViewModel,
    studentId: Long,
    day: Int,
    yearMonth: String,
    onLessonClick: (Long) -> Unit
) {
    val dateStr = remember(day, yearMonth) {
        "${yearMonth}-${day.toString().padStart(2, '0')}"
    }
    val schedules by remember(dateStr) {
        viewModel.getSchedulesByStudentCoursesAndDate(studentId, dateStr)
    }.collectAsState(initial = emptyList())

    val lessons = remember(schedules) {
        schedules.map { schedule ->
            StudentDayLesson(
                scheduleId = schedule.id,
                time = "${schedule.startTime}-${schedule.endTime}",
                topic = schedule.title,
                status = schedule.status,
                room = schedule.room,
                startTime = schedule.startTime,
                endTime = schedule.endTime,
                courseId = schedule.courseId,
                date = schedule.date
            )
        }
    }

    val monthPart = remember(yearMonth) { yearMonth.substringAfterLast("-") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${monthPart}月${day}日 课程安排",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                if (lessons.isNotEmpty()) {
                    Text(
                        "共${lessons.size}节课",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
        }
        if (lessons.isEmpty()) {
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "当日无课程安排",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        items(lessons) { lesson ->
            val (statusColor, statusBg, statusLabel) = when (lesson.status) {
                "completed" -> Triple(
                    MaterialTheme.colorScheme.primary,
                    MaterialTheme.colorScheme.primaryContainer,
                    "已完成"
                )
                "pending", "active" -> Triple(
                    MaterialTheme.colorScheme.tertiary,
                    MaterialTheme.colorScheme.tertiaryContainer,
                    "待上课"
                )
                "leave" -> Triple(
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.errorContainer,
                    "已请假"
                )
                else -> Triple(
                    MaterialTheme.colorScheme.error,
                    MaterialTheme.colorScheme.errorContainer,
                    "异常"
                )
            }
            Card(
                modifier = Modifier.fillMaxWidth().clickable { onLessonClick(lesson.scheduleId) },
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            lesson.startTime,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            lesson.endTime,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                lesson.topic,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Spacer(Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(statusBg)
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    statusLabel,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = statusColor
                                )
                            }
                        }
                        if (lesson.room.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "教室: ${lesson.room}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        Icons.Filled.ChevronRight,
                        "detail",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun LessonDetailView(
    viewModel: AppViewModel,
    studentId: Long,
    scheduleId: Long,
    onViewReport: () -> Unit
) {
    var schedule by remember { mutableStateOf<Schedule?>(null) }
    var courseName by remember { mutableStateOf("") }
    var studentName by remember { mutableStateOf("") }
    var attendanceRecord by remember { mutableStateOf<Attendance?>(null) }

    LaunchedEffect(scheduleId) {
        schedule = viewModel.getScheduleById(scheduleId)
        val s = schedule
        if (s != null) {
            if (s.courseId != null) {
                courseName = viewModel.getCourseById(s.courseId)?.name ?: ""
            }
            val student = viewModel.getStudentById(studentId)
            studentName = student?.name ?: ""
            val attendances = viewModel.getAttendanceBySchedule(scheduleId)
            attendanceRecord = attendances.find { it.studentId == studentId }
        }
    }

    if (schedule == null) {
        Box(Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.Center) {
            Text("课程信息加载中...", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val s = schedule!!
    val (statusColor, statusBg, statusLabel) = when (s.status) {
        "completed" -> Triple(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.primaryContainer,
            "已完成"
        )
        "pending", "active" -> Triple(
            MaterialTheme.colorScheme.tertiary,
            MaterialTheme.colorScheme.tertiaryContainer,
            "待上课"
        )
        "leave" -> Triple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
            "已请假"
        )
        else -> Triple(
            MaterialTheme.colorScheme.error,
            MaterialTheme.colorScheme.errorContainer,
            "异常"
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "课程详情",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.width(12.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                ) {
                    Text(
                        statusLabel,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        s.title,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineSmall
                    )
                    if (courseName.isNotEmpty()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "课程: $courseName",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (studentName.isNotEmpty()) {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            "学生: $studentName",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    DetailInfoRow("日期", s.date)
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    DetailInfoRow("时间", "${s.startTime} - ${s.endTime}")
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    DetailInfoRow("教室", s.room.ifEmpty { "未指定" })
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    DetailInfoRow("类型", when (s.type) {
                        "individual" -> "一对一私教"
                        "group" -> "班级授课"
                        else -> s.type
                    })
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 8.dp),
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                    DetailInfoRow("地点", s.location.ifEmpty { s.room.ifEmpty { "未指定" } })
                }
            }
        }

        if (attendanceRecord != null) {
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(16.dp)) {
                        Text(
                            "出勤记录",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(8.dp))
                        val attLabel = when (attendanceRecord!!.status) {
                            "present" -> "出勤"
                            "leave" -> "请假"
                            "absent" -> "缺勤"
                            else -> attendanceRecord!!.status
                        }
                        DetailInfoRow("考勤状态", attLabel)
                        if (attendanceRecord!!.rating > 0) {
                            DetailInfoRow("评分", "${attendanceRecord!!.rating}分")
                        }
                        if (attendanceRecord!!.note.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "评语: ${attendanceRecord!!.note}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onViewReport,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "查看学习报告",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}

@Composable
private fun DetailInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun PerformanceReportView(
    viewModel: AppViewModel,
    studentId: Long
) {
    var attendanceRecords by remember { mutableStateOf<List<Attendance>>(emptyList()) }
    var student by remember { mutableStateOf<Student?>(null) }

    LaunchedEffect(studentId) {
        attendanceRecords = viewModel.getAttendanceByStudent(studentId)
        student = viewModel.getStudentById(studentId)
    }

    val ratedRecords = attendanceRecords.filter { it.rating > 0 }
    val ratings = remember(ratedRecords) {
        if (ratedRecords.isNotEmpty()) {
            ratedRecords.take(4).mapIndexed { index, att ->
                PerformanceRating(
                    label = "第${index + 1}次课",
                    value = att.rating / 100f
                )
            }
        } else {
            listOf(
                PerformanceRating("综合表现", 0.7f),
                PerformanceRating("专注度", 0.75f),
                PerformanceRating("参与度", 0.8f),
                PerformanceRating("完成度", 0.65f),
            )
        }
    }
    val latestNote = ratedRecords.firstOrNull()?.note ?: ""

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    ratings.forEach { rating ->
                        var animatedValue by remember { mutableStateOf(0f) }
                        val animatedProgress by animateFloatAsState(
                            targetValue = animatedValue,
                            animationSpec = tween(800),
                            label = "progress"
                        )
                        LaunchedEffect(Unit) {
                            animatedValue = rating.value
                        }

                        Text(
                            rating.label,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LinearProgressIndicator(
                                progress = { animatedProgress },
                                modifier = Modifier.weight(1f).height(8.dp),
                                color = MaterialTheme.colorScheme.primary,
                                trackColor = MaterialTheme.colorScheme.primaryContainer,
                                strokeCap = StrokeCap.Round,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${(rating.value * 100).toInt()}%",
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                    Text(
                        "老师评语",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        latestNote.ifEmpty {
                            "${student?.name ?: "学生"}暂无评语记录，完成课程后将更新表现报告。"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        item {
            Text(
                "课堂照片",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.titleLarge
            )
            Spacer(Modifier.height(8.dp))
        }
        item {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth().height(300.dp)
            ) {
                items(5) { _ ->
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.PhotoLibrary,
                            "photo",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
        item {
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.People, null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("分享给家长", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onPrimary)
                }
                OutlinedButton(
                    onClick = {},
                    modifier = Modifier.weight(1f).height(54.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.PhotoLibrary, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("下载报告", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                }
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}
