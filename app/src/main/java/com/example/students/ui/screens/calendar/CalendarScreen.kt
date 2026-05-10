package com.example.students.ui.screens.calendar

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Schedule
import com.example.students.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: AppViewModel,
    isStudentCalendar: Boolean = false,
    studentId: Long? = null,
    onAddSchedule: () -> Unit,
    onDayClick: (String) -> Unit,
    onLessonClick: (Long) -> Unit
) {
    var viewMode by remember { mutableStateOf(0) }
    var editingSchedule by remember { mutableStateOf<Schedule?>(null) }
    var showDeleteConfirm by remember { mutableStateOf<Schedule?>(null) }
    val cachedToday = viewModel.todayDate.value
    val cachedYearMonth = viewModel.currentYearMonth.value
    val initialYear = cachedYearMonth.split("-")[0].toInt()
    val initialMonth = cachedYearMonth.split("-")[1].toInt() - 1
    var displayYear by remember { mutableStateOf(initialYear) }
    var displayMonth by remember { mutableStateOf(initialMonth) }

    val yearMonth = remember(displayYear, displayMonth) {
        String.format(Locale.getDefault(), "%04d-%02d", displayYear, displayMonth + 1)
    }

    val schedulesFlow = remember(displayYear, displayMonth, isStudentCalendar, studentId) {
        if (isStudentCalendar && studentId != null) {
            viewModel.getSchedulesByStudentAndMonth(studentId, yearMonth)
        } else {
            viewModel.getSchedulesByMonth(yearMonth)
        }
    }

    val monthSchedules by schedulesFlow.collectAsState(initial = emptyList())

    val statusMap = remember(monthSchedules) {
        val map = mutableMapOf<Int, String>()
        val grouped = monthSchedules.groupBy { schedule ->
            try { schedule.date.split("-")[2].toInt() } catch (_: Exception) { null }
        }
        for ((day, schedules) in grouped) {
            if (day == null) continue
            val hasActive = schedules.any { it.status == "active" }
            val hasCompleted = schedules.any { it.status == "completed" }
            val hasLeave = schedules.any { it.status == "leave" }
            val hasPending = schedules.any { it.status == "pending" }
            val hasStudents = schedules.any { it.studentId != null || it.courseId != null }
            when {
                !hasStudents -> map[day] = "无学生"
                hasActive -> map[day] = "进行中"
                hasCompleted -> map[day] = "已完成"
                hasLeave -> map[day] = "已请假"
                hasPending -> map[day] = "待上课"
            }
        }
        map
    }

    val todayDate = viewModel.todayDate.value
    val todayParts = remember(todayDate) {
        val parts = todayDate.split("-")
        Triple(parts[0].toInt(), parts[1].toInt() - 1, parts[2].toInt())
    }
    val isViewingCurrentMonth = displayYear == todayParts.first && displayMonth == todayParts.second

    val weekSchedules = remember(monthSchedules, todayDate) {
        val cal = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val weekDays = (0..6).map {
            val date = dateFormat.format(cal.time)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            date
        }
        monthSchedules.filter { it.date in weekDays }
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddSchedule,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, "添加课程")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${displayYear}年${displayMonth + 1}月",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (!isViewingCurrentMonth) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .clickable { displayYear = todayParts.first; displayMonth = todayParts.second }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text("今天", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                    NavIconButton({ if (displayMonth == 0) { displayMonth = 11; displayYear-- } else displayMonth-- }, Icons.Filled.ChevronLeft, "上月")
                    NavIconButton({ if (displayMonth == 11) { displayMonth = 0; displayYear++ } else displayMonth++ }, Icons.Filled.ChevronRight, "下月")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf("月视图", "周视图", "课程列表").forEachIndexed { index, label ->
                    val selected = index == viewMode
                    val chipBg by animateColorAsState(
                        targetValue = if (selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerLow,
                        label = "toggle$index"
                    )
                    Box(
                        modifier = Modifier.weight(1f).clip(RoundedCornerShape(8.dp)).background(chipBg).clickable { viewMode = index }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal, color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            when (viewMode) {
                0 -> MonthView(displayYear, displayMonth, statusMap, todayParts, onDayClick)
                1 -> WeekView(weekSchedules, onLessonClick)
                2 -> CourseListView(monthSchedules, { editingSchedule = it }, { showDeleteConfirm = it }, onLessonClick)
            }
        }
    }

    editingSchedule?.let { schedule ->
        ScheduleEditDialog(schedule, viewModel) { editingSchedule = null }
    }

    showDeleteConfirm?.let { schedule ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = null },
            title = { Text("确认删除", fontWeight = FontWeight.Bold) },
            text = { Text("确定要删除「${schedule.title}」(${schedule.date}) 吗？") },
            confirmButton = {
                Button(onClick = { viewModel.deleteScheduleById(schedule.id) { showDeleteConfirm = null } }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirm = null }) { Text("取消") } },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun NavIconButton(onClick: () -> Unit, icon: androidx.compose.ui.graphics.vector.ImageVector, desc: String) {
    Box(modifier = Modifier.size(36.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceContainerLow).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Icon(icon, desc, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(20.dp))
    }
}

@Composable
private fun MonthView(year: Int, month: Int, statusMap: Map<Int, String>, todayParts: Triple<Int, Int, Int>, onDayClick: (String) -> Unit) {
    val dayHeaders = listOf("日", "一", "二", "三", "四", "五", "六")
    val cal = remember(year, month) { GregorianCalendar(year, month, 1) }
    val firstDayIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
    val maxDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val (todayYear, todayMonth, todayDay) = todayParts
    val totalCells = firstDayIndex + maxDays
    val rows = (totalCells + 6) / 7

    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            dayHeaders.forEach { day -> Text(day, modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Spacer(Modifier.height(4.dp))
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0..6) {
                    val dayNumber = row * 7 + col - firstDayIndex + 1
                    if (dayNumber in 1..maxDays) {
                        val isToday = dayNumber == todayDay && month == todayMonth && year == todayYear
                        val dateStr = String.format(Locale.getDefault(), "%04d-%02d-%02d", year, month + 1, dayNumber)
                        DayCell(dayNumber, isToday, statusMap[dayNumber], { onDayClick(dateStr) }, Modifier.weight(1f))
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(day: Int, isToday: Boolean, status: String?, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val bgColor = if (isToday) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
    val (badgeColor, badgeBg) = when (status) {
        "进行中" -> Color(0xFF1976D2) to Color(0xFFE3F2FD)
        "已完成" -> Color(0xFF2E7D32) to Color(0xFFE8F5E9)
        "待上课" -> Color(0xFFEF6C00) to Color(0xFFFFF3E0)
        "已请假" -> Color(0xFFC62828) to Color(0xFFFFEBEE)
        "无学生" -> Color(0xFF757575) to Color(0xFFF5F5F5)
        else -> MaterialTheme.colorScheme.outline to MaterialTheme.colorScheme.surfaceVariant
    }

    Column(
        modifier = modifier.aspectRatio(1f).clip(RoundedCornerShape(8.dp)).background(bgColor).then(
            if (status != null) Modifier.clickable(onClick = onClick) else Modifier
        ).then(
            if (isToday) Modifier.background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), RoundedCornerShape(8.dp)) else Modifier
        ).padding(2.dp),
        horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier.size(if (isToday) 28.dp else 24.dp).then(
                if (isToday) Modifier.clip(CircleShape).background(MaterialTheme.colorScheme.primary) else Modifier
            ), contentAlignment = Alignment.Center
        ) {
            Text(day.toString(), style = MaterialTheme.typography.bodySmall, fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal, color = if (isToday) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface)
        }
        if (status != null) {
            Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(badgeBg).padding(horizontal = 3.dp, vertical = 1.dp)) {
                Text(status, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = badgeColor, maxLines = 1)
            }
        }
    }
}

@Composable
private fun WeekView(schedules: List<Schedule>, onLessonClick: (Long) -> Unit) {
    val sorted = remember(schedules) { schedules.sortedBy { it.startTime } }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item { Text("本周课程", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 8.dp)) }
        if (sorted.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) { Text("本周暂无课程安排", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        items(sorted) { schedule -> LessonCard(schedule) { onLessonClick(schedule.id) } }
        item { Spacer(Modifier.height(72.dp)) }
    }
}

@Composable
private fun LessonCard(schedule: Schedule, onClick: () -> Unit) {
    val (statusColor, statusBg) = when (schedule.status) {
        "completed" -> MaterialTheme.colorScheme.primary to MaterialTheme.colorScheme.primaryContainer
        "pending", "active" -> MaterialTheme.colorScheme.tertiary to MaterialTheme.colorScheme.tertiaryContainer
        "leave" -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.error to MaterialTheme.colorScheme.errorContainer
    }
    val statusLabel = when (schedule.status) { "completed" -> "已完成"; "pending", "active" -> "待上课"; "leave" -> "请假"; else -> "异常" }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(52.dp)) {
            Text(schedule.startTime, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(schedule.endTime, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.outline)
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(schedule.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(schedule.room, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(6.dp)).background(statusBg).padding(horizontal = 8.dp, vertical = 4.dp)) {
            Text(statusLabel, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = statusColor)
        }
    }
}

@Composable
private fun CourseListView(schedules: List<Schedule>, onEditClick: (Schedule) -> Unit, onDeleteClick: (Schedule) -> Unit, onLessonClick: (Long) -> Unit) {
    val sorted = remember(schedules) { schedules.sortedWith(compareBy({ it.date }, { it.startTime })) }
    val grouped = remember(sorted) { sorted.groupBy { it.date } }
    LazyColumn(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (sorted.isEmpty()) {
            item { Box(Modifier.fillMaxWidth().padding(vertical = 48.dp), contentAlignment = Alignment.Center) { Text("本月暂无课程", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant) } }
        }
        grouped.forEach { (date, daySchedules) ->
            item { Text(date, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp, bottom = 4.dp)) }
            items(daySchedules) { schedule -> CourseListCard(schedule, { onEditClick(schedule) }, { onDeleteClick(schedule) }, { onLessonClick(schedule.id) }) }
        }
        item { Spacer(Modifier.height(80.dp)) }
    }
}

