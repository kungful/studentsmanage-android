package com.example.students.ui.screens.reports

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Invoice
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.ui.components.FilterChipRow
import com.example.students.ui.components.StatCard
import com.example.students.ui.navigation.Screen
import com.example.students.ui.viewmodel.AppViewModel
import java.text.DecimalFormat

@Composable
fun ReportsScreen(viewModel: AppViewModel, onNavigate: (String) -> Unit) {
    var selectedTimeframe by remember { mutableStateOf(0) }
    val timeframes = listOf("本周", "本月", "上月")

    val completedSchedules by viewModel.getSchedulesByStatus("completed").collectAsState(initial = emptyList())
    val allInvoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val allStudents by viewModel.allStudents.collectAsState(initial = emptyList())
    val presentCount by viewModel.getAttendanceCountByStatus("present").collectAsState(initial = 0)
    val leaveCount by viewModel.getAttendanceCountByStatus("leave").collectAsState(initial = 0)
    val absentCount by viewModel.getAttendanceCountByStatus("absent").collectAsState(initial = 0)

    val consumedHours = completedSchedules.size
    val totalRevenue = allInvoices.sumOf { parseAmount(it.amount) }
    val activeStudents = allStudents.size
    val totalAttendance = presentCount + leaveCount + absentCount
    val attendanceRate = if (totalAttendance > 0) (presentCount * 100 / totalAttendance) else 0

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Spacer(Modifier.height(4.dp))
            FilterChipRow(
                items = timeframes,
                selectedIndex = selectedTimeframe,
                onSelected = { selectedTimeframe = it }
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "总览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            StatCard(
                label = "消耗课时",
                value = consumedHours.toString(),
                sub = "",
                color = MaterialTheme.colorScheme.primary,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigate(Screen.ConsumedHoursDetail.route) }
            )
        }

        item {
            StatCard(
                label = "预计收益总额",
                value = formatRevenue(totalRevenue),
                sub = "",
                color = MaterialTheme.colorScheme.tertiary,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { onNavigate(Screen.RevenueDetail.route) }
            )
        }

        item {
            StatCard(
                label = "服务中活跃学员",
                value = activeStudents.toString(),
                sub = "",
                color = MaterialTheme.colorScheme.secondary,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onNavigate(Screen.ActiveStudentsDetail.route) }
            )
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                "出勤分析",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        item {
            AttendanceCard(
                presentCount = presentCount,
                leaveCount = leaveCount,
                absentCount = absentCount,
                totalSchedules = consumedHours,
                attendanceRate = attendanceRate,
                onClick = { onNavigate(Screen.AttendanceDetail.route) }
            )
        }

        item {
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AttendanceCard(
    presentCount: Int,
    leaveCount: Int,
    absentCount: Int,
    totalSchedules: Int,
    attendanceRate: Int,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "本周出勤率",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                "查看详情",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttendanceStat(label = "出勤", value = presentCount.toString(), color = MaterialTheme.colorScheme.primary)
            AttendanceStat(label = "请假", value = leaveCount.toString(), color = MaterialTheme.colorScheme.tertiary)
            AttendanceStat(label = "缺勤", value = absentCount.toString(), color = MaterialTheme.colorScheme.error)
            AttendanceStat(label = "总课时", value = totalSchedules.toString(), color = MaterialTheme.colorScheme.secondary)
        }

        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(attendanceRate / 100f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(MaterialTheme.colorScheme.primary)
            )
        }

        Spacer(Modifier.height(6.dp))
        Text(
            "出勤率 $attendanceRate%",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AttendanceStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                value,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun parseAmount(amount: String): Double {
    return amount.replace("¥", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
}

private fun formatRevenue(amount: Double): String {
    val formatter = DecimalFormat("#,###")
    return "¥${formatter.format(amount.toLong())}"
}
