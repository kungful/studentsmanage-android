package com.example.students.ui.screens.manualscheduleadjust

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Schedule
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.viewmodel.AppViewModel

@Composable
fun ManualScheduleAdjustScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var selectedDay by remember { mutableStateOf(0) }
    var selectedRoom by remember { mutableStateOf("") }

    val allSchedules by viewModel.allSchedules.collectAsState(initial = emptyList())
    val days = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val rooms = listOf("琴房302", "多媒体教室A", "VIP琴房1", "会议室1", "理论教室B")
    val timeSlots = listOf("08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00")

    val bookedSlots = timeSlots.mapNotNull { time ->
        val hour = time.split(":")[0].toIntOrNull() ?: return@mapNotNull null
        val hasSchedule = allSchedules.any { schedule ->
            val startH = schedule.startTime.split(":")[0].toIntOrNull() ?: 0
            startH == hour
        }
        if (hasSchedule) time to "booked" else time to "available"
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DetailTopBar(title = "手动调整", onBack = onBack)
            Spacer(Modifier.height(8.dp))
        }

        item {
            Text("选择日期", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                days.forEachIndexed { index, day ->
                    FilterChip(
                        selected = selectedDay == index,
                        onClick = { selectedDay = index },
                        label = { Text(day, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        item {
            Text("选择教室", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                rooms.forEach { room ->
                    FilterChip(
                        selected = selectedRoom == room,
                        onClick = { selectedRoom = room },
                        label = { Text(room, style = MaterialTheme.typography.labelSmall) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    )
                }
            }
        }

        item {
            Text("${days[selectedDay]} · ${selectedRoom.ifEmpty { "请选择教室" }} 时段",
                fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
        }

        items(bookedSlots) { (time, status) ->
            val isBooked = status == "booked"
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isBooked) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f)
                        else MaterialTheme.colorScheme.surface
                ),
                border = if (isBooked) null else androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(time, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                    Text(
                        if (isBooked) "已占用" else "空闲",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium,
                        color = if (isBooked) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        item { Spacer(Modifier.height(16.dp)) }
    }
}
