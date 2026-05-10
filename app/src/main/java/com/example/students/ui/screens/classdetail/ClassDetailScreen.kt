package com.example.students.ui.screens.classdetail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Attendance
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.components.StatusBadge
import com.example.students.ui.viewmodel.AppViewModel
import kotlinx.coroutines.launch

private data class StudentAttendance(
    val id: Long, val name: String, var status: String = "present"
)

private data class StudentPerformance(
    val id: Long, val name: String, var rating: Int = 0, var note: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassDetailScreen(viewModel: AppViewModel, scheduleId: Long, onBack: () -> Unit) {
    var currentSubView by remember { mutableStateOf("attendance") }
    var schedule by remember { mutableStateOf<Schedule?>(null) }
    var enrolledStudents by remember { mutableStateOf<List<Student>>(emptyList()) }

    LaunchedEffect(scheduleId) {
        schedule = viewModel.getScheduleById(scheduleId)
        val s = schedule ?: return@LaunchedEffect
        enrolledStudents = when {
            s.studentId != null -> {
                val student = viewModel.getStudentById(s.studentId)
                if (student != null) listOf(student) else emptyList()
            }
            s.courseId != null -> {
                viewModel.getStudentsByCourseId(s.courseId)
            }
            else -> emptyList()
        }
    }

    fun navigateTo(view: String) {
        currentSubView = view
    }

    val goBack: () -> Unit = {
        if (currentSubView != "attendance") {
            currentSubView = "attendance"
        } else {
            onBack()
        }
    }

    Scaffold(
        topBar = {
            when (currentSubView) {
                "attendance" -> DetailTopBar("课堂管理", onBack)
                "performance" -> DetailTopBar("课堂表现", goBack)
                "success" -> DetailTopBar("", goBack)
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (currentSubView) {
                "attendance" -> AttendanceView(
                    schedule = schedule,
                    students = enrolledStudents,
                    onPerformance = { navigateTo("performance") },
                    onSubmitSuccess = { records ->
                        viewModel.submitAttendance(scheduleId, records)
                        navigateTo("success")
                    }
                )
                "performance" -> PerformanceView(
                    schedule = schedule,
                    students = enrolledStudents,
                    onSuccess = { navigateTo("success") }
                )
                "success" -> SuccessView(onBack = goBack)
            }
        }
    }
}

@Composable
private fun AttendanceView(
    schedule: Schedule?,
    students: List<Student>,
    onPerformance: () -> Unit,
    onSubmitSuccess: (List<Triple<Long, String, String>>) -> Unit
) {
    var showConfirm by remember { mutableStateOf(false) }

    val studentAttendances = remember(students) {
        mutableStateListOf<StudentAttendance>().also { list ->
            list.addAll(students.map { StudentAttendance(it.id, it.name, "present") })
        }
    }
    val presentCount = studentAttendances.count { it.status == "present" }
    val leaveCount = studentAttendances.count { it.status == "leave" }
    val absentCount = studentAttendances.count { it.status == "absent" }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            CourseInfoCard(schedule)
        }
        item {
            AttendanceStatsCard(presentCount, leaveCount, absentCount)
        }
        item {
            StudentAttendanceList(studentAttendances, onPerformance)
        }
        item {
            Button(
                onClick = { showConfirm = true },
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("提交考勤", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showConfirm) {
        AttendanceConfirmDialog(
            onConfirm = {
                val records = studentAttendances.map { Triple(it.id, it.status, "") }
                onSubmitSuccess(records)
            },
            onDismiss = { showConfirm = false }
        )
    }
}

@Composable
private fun CourseInfoCard(schedule: Schedule?) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Text(
                schedule?.title ?: "课程名称",
                fontWeight = FontWeight.Bold,
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Schedule, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "${schedule?.startTime ?: ""} - ${schedule?.endTime ?: ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        schedule?.room ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun AttendanceStatsCard(present: Int, leave: Int, absent: Int) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            StatBadge("已到", "$present", MaterialTheme.colorScheme.primary)
            StatBadge("请假", "$leave", MaterialTheme.colorScheme.tertiary)
            StatBadge("缺课", "$absent", MaterialTheme.colorScheme.error)
        }
    }
}

@Composable
private fun StatBadge(label: String, count: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(count, style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun StudentAttendanceList(
    students: SnapshotStateList<StudentAttendance>,
    onPerformance: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("学生列表", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Text(
                    "课堂表现 >",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable(onClick = onPerformance)
                )
            }
            Spacer(Modifier.height(8.dp))
            if (students.isEmpty()) {
                Text("暂无学生数据", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            val snapshot = students.toList()
            snapshot.forEach { student ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            student.name.takeLast(1),
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Text(student.name, fontWeight = FontWeight.Medium,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f))
                    AttendanceToggle(student) { newStatus ->
                        val idx = students.indexOfFirst { it.id == student.id }
                        if (idx >= 0) {
                            students.removeAt(idx)
                            students.add(idx, student.copy(status = newStatus))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AttendanceToggle(
    student: StudentAttendance,
    onStatusChange: (String) -> Unit
) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        val statuses = listOf(
            Triple("present", "已到", MaterialTheme.colorScheme.primary),
            Triple("leave", "请假", MaterialTheme.colorScheme.tertiary),
            Triple("absent", "旷课", MaterialTheme.colorScheme.error),
        )
        statuses.forEach { (value, label, color) ->
            val isSelected = student.status == value
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .then(if (isSelected) Modifier.background(color)
                        else Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow))
                    .clickable { onStatusChange(value) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(label, style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun AttendanceConfirmDialog(onConfirm: () -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("确认提交", fontWeight = FontWeight.Bold) },
        text = { Text("提交后将不可修改，确定要提交本次考勤记录吗？") },
        confirmButton = { Button(onClick = onConfirm) { Text("确认提交") } },
        dismissButton = { OutlinedButton(onClick = onDismiss) { Text("取消") } },
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
private fun PerformanceView(
    schedule: Schedule?,
    students: List<Student>,
    onSuccess: () -> Unit
) {
    val performances = remember(students) {
        mutableStateListOf<StudentPerformance>().also { list ->
            list.addAll(students.map { StudentPerformance(it.id, it.name, 0, "") })
        }
    }
    var selectedStudentId by remember { mutableStateOf(students.firstOrNull()?.id ?: 0L) }
    var currentNote by remember { mutableStateOf("") }

    val selectedStudent = performances.find { it.id == selectedStudentId } ?: performances.firstOrNull()

    if (performances.isEmpty()) {
        Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.Center) {
            Text("暂无学生数据", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("学生选择", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                performances.forEach { student ->
                    val isSelected = student.id == selectedStudentId
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .then(if (isSelected) Modifier.background(
                                MaterialTheme.colorScheme.primaryContainer)
                                else Modifier.background(MaterialTheme.colorScheme.surfaceContainerLow))
                            .clickable {
                                selectedStudentId = student.id
                                currentNote = student.note
                            }
                            .padding(horizontal = 16.dp, vertical = 10.dp)
                    ) {
                        Text(student.name, style = MaterialTheme.typography.bodySmall,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        if (selectedStudent != null) {
            item {
                Text("评分", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(selectedStudent.name, fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(8.dp))
                        StarRating(selectedStudent.rating) { rating ->
                            val i = performances.indexOfFirst { it.id == selectedStudent.id }
                            if (i >= 0) {
                                val updated = performances[i].copy(rating = rating)
                                performances.removeAt(i)
                                performances.add(i, updated)
                            }
                        }
                    }
                }
            }

            item {
                Text("课堂笔记", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = currentNote,
                    onValueChange = { currentNote = it },
                    placeholder = { Text("输入课堂表现评价...") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    )
                )
            }

            item {
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = {
                        val idx = performances.indexOfFirst { it.id == selectedStudent.id }
                        if (idx >= 0) {
                            val updated = performances[idx].copy(note = currentNote)
                            performances.removeAt(idx)
                            performances.add(idx, updated)
                        }
                        onSuccess()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("保存并完成", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun StarRating(rating: Int, onRatingChange: (Int) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        (1..5).forEach { star ->
            IconButton(onClick = { onRatingChange(star) }, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = if (star <= rating) Icons.Filled.Star else Icons.Filled.StarBorder,
                    contentDescription = "star $star",
                    tint = if (star <= rating) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun SuccessView(onBack: () -> Unit) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) { scale.animateTo(1f, animationSpec = tween(600)) }
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.CheckCircle, "success",
                tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        }
        Spacer(Modifier.height(24.dp))
        Text("提交成功！", fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.onSurface)
        Spacer(Modifier.height(8.dp))
        Text("考勤记录已保存", style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("返回", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        }
    }
}
