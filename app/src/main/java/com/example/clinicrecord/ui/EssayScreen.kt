package com.example.clinicrecord.ui

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clinicrecord.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EssayScreen(
    viewModel: ClinicViewModel
) {
    val context = LocalContext.current
    val preferences = remember {
        context.getSharedPreferences("clinic_record_essays", Context.MODE_PRIVATE)
    }
    val essays = remember {
        mutableStateListOf<String>().apply {
            val saved = preferences.getStringSet("items", emptySet()).orEmpty()
            addAll(saved.sortedDescending())
        }
    }
    var draft by remember { mutableStateOf("") }
    var editingRaw by remember { mutableStateOf<String?>(null) }
    var editDraft by remember { mutableStateOf("") }
    var pendingUpdateRaw by remember { mutableStateOf<String?>(null) }
    var pendingDeleteRaw by remember { mutableStateOf<String?>(null) }

    fun saveEssays() {
        preferences.edit()
            .putStringSet("items", essays.toSet())
            .apply()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                title = { Text("随笔") }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                    shape = MaterialTheme.shapes.large
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedTextField(
                            value = draft,
                            onValueChange = { draft = it },
                            label = { Text("随手记录") },
                            minLines = 4,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(
                            onClick = {
                                val text = draft.trim()
                                if (text.isNotBlank()) {
                                    essays.add(0, "${System.currentTimeMillis()}|$text")
                                    preferences.edit()
                                        .putStringSet("items", essays.toSet())
                                        .apply()
                                    draft = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("保存随笔")
                        }
                    }
                }
            }

            if (essays.isEmpty()) {
                item {
                    Text(
                        text = "暂无随笔",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 24.dp)
                    )
                }
            } else {
                items(essays, key = { it }) { raw ->
                    val text = raw.substringAfter('|', raw)
                    EssayCard(
                        text = text,
                        onEdit = {
                            editingRaw = raw
                            editDraft = text
                        },
                        onDelete = { pendingDeleteRaw = raw }
                    )
                }
            }
        }
    }

    val rawBeingEdited = editingRaw
    if (rawBeingEdited != null) {
        AlertDialog(
            onDismissRequest = { editingRaw = null },
            title = { Text("修改随笔") },
            text = {
                OutlinedTextField(
                    value = editDraft,
                    onValueChange = { editDraft = it },
                    minLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (editDraft.trim().isNotBlank()) {
                            pendingUpdateRaw = rawBeingEdited
                        }
                    }
                ) {
                    Text("保存")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingRaw = null }) {
                    Text("取消")
                }
            }
        )
    }

    val rawPendingUpdate = pendingUpdateRaw
    if (rawPendingUpdate != null) {
        AlertDialog(
            onDismissRequest = { pendingUpdateRaw = null },
            title = { Text("确认修改？") },
            text = { Text("保存后会覆盖这条随笔原内容。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val index = essays.indexOf(rawPendingUpdate)
                        if (index != -1) {
                            val timestamp = rawPendingUpdate.substringBefore('|', System.currentTimeMillis().toString())
                            essays[index] = "$timestamp|${editDraft.trim()}"
                            saveEssays()
                        }
                        pendingUpdateRaw = null
                        editingRaw = null
                    }
                ) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingUpdateRaw = null }) {
                    Text("取消")
                }
            }
        )
    }

    val rawPendingDelete = pendingDeleteRaw
    if (rawPendingDelete != null) {
        AlertDialog(
            onDismissRequest = { pendingDeleteRaw = null },
            title = { Text("删除这条随笔？") },
            text = { Text("删除后这条随笔不会再显示。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        val text = rawPendingDelete.substringAfter('|', rawPendingDelete)
                        essays.remove(rawPendingDelete)
                        saveEssays()
                        viewModel.moveEssayToTrash(text)
                        pendingDeleteRaw = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteRaw = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun EssayCard(
    text: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 8,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onEdit) {
                    Text("修改")
                }
                TextButton(onClick = onDelete) {
                    Text("删除")
                }
            }
        }
    }
}
