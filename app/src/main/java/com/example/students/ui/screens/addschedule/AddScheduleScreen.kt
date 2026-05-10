package com.example.students.ui.screens.addschedule

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Course
import com.example.students.data.model.Student
import com.example.students.ui.theme.*
import com.example.students.ui.viewmodel.AppViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class ScheduleType { PRIVATE, GROUP }

data class ScheduleFormData(
    val type: ScheduleType? = null,
    val targetName: String = "",
    val selectedStudentId: Long? = null,
    val selectedCourseId: Long? = null,
    val date: String = "",
    val time: String = "",
    val duration: Int = 60,
    val room: String = ""
)

private fun calculateEndTime(startTime: String, durationMinutes: Int): String {
    val parts = startTime.split(":")
    val hours = parts.getOrNull(0)?.toIntOrNull() ?: 0
    val minutes = parts.getOrNull(1)?.toIntOrNull() ?: 0
    val totalMinutes = hours * 60 + minutes + durationMinutes
    val endHours = (totalMinutes / 60) % 24
    val endMinutes = totalMinutes % 60
    return "%02d:%02d".format(endHours, endMinutes)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddScheduleScreen(viewModel: AppViewModel, onBack: () -> Unit, onSuccess: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    var formData by remember { mutableStateOf(ScheduleFormData()) }
    var searchQuery by remember { mutableStateOf("") }

    val allStudents by viewModel.allStudents.collectAsState(initial = emptyList())
    val allCourses by viewModel.allCourses.collectAsState(initial = emptyList())

    val steps = listOf("类型", "选择", "详情", "确认")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("添加排课") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (currentStep > 0) currentStep-- else onBack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            StepIndicator(
                steps = steps,
                currentStep = currentStep,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { width -> direction * width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> TypeSelectionStep(
                        selectedType = formData.type,
                        onTypeSelected = { type ->
                            formData = formData.copy(type = type)
                            currentStep++
                        }
                    )
                    1 -> SelectTargetStep(
                        searchQuery = searchQuery,
                        onSearchQueryChange = { searchQuery = it },
                        scheduleType = formData.type,
                        students = allStudents,
                        courses = allCourses,
                        selectedName = formData.targetName,
                        onStudentSelected = { student ->
                            formData = formData.copy(
                                targetName = student.name,
                                selectedStudentId = student.id,
                                selectedCourseId = null
                            )
                            currentStep++
                        },
                        onCourseSelected = { course ->
                            formData = formData.copy(
                                targetName = course.name,
                                selectedCourseId = course.id,
                                selectedStudentId = null
                            )
                            currentStep++
                        },
                        onCustomGroupSelected = { name ->
                            formData = formData.copy(
                                targetName = name,
                                selectedCourseId = null,
                                selectedStudentId = null
                            )
                            currentStep++
                        }
                    )
                    2 -> DetailsStep(
                        date = formData.date,
                        time = formData.time,
                        duration = formData.duration,
                        room = formData.room,
                        onDateChange = { formData = formData.copy(date = it) },
                        onTimeChange = { formData = formData.copy(time = it) },
                        onDurationChange = { formData = formData.copy(duration = it) },
                        onRoomChange = { formData = formData.copy(room = it) },
                        onNext = { currentStep++ }
                    )
                    3 -> ConfirmStep(
                        formData = formData,
                        onConfirm = {
                            val scheduleType = if (formData.type == ScheduleType.PRIVATE) "individual" else "group"
                            val hasCustomCourse = formData.type == ScheduleType.GROUP && formData.selectedCourseId == null
                            viewModel.addNewSchedule(
                                title = formData.targetName,
                                studentId = if (formData.type == ScheduleType.PRIVATE) formData.selectedStudentId else null,
                                courseId = formData.selectedCourseId,
                                date = formData.date,
                                startTime = formData.time,
                                endTime = calculateEndTime(formData.time, formData.duration),
                                room = formData.room,
                                type = scheduleType,
                                students = 1,
                                courseName = if (hasCustomCourse) formData.targetName else null
                            )
                            onSuccess()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentStep
            val isCurrent = index == currentStep
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (index < currentStep) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = if (isCurrent) MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold
                    ) else MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TypeSelectionStep(
    selectedType: ScheduleType?,
    onTypeSelected: (ScheduleType) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "请选择排课类型",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(32.dp))

        ScheduleTypeCard(
            title = "一对一私教",
            description = "针对单个学生定制教学计划",
            isSelected = selectedType == ScheduleType.PRIVATE,
            onClick = { onTypeSelected(ScheduleType.PRIVATE) }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ScheduleTypeCard(
            title = "班级授课",
            description = "面向班级进行集体教学",
            isSelected = selectedType == ScheduleType.GROUP,
            onClick = { onTypeSelected(ScheduleType.GROUP) }
        )
    }
}

@Composable
private fun ScheduleTypeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.outline

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SelectTargetStep(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    scheduleType: ScheduleType?,
    students: List<Student>,
    courses: List<Course>,
    selectedName: String,
    onStudentSelected: (Student) -> Unit,
    onCourseSelected: (Course) -> Unit,
    onCustomGroupSelected: (String) -> Unit
) {
    var customName by remember { mutableStateOf("") }
    val placeholder = when (scheduleType) {
        ScheduleType.PRIVATE -> "搜索学生姓名..."
        ScheduleType.GROUP -> "搜索班级名称..."
        null -> "搜索..."
    }

    val isGroup = scheduleType == ScheduleType.GROUP
    val targetLabel = if (isGroup) "班级" else "学生"

    val filtered = if (scheduleType == ScheduleType.PRIVATE) {
        if (searchQuery.isBlank()) students
        else students.filter { it.name.contains(searchQuery, ignoreCase = true) }
    } else if (scheduleType == ScheduleType.GROUP) {
        if (searchQuery.isBlank()) courses
        else courses.filter { it.name.contains(searchQuery, ignoreCase = true) }
    } else emptyList<Any>()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = { Text(placeholder) },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Text(
                text = "选择${targetLabel}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        when (scheduleType) {
            ScheduleType.PRIVATE -> {
                items(filtered.size) { index ->
                    val student = filtered[index] as Student
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onStudentSelected(student) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedName == student.name) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                    ) {
                        Text(
                            text = student.name,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(16.dp),
                            color = if (selectedName == student.name) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            ScheduleType.GROUP -> {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "创建新班级",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = customName,
                                    onValueChange = { customName = it },
                                    placeholder = { Text("输入新班级名称，如：暑期集训班") },
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true
                                )
                                if (customName.isNotBlank()) {
                                    Spacer(Modifier.width(8.dp))
                                    Button(
                                        onClick = { onCustomGroupSelected(customName) },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("创建并使用")
                                    }
                                }
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                if (filtered.isNotEmpty()) {
                    item {
                        Text(
                            text = "已有班级",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }
                }

                items(filtered.size) { index ->
                    val course = filtered[index] as Course
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onCourseSelected(course) },
                        shape = RoundedCornerShape(8.dp),
                        color = if (selectedName == course.name) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = course.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = if (selectedName == course.name) MaterialTheme.colorScheme.onPrimaryContainer
                                        else MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${course.type} · ${course.duration}分钟 · ¥${course.price}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
            null -> {}
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailsStep(
    date: String,
    time: String,
    duration: Int,
    room: String,
    onDateChange: (String) -> Unit,
    onTimeChange: (String) -> Unit,
    onDurationChange: (Int) -> Unit,
    onRoomChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val durations = listOf(45, 60, 90, 120)
    var customDurationText by remember { mutableStateOf(if (duration in durations) "" else duration.toString()) }
    var useCustom by remember { mutableStateOf(duration !in durations) }
    val isFormValid = date.isNotBlank() && time.isNotBlank() && room.isNotBlank() && duration > 0

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = if (date.isNotBlank()) {
            try {
                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                sdf.parse(date)?.time
            } catch (_: Exception) { null }
        } else null
    )

    val timeParts = remember(time) {
        when {
            time.isNotBlank() -> {
                val parts = time.split(":")
                val h = parts.getOrNull(0)?.toIntOrNull() ?: 8
                val m = parts.getOrNull(1)?.toIntOrNull() ?: 0
                h to m
            }
            else -> 8 to 0
        }
    }

    val timePickerState = rememberTimePickerState(
        initialHour = timeParts.first,
        initialMinute = timeParts.second,
        is24Hour = true
    )

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            val formatted = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(millis))
                            onDateChange(formatted)
                        }
                        showDatePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("取消") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("选择时间", fontWeight = FontWeight.Bold) },
            text = {
                TimePicker(state = timePickerState)
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val h = timePickerState.hour
                        val m = timePickerState.minute
                        onTimeChange(String.format("%02d:%02d", h, m))
                        showTimePicker = false
                    }
                ) { Text("确定") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("取消") }
            },
            shape = RoundedCornerShape(16.dp)
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "课程详情",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
        }

        item {
            OutlinedTextField(
                value = date,
                onValueChange = onDateChange,
                label = { Text("上课日期") },
                placeholder = { Text("点击日历图标选择日期") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Text("📅", style = MaterialTheme.typography.titleMedium)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = time,
                onValueChange = onTimeChange,
                label = { Text("上课时间") },
                placeholder = { Text("点击时钟图标选择时间") },
                trailingIcon = {
                    IconButton(onClick = { showTimePicker = true }) {
                        Text("🕐", style = MaterialTheme.typography.titleMedium)
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Text(
                text = "课时时长",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                durations.forEach { d ->
                    val isSelected = !useCustom && duration == d
                    OutlinedButton(
                        onClick = {
                            useCustom = false
                            customDurationText = ""
                            onDurationChange(d)
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(
                            if (isSelected) 2.dp else 1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.outline
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = if (isSelected) MaterialTheme.colorScheme.primaryContainer
                                else Color.Transparent
                        )
                    ) {
                        Text(
                            text = "${d}min",
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                                else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                OutlinedButton(
                    onClick = {
                        useCustom = true
                        onDurationChange(customDurationText.toIntOrNull() ?: 0)
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(
                        if (useCustom) 2.dp else 1.dp,
                        if (useCustom) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.outline
                    ),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (useCustom) MaterialTheme.colorScheme.primaryContainer
                            else Color.Transparent
                    )
                ) {
                    Text(
                        text = "自定义",
                        color = if (useCustom) MaterialTheme.colorScheme.onPrimaryContainer
                            else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            if (useCustom) {
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = customDurationText,
                    onValueChange = {
                        customDurationText = it
                        onDurationChange(it.toIntOrNull() ?: duration)
                    },
                    label = { Text("自定义时长（分钟）") },
                    placeholder = { Text("输入分钟数...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        }

        item {
            OutlinedTextField(
                value = room,
                onValueChange = onRoomChange,
                label = { Text("上课教室") },
                placeholder = { Text("例如：A101") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid,
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("下一步")
            }
        }
    }
}

@Composable
private fun ConfirmStep(
    formData: ScheduleFormData,
    onConfirm: () -> Unit
) {
    val typeLabel = when (formData.type) {
        ScheduleType.PRIVATE -> "一对一私教"
        ScheduleType.GROUP -> "班级授课"
        null -> "未选择"
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Text(
            text = "确认排课信息",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                SummaryRow("排课类型", typeLabel)
                SummaryRow("${if (formData.type == ScheduleType.PRIVATE) "学生" else "班级"}", formData.targetName)
                SummaryRow("上课日期", formData.date)
                SummaryRow("上课时间", formData.time)
                SummaryRow("课时时长", "${formData.duration} 分钟")
                SummaryRow("上课教室", formData.room)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onConfirm,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("确认发布排课")
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