@Composable
private fun CourseListCard(schedule: Schedule, onEditClick: () -> Unit, onDeleteClick: () -> Unit, onLessonClick: () -> Unit) {
    val (statusColor, statusLabel) = when (schedule.status) {
        "completed" -> Color(0xFF2E7D32) to "已完成"; "active" -> Color(0xFF1976D2) to "进行中"
        "pending" -> Color(0xFFEF6C00) to "待上课"; "leave" -> Color(0xFFC62828) to "已请假"
        else -> MaterialTheme.colorScheme.outline to "未知"
    }
    Row(
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surface).clickable(onClick = onLessonClick).padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(48.dp)) {
            Text(schedule.startTime, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Text(schedule.endTime, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(schedule.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold, maxLines = 1)
            if (schedule.room.isNotEmpty()) { Text(schedule.room, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Box(modifier = Modifier.clip(RoundedCornerShape(4.dp)).background(statusColor.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
            Text(statusLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
        }
        IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Edit, "编辑", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp)) }
        IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) { Icon(Icons.Filled.Delete, "删除", tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp)) }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ScheduleEditDialog(schedule: Schedule, viewModel: AppViewModel, onDismiss: () -> Unit) {
    var editTitle by remember { mutableStateOf(schedule.title) }
    var editDate by remember { mutableStateOf(schedule.date) }
    var editStartTime by remember { mutableStateOf(schedule.startTime) }
    var editEndTime by remember { mutableStateOf(schedule.endTime) }
    var editRoom by remember { mutableStateOf(schedule.room) }
    var editStatus by remember { mutableStateOf(schedule.status) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = try { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(editDate)?.time } catch (_: Exception) { null })
    val startParts = editStartTime.split(":").let { (it.getOrNull(0)?.toIntOrNull() ?: 8) to (it.getOrNull(1)?.toIntOrNull() ?: 0) }
    val endParts = editEndTime.split(":").let { (it.getOrNull(0)?.toIntOrNull() ?: 9) to (it.getOrNull(1)?.toIntOrNull() ?: 0) }
    val startTimePickerState = rememberTimePickerState(initialHour = startParts.first, initialMinute = startParts.second, is24Hour = true)
    val endTimePickerState = rememberTimePickerState(initialHour = endParts.first, initialMinute = endParts.second, is24Hour = true)

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { datePickerState.selectedDateMillis?.let { editDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it)) }; showDatePicker = false }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("取消") } }
        ) { DatePicker(state = datePickerState) }
    }

    if (showStartTimePicker) {
        AlertDialog(
            onDismissRequest = { showStartTimePicker = false }, title = { Text("开始时间", fontWeight = FontWeight.Bold) }, text = { TimePicker(state = startTimePickerState) },
            confirmButton = { TextButton(onClick = { editStartTime = String.format("%02d:%02d", startTimePickerState.hour, startTimePickerState.minute); showStartTimePicker = false }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showStartTimePicker = false }) { Text("取消") } }, shape = RoundedCornerShape(16.dp)
        )
    }

    if (showEndTimePicker) {
        AlertDialog(
            onDismissRequest = { showEndTimePicker = false }, title = { Text("结束时间", fontWeight = FontWeight.Bold) }, text = { TimePicker(state = endTimePickerState) },
            confirmButton = { TextButton(onClick = { editEndTime = String.format("%02d:%02d", endTimePickerState.hour, endTimePickerState.minute); showEndTimePicker = false }) { Text("确定") } },
            dismissButton = { TextButton(onClick = { showEndTimePicker = false }) { Text("取消") } }, shape = RoundedCornerShape(16.dp)
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("编辑课程", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge) },
        text = {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item { OutlinedTextField(editTitle, { editTitle = it }, label = { Text("课程名称") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true) }
                item { OutlinedTextField(editDate, { editDate = it }, label = { Text("日期") }, trailingIcon = { TextButton(onClick = { showDatePicker = true }) { Text("📅") } }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true) }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(editStartTime, { editStartTime = it }, label = { Text("开始") }, trailingIcon = { TextButton(onClick = { showStartTimePicker = true }) { Text("🕐") } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true)
                        OutlinedTextField(editEndTime, { editEndTime = it }, label = { Text("结束") }, trailingIcon = { TextButton(onClick = { showEndTimePicker = true }) { Text("🕐") } }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(10.dp), singleLine = true)
                    }
                }
                item { OutlinedTextField(editRoom, { editRoom = it }, label = { Text("教室") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(10.dp), singleLine = true) }
                item {
                    Text("课程状态", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("pending" to "待上课", "active" to "进行中", "completed" to "已完成", "leave" to "已请假").forEach { (value, label) ->
                            val sel = editStatus == value
                            val chipColor = when (value) { "pending" -> Color(0xFFEF6C00); "active" -> Color(0xFF1976D2); "completed" -> Color(0xFF2E7D32); "leave" -> Color(0xFFC62828); else -> MaterialTheme.colorScheme.primary }
                            OutlinedButton(onClick = { editStatus = value }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) chipColor else MaterialTheme.colorScheme.outline),
                                colors = ButtonDefaults.outlinedButtonColors(containerColor = if (sel) chipColor.copy(alpha = 0.12f) else Color.Transparent)
                            ) { Text(label, style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) chipColor else MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1) }
                        }
                    }
                }
            }
        },
        confirmButton = { Button(onClick = { viewModel.updateScheduleInfo(schedule.id, editTitle, editDate, editStartTime, editEndTime, editRoom, editStatus, schedule.type, schedule.courseId, schedule.studentId); onDismiss() }) { Text("保存") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("取消") } },
        shape = RoundedCornerShape(20.dp)
    )
}
