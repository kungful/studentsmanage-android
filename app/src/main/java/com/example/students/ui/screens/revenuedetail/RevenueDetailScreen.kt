package com.example.students.ui.screens.revenuedetail

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Invoice
import com.example.students.ui.components.DetailTopBar
import com.example.students.ui.components.FilterChipRow
import com.example.students.ui.viewmodel.AppViewModel
import java.text.DecimalFormat

@Composable
fun RevenueDetailScreen(
    viewModel: AppViewModel,
    onBack: () -> Unit
) {
    var tabIndex by remember { mutableIntStateOf(0) }
    val allInvoices by viewModel.allInvoices.collectAsState(initial = emptyList())
    val pendingInvoices by viewModel.getInvoicesByStatus("pending").collectAsState(initial = emptyList())
    val paidInvoices by viewModel.getInvoicesByStatus("paid").collectAsState(initial = emptyList())

    val totalRevenue = allInvoices.sumOf { parseAmount(it.amount) }
    val paidRevenue = paidInvoices.sumOf { parseAmount(it.amount) }
    val pendingRevenue = pendingInvoices.sumOf { parseAmount(it.amount) }

    val df = DecimalFormat("#,##0.00")

    Column(modifier = Modifier.fillMaxSize()) {
        DetailTopBar(title = "收益详情", onBack = onBack)

        FilterChipRow(
            items = listOf("总览", "待收账单", "交易记录"),
            selectedIndex = tabIndex,
            onSelected = { tabIndex = it }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (tabIndex) {
                0 -> {
                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("预计收益总额", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Spacer(Modifier.height(8.dp))
                                Text("¥${df.format(totalRevenue)}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                    RevenueStat("已收", "¥${df.format(paidRevenue)}", MaterialTheme.colorScheme.primary)
                                    RevenueStat("待收", "¥${df.format(pendingRevenue)}", MaterialTheme.colorScheme.error)
                                    RevenueStat("账单", "${allInvoices.size}", MaterialTheme.colorScheme.tertiary)
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
                                Text("收益趋势", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(12.dp))
                                Text("本月预计收入：¥${df.format(totalRevenue)}",
                                    style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("较上月持平",
                                    style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
                1 -> {
                    items(pendingInvoices) { invoice ->
                        InvoiceCard(invoice)
                    }
                    if (pendingInvoices.isEmpty()) {
                        item {
                            Text("暂无待收账单", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(32.dp))
                        }
                    }
                }
                2 -> {
                    items(allInvoices) { invoice ->
                        InvoiceCard(invoice)
                    }
                    if (allInvoices.isEmpty()) {
                        item {
                            Text("暂无交易记录", style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(32.dp))
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun RevenueStat(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
    }
}

@Composable
private fun InvoiceCard(invoice: Invoice) {
    val (statusColor, statusLabel) = when (invoice.status) {
        "paid" -> MaterialTheme.colorScheme.primary to "已支付"
        "pending" -> MaterialTheme.colorScheme.tertiary to "待支付"
        "overdue" -> MaterialTheme.colorScheme.error to "已逾期"
        else -> MaterialTheme.colorScheme.outline to invoice.status
    }

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(invoice.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Text("${invoice.date} · ${invoice.method}", style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.width(8.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(invoice.amount, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.onSurface)
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = statusColor)
            }
        }
    }
}

private fun parseAmount(amount: String): Double {
    return try {
        amount.replace("¥", "").replace(",", "").trim().toDoubleOrNull() ?: 0.0
    } catch (_: Exception) {
        0.0
    }
}
