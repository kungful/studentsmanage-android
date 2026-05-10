package com.example.students.ui.screens.notifications

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import com.example.students.data.model.Notification
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.components.FilterChipRow
import com.example.students.ui.components.StatusBadge
import com.example.students.ui.viewmodel.AppViewModel

private val typeIcons = mapOf(
    "admin" to Icons.Filled.Campaign,
    "academic" to Icons.Filled.School,
    "system" to Icons.Filled.Build,
)

private val typeKeys = listOf("all", "admin", "academic", "system")

@Composable
fun NotificationsScreen(viewModel: AppViewModel, onBack: () -> Unit) {
    var tabIndex by remember { mutableStateOf(0) }
    var selectedNotification by remember { mutableStateOf<Notification?>(null) }
    val scope = rememberCoroutineScope()

    val allNotifications by viewModel.allNotifications.collectAsState(initial = emptyList())
    val filteredNotifications = remember(tabIndex, allNotifications) {
        val key = typeKeys[tabIndex]
        if (key == "all") allNotifications
        else allNotifications.filter { it.type == key }
    }

    val unreadCount = allNotifications.count { !it.isRead }

    Column(modifier = Modifier.fillMaxSize()) {
        DetailTopBar(title = "通知中心", onBack = onBack)

        FilterChipRow(
            items = listOf("全部动态", "行政事务", "教学动态", "系统消息"),
            selectedIndex = tabIndex,
            onSelected = { tabIndex = it }
        )

        if (selectedNotification != null) {
            NotificationDetailView(
                notification = selectedNotification!!,
                onClose = {
                    scope.launch { viewModel.markNotificationRead(selectedNotification!!.id) }
                    selectedNotification = null
                },
                typeIcons = typeIcons
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "共 ${filteredNotifications.size} 条通知",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (unreadCount > 0) {
                            StatusBadge(
                                text = "$unreadCount 条未读",
                                color = MaterialTheme.colorScheme.error,
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        }
                    }
                }

                items(filteredNotifications, key = { it.id }) { notification ->
                    NotificationCard(
                        notification = notification,
                        onClick = {
                            selectedNotification = notification
                            scope.launch { viewModel.markNotificationRead(notification.id) }
                        },
                        typeIcons = typeIcons
                    )
                }

                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun NotificationCard(
    notification: Notification,
    onClick: () -> Unit,
    typeIcons: Map<String, ImageVector>
) {
    val icon = typeIcons[notification.type] ?: Icons.Filled.Info
    val priorityColor = when (notification.priority) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    val typeLabel = when (notification.type) {
        "admin" -> "行政"
        "academic" -> "教学"
        "system" -> "系统"
        else -> notification.type
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (!notification.isRead) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                else MaterialTheme.colorScheme.surface
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(notification.title, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleSmall)
                    if (!notification.isRead) {
                        Spacer(Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.error)
                        )
                    }
                }
                StatusBadge(
                    text = when (notification.priority) {
                        "high" -> "高"
                        "medium" -> "中"
                        else -> "低"
                    },
                    color = priorityColor,
                    containerColor = priorityColor.copy(alpha = 0.15f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(notification.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.height(4.dp))
            Text(notification.time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.outline)
        }
    }
}

@Composable
private fun NotificationDetailView(
    notification: Notification,
    onClose: () -> Unit,
    typeIcons: Map<String, ImageVector>
) {
    val icon = typeIcons[notification.type] ?: Icons.Filled.Info
    val priorityColor = when (notification.priority) {
        "high" -> MaterialTheme.colorScheme.error
        "medium" -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.outline
    }
    val typeLabel = when (notification.type) {
        "admin" -> "行政"
        "academic" -> "教学"
        "system" -> "系统"
        else -> notification.type
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(icon, null, tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(notification.title, fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge)
                    Text(typeLabel,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            IconButton(onClick = onClose) {
                Icon(Icons.Filled.Close, "关闭")
            }
        }
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatusBadge(
                text = when (notification.priority) {
                    "high" -> "高"
                    "medium" -> "中"
                    else -> "低"
                },
                color = priorityColor,
                containerColor = priorityColor.copy(alpha = 0.15f)
            )
            StatusBadge(
                text = notification.time,
                color = MaterialTheme.colorScheme.outline,
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(notification.content + "\n\n" + notification.detail,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface)
    }
}
