package com.example.students.ui.screens.lowlessonreminders

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.students.data.model.Student
import com.example.students.data.model.StudentParent
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.viewmodel.AppViewModel

enum class LowLessonView {
    LIST, STUDENT_DETAIL, MESSAGE_TEMPLATE, SUCCESS
}

@Composable
fun LowLessonRemindersScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var internalView by remember { mutableStateOf(LowLessonView.LIST) }
    var selectedStudent by remember { mutableStateOf<Student?>(null) }
    var selectedParent by remember { mutableStateOf<StudentParent?>(null) }
    val scope = rememberCoroutineScope()

    val lowLessonStudents by viewModel.lowLessonStudents.collectAsState(initial = emptyList())

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DetailTopBar(
            title = when (internalView) {
                LowLessonView.LIST -> "低课时提醒"
                LowLessonView.STUDENT_DETAIL -> "学生详情"
                LowLessonView.MESSAGE_TEMPLATE -> "发送提醒"
                LowLessonView.SUCCESS -> "发送成功"
            },
            onBack = {
                when (internalView) {
                    LowLessonView.LIST -> onBack()
                    else -> {
                        internalView = LowLessonView.LIST
                        selectedStudent = null
                        selectedParent = null
                    }
                }
            }
        )

        when (internalView) {
            LowLessonView.LIST -> ListView(
                students = lowLessonStudents,
                onStudentClick = { student ->
                    selectedStudent = student
                    internalView = LowLessonView.STUDENT_DETAIL
                }
            )
            LowLessonView.STUDENT_DETAIL -> StudentDetailView(
                student = selectedStudent,
                parent = selectedParent,
                onLoadParent = { studentId ->
                    scope.launch {
                        viewModel.getParentsByStudent(studentId).firstOrNull()?.let {
                            selectedParent = it
                        }
                    }
                },
                onSendReminder = {
                    internalView = LowLessonView.MESSAGE_TEMPLATE
                }
            )
            LowLessonView.MESSAGE_TEMPLATE -> MessageTemplateView(
                student = selectedStudent,
                parent = selectedParent,
                onConfirm = {
                    scope.launch {
                        viewModel.insertNotification(com.example.students.data.model.Notification(
                            type = "academic",
                            title = "续费提醒已发送",
                            content = "已向${selectedStudent?.name ?: ""}的家长发送续费提醒",
                            time = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
                            priority = "medium",
                            isRead = false
                        ))
                        internalView = LowLessonView.SUCCESS
                    }
                }
            )
            LowLessonView.SUCCESS -> SuccessView(
                onBack = {
                    internalView = LowLessonView.LIST
                    selectedStudent = null
                    selectedParent = null
                }
            )
        }
    }
}

@Composable
private fun ListView(
    students: List<Student>,
    onStudentClick: (Student) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            AlertBanner(count = students.size)
            Spacer(modifier = Modifier.height(8.dp))
        }

        items(students) { student ->
            LowLessonCard(
                student = student,
                onClick = { onStudentClick(student) }
            )
        }

        if (students.isEmpty()) {
            item {
                Text(
                    text = "目前没有课时不足的学生",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(32.dp)
                )
            }
        }
    }
}

@Composable
private fun AlertBanner(count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.errorContainer)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Warning,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onErrorContainer,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "共 $count 名学生剩余课时不足3节，请及时联系家长续费。",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onErrorContainer
        )
    }
}

@Composable
private fun LowLessonCard(
    student: Student,
    onClick: () -> Unit
) {
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = student.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = student.course,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = "剩余 ${student.hours} 节",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
            }
        }
    }
}

@Composable
private fun StudentDetailView(
    student: Student?,
    parent: StudentParent?,
    onLoadParent: (Long) -> Unit,
    onSendReminder: () -> Unit
) {
    if (student == null) return

    LaunchedEffect(student.id) {
        onLoadParent(student.id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = student.name,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(16.dp))

        DetailRow("课程", student.course)
        DetailRow("剩余课时", "${student.hours} 节")
        DetailRow("家长姓名", parent?.name ?: "未登记")
        DetailRow("联系电话", parent?.phone ?: "未登记")

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onSendReminder),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "发送提醒",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String) {
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
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun MessageTemplateView(
    student: Student?,
    parent: StudentParent?,
    onConfirm: () -> Unit
) {
    if (student == null) return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "提醒模板",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = "尊敬的${parent?.name ?: "家长"}，您好！\n\n" +
                        "${student.name}同学的「${student.course}」课程剩余课时仅剩" +
                        "${student.hours}节，建议您及时联系教务老师办理续费，以保" +
                        "证课程学习的连续性。\n\n感谢您的支持与配合！",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onConfirm),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "确认发送",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

@Composable
private fun SuccessView(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "✓",
            style = MaterialTheme.typography.displayLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "提醒已成功发送",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onBack),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "返回列表",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}
