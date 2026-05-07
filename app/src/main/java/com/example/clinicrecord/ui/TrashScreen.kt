package com.example.clinicrecord.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clinicrecord.data.DeletedRecord
import com.example.clinicrecord.viewmodel.ClinicViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrashScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val records by viewModel.recentDeletedRecords.collectAsState()
    var recordPendingRestore by remember { mutableStateOf<DeletedRecord?>(null) }
    var recordPendingPermanentDelete by remember { mutableStateOf<DeletedRecord?>(null) }

    LaunchedEffect(Unit) {
        viewModel.purgeExpiredDeletedRecords()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("最近删除") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        if (records.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            ) {
                Text(
                    text = "暂无最近删除内容",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "仅保留近 7 日删除的文件或记录",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                items(records, key = { it.deletedRecordId }) { record ->
                    DeletedRecordCard(
                        record = record,
                        onRestoreClick = { recordPendingRestore = record },
                        onPermanentDeleteClick = { recordPendingPermanentDelete = record }
                    )
                }
            }
        }
    }

    val restoreRecord = recordPendingRestore
    if (restoreRecord != null) {
        AlertDialog(
            onDismissRequest = { recordPendingRestore = null },
            title = { Text("恢复此记录？") },
            text = { Text("将“${restoreRecord.title}”从最近删除恢复到诊籍中。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.restoreDeletedRecord(restoreRecord)
                        recordPendingRestore = null
                    }
                ) {
                    Text("恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordPendingRestore = null }) {
                    Text("取消")
                }
            }
        )
    }

    val permanentDeleteRecord = recordPendingPermanentDelete
    if (permanentDeleteRecord != null) {
        AlertDialog(
            onDismissRequest = { recordPendingPermanentDelete = null },
            title = { Text("永久删除此记录？") },
            text = { Text("“${permanentDeleteRecord.title}”将从最近删除中移除，之后无法恢复。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.permanentlyDeleteRecord(permanentDeleteRecord)
                        recordPendingPermanentDelete = null
                    }
                ) {
                    Text("永久删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { recordPendingPermanentDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun DeletedRecordCard(
    record: DeletedRecord,
    onRestoreClick: () -> Unit,
    onPermanentDeleteClick: () -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = {},
                    onLongClick = { showActionMenu = true }
                ),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = "${record.recordType} · ${record.title}",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = record.summary.ifBlank { "无摘要" },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "删除时间：${formatDeletedTime(record.deletedAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
        DropdownMenu(
            expanded = showActionMenu,
            onDismissRequest = { showActionMenu = false }
        ) {
            DropdownMenuItem(
                text = { Text("恢复") },
                onClick = {
                    showActionMenu = false
                    onRestoreClick()
                }
            )
            DropdownMenuItem(
                text = { Text("永久删除") },
                onClick = {
                    showActionMenu = false
                    onPermanentDeleteClick()
                }
            )
        }
    }
}

private fun formatDeletedTime(timestamp: Long): String {
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
}
