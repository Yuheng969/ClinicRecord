package com.example.clinicrecord.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.clinicrecord.data.Patient
import com.example.clinicrecord.viewmodel.ClinicViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientListScreen(
    viewModel: ClinicViewModel,
    onPatientClick: (Long) -> Unit
) {
    val patients by viewModel.patients.collectAsState()
    val allPatients by viewModel.allPatients.collectAsState()
    val folders by viewModel.folders.collectAsState()
    val selectedFolder by viewModel.selectedFolder.collectAsState()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showAddDialog by remember { mutableStateOf(false) }
    var showNewFolderDialog by remember { mutableStateOf(false) }
    var renameFolderName by remember { mutableStateOf<String?>(null) }
    var selectedPatientForActions by remember { mutableStateOf<Patient?>(null) }
    var patientPendingDelete by remember { mutableStateOf<Patient?>(null) }
    var folderPendingDelete by remember { mutableStateOf<String?>(null) }
    var patientSearchQuery by remember { mutableStateOf("") }
    var showPatientSearch by remember { mutableStateOf(false) }

    fun selectFolder(folderName: String) {
        viewModel.selectFolder(folderName)
        scope.launch { drawerState.close() }
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            PatientFolderDrawer(
                allCount = allPatients.size,
                uncategorizedCount = allPatients.count {
                    it.folderName == ClinicViewModel.FOLDER_UNCATEGORIZED || it.folderName.isBlank()
                },
                folders = folders,
                selectedFolder = selectedFolder,
                folderCount = { folderName ->
                    allPatients.count { it.folderName == folderName }
                },
                onSelectFolder = ::selectFolder,
                onNewFolderClick = { showNewFolderDialog = true },
                onRenameFolder = { folderName -> renameFolderName = folderName },
                onDeleteFolder = { folderName -> folderPendingDelete = folderName }
            )
        }
    ) {
        Scaffold(
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        scrolledContainerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.primary
                    ),
                    navigationIcon = {
                        TextButton(onClick = { scope.launch { drawerState.open() } }) {
                            Text(
                                text = "☰",
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    title = {
                        Text(
                            text = selectedFolderTitle(selectedFolder),
                            style = MaterialTheme.typography.titleLarge
                        )
                    },
                    actions = {
                        Box {
                            IconButton(onClick = { showPatientSearch = true }) {
                                SearchIcon()
                            }
                            DropdownMenu(
                                expanded = showPatientSearch,
                                onDismissRequest = { showPatientSearch = false }
                            ) {
                                Box(
                                    modifier = Modifier
                                        .width(280.dp)
                                        .padding(12.dp)
                                ) {
                                    OutlinedTextField(
                                        value = patientSearchQuery,
                                        onValueChange = { patientSearchQuery = it },
                                        placeholder = { Text("按姓名检索患者") },
                                        singleLine = true,
                                        trailingIcon = {
                                            if (patientSearchQuery.isNotBlank()) {
                                                IconButton(onClick = { patientSearchQuery = "" }) {
                                                    Text("×")
                                                }
                                            }
                                        },
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                ) {
                    Text(
                        text = "+",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        ) { innerPadding ->
            PatientListContent(
                patients = patients,
                allPatients = allPatients,
                searchQuery = patientSearchQuery,
                onSearchQueryChange = { patientSearchQuery = it },
                modifier = Modifier
                    .padding(innerPadding)
                    .background(MaterialTheme.colorScheme.background),
                onPatientClick = onPatientClick,
                onPatientLongClick = { patient -> selectedPatientForActions = patient }
            )
        }
    }

    if (showAddDialog) {
        AddPatientDialog(
            viewModel = viewModel,
            initialFolderName = defaultAddFolder(selectedFolder),
            folders = folders,
            onDismiss = { showAddDialog = false }
        )
    }

    if (showNewFolderDialog) {
        NewFolderDialog(
            onDismiss = { showNewFolderDialog = false },
            onConfirm = { folderName ->
                viewModel.createFolder(folderName)
                showNewFolderDialog = false
            }
        )
    }

    val folderBeingRenamed = renameFolderName
    if (folderBeingRenamed != null) {
        RenameFolderDialog(
            oldName = folderBeingRenamed,
            onDismiss = { renameFolderName = null },
            onConfirm = { newName ->
                viewModel.renameFolder(folderBeingRenamed, newName)
                renameFolderName = null
            }
        )
    }

    val patientForActions = selectedPatientForActions
    if (patientForActions != null) {
        PatientActionSheet(
            patient = patientForActions,
            folders = folders,
            onDismiss = { selectedPatientForActions = null },
            onDelete = {
                selectedPatientForActions = null
                patientPendingDelete = patientForActions
            },
            onMoveTo = { folderName ->
                viewModel.updatePatientProfile(
                    patient = patientForActions,
                    name = patientForActions.name,
                    gender = patientForActions.gender,
                    age = patientForActions.age,
                    contact = patientForActions.contact,
                    nativePlace = patientForActions.nativePlace,
                    folderName = folderName,
                    pastHistory = patientForActions.pastHistory
                )
                selectedPatientForActions = null
            }
        )
    }

    val folderForDelete = folderPendingDelete
    if (folderForDelete != null) {
        DeleteFolderDialog(
            folderName = folderForDelete,
            onDismiss = { folderPendingDelete = null },
            onDeleteFolderOnly = {
                viewModel.deleteFolder(folderForDelete, deletePatients = false)
                folderPendingDelete = null
            },
            onDeleteFolderAndPatients = {
                viewModel.deleteFolder(folderForDelete, deletePatients = true)
                folderPendingDelete = null
            }
        )
    }

    val patientForDelete = patientPendingDelete
    if (patientForDelete != null) {
        AlertDialog(
            onDismissRequest = { patientPendingDelete = null },
            title = { Text("删除此患者？") },
            text = { Text("患者资料和就诊记录会移至最近删除，并保留 7 日。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deletePatient(patientForDelete)
                        patientPendingDelete = null
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { patientPendingDelete = null }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun PatientFolderDrawer(
    allCount: Int,
    uncategorizedCount: Int,
    folders: List<String>,
    selectedFolder: String,
    folderCount: (String) -> Int,
    onSelectFolder: (String) -> Unit,
    onNewFolderClick: () -> Unit,
    onRenameFolder: (String) -> Unit,
    onDeleteFolder: (String) -> Unit
) {
    ModalDrawerSheet(
        modifier = Modifier
            .fillMaxWidth(0.72f)
            .widthIn(max = 320.dp)
            .fillMaxHeight(),
        drawerContainerColor = MaterialTheme.colorScheme.background,
        drawerContentColor = MaterialTheme.colorScheme.onBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(18.dp)
        ) {
            Text(
                text = "我的病案夹",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(vertical = 18.dp)
            )
            DrawerFolderItem(
                title = "全部患者",
                countText = allCount.toString(),
                selected = selectedFolder == ClinicViewModel.FOLDER_ALL,
                leading = { DrawerCategoryIcon(DrawerIconKind.All) },
                onClick = { onSelectFolder(ClinicViewModel.FOLDER_ALL) }
            )
            DrawerFolderItem(
                title = ClinicViewModel.FOLDER_UNCATEGORIZED,
                countText = uncategorizedCount.toString(),
                selected = selectedFolder == ClinicViewModel.FOLDER_UNCATEGORIZED,
                leading = { DrawerCategoryIcon(DrawerIconKind.Uncategorized) },
                onClick = { onSelectFolder(ClinicViewModel.FOLDER_UNCATEGORIZED) }
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 14.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "文件",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(onClick = onNewFolderClick) {
                    Text("+ 新建")
                }
            }
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(folders, key = { it }) { folderName ->
                    DrawerFolderItem(
                        title = folderName,
                        countText = folderCount(folderName).toString(),
                        selected = selectedFolder == folderName,
                        leading = { DrawerCategoryIcon(DrawerIconKind.Folder) },
                        onClick = { onSelectFolder(folderName) },
                        onRename = { onRenameFolder(folderName) },
                        onDelete = { onDeleteFolder(folderName) }
                    )
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun DrawerFolderItem(
    title: String,
    countText: String,
    selected: Boolean,
    leading: @Composable () -> Unit,
    onClick: () -> Unit,
    onRename: (() -> Unit)? = null,
    onDelete: (() -> Unit)? = null
) {
    var showMenu by remember { mutableStateOf(false) }
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp)
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = {
                        if (onRename != null || onDelete != null) {
                            showMenu = true
                        }
                    }
                ),
            color = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            },
            shape = MaterialTheme.shapes.medium,
            tonalElevation = if (selected) 4.dp else 1.dp,
            shadowElevation = if (selected) 3.dp else 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading()
                Text(
                    text = title,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = countText,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        DropdownMenu(
            expanded = showMenu,
            onDismissRequest = { showMenu = false }
        ) {
            if (onRename != null) {
                DropdownMenuItem(
                    text = { Text("重命名") },
                    onClick = {
                        showMenu = false
                        onRename()
                    }
                )
            }
            if (onDelete != null) {
                DropdownMenuItem(
                    text = { Text("删除") },
                    onClick = {
                        showMenu = false
                        onDelete()
                    }
                )
            }
        }
    }
}

private enum class DrawerIconKind {
    All,
    Uncategorized,
    Folder
}

@Composable
private fun DrawerCategoryIcon(kind: DrawerIconKind) {
    val backgroundColor = when (kind) {
        DrawerIconKind.All -> Color(0xFFF2E681)
        DrawerIconKind.Uncategorized -> Color(0xFFDCEFFD)
        DrawerIconKind.Folder -> Color(0xFFFFD875)
    }
    val contentColor = when (kind) {
        DrawerIconKind.All -> Color(0xFFE0AF08)
        DrawerIconKind.Uncategorized -> Color(0xFF1787C8)
        DrawerIconKind.Folder -> Color(0xFFE4A20A)
    }
    Canvas(modifier = Modifier.size(24.dp)) {
        drawRoundRect(
            color = backgroundColor,
            size = Size(size.width, size.height),
            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
        )
        when (kind) {
            DrawerIconKind.All -> {
                drawRect(
                    color = contentColor,
                    topLeft = Offset(6.dp.toPx(), 5.dp.toPx()),
                    size = Size(5.dp.toPx(), 5.dp.toPx())
                )
                drawLine(
                    color = contentColor,
                    start = Offset(6.dp.toPx(), 14.dp.toPx()),
                    end = Offset(17.dp.toPx(), 14.dp.toPx()),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = contentColor,
                    start = Offset(6.dp.toPx(), 18.dp.toPx()),
                    end = Offset(15.dp.toPx(), 18.dp.toPx()),
                    strokeWidth = 2.4.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            DrawerIconKind.Uncategorized -> {
                drawCircle(
                    color = contentColor,
                    radius = 1.3.dp.toPx(),
                    center = Offset(12.dp.toPx(), 18.dp.toPx())
                )
                drawLine(
                    color = contentColor,
                    start = Offset(12.dp.toPx(), 14.dp.toPx()),
                    end = Offset(12.dp.toPx(), 13.dp.toPx()),
                    strokeWidth = 2.6.dp.toPx(),
                    cap = StrokeCap.Round
                )
                drawArc(
                    color = contentColor,
                    startAngle = 210f,
                    sweepAngle = 230f,
                    useCenter = false,
                    topLeft = Offset(7.dp.toPx(), 5.dp.toPx()),
                    size = Size(10.dp.toPx(), 10.dp.toPx()),
                    style = androidx.compose.ui.graphics.drawscope.Stroke(
                        width = 2.4.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                )
            }
            DrawerIconKind.Folder -> {
                drawRoundRect(
                    color = contentColor,
                    topLeft = Offset(4.dp.toPx(), 8.dp.toPx()),
                    size = Size(16.dp.toPx(), 10.dp.toPx()),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
                drawRoundRect(
                    color = contentColor,
                    topLeft = Offset(5.dp.toPx(), 6.dp.toPx()),
                    size = Size(7.dp.toPx(), 5.dp.toPx()),
                    cornerRadius = CornerRadius(1.5.dp.toPx(), 1.5.dp.toPx())
                )
                drawRect(
                    color = backgroundColor.copy(alpha = 0.72f),
                    topLeft = Offset(4.dp.toPx(), 10.dp.toPx()),
                    size = Size(16.dp.toPx(), 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun PatientListContent(
    patients: List<Patient>,
    allPatients: List<Patient>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    onPatientClick: (Long) -> Unit,
    onPatientLongClick: (Patient) -> Unit
) {
    val normalizedQuery = searchQuery.trim()
    val visiblePatients = remember(patients, allPatients, normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            patients
        } else {
            allPatients.filter { patient ->
                patient.name.contains(normalizedQuery, ignoreCase = true)
            }
        }
    }
    val listState = rememberLazyListState()

    LaunchedEffect(normalizedQuery, visiblePatients.firstOrNull()?.patientId) {
        if (normalizedQuery.isNotBlank() && visiblePatients.isNotEmpty()) {
            listState.animateScrollToItem(0)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (visiblePatients.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (allPatients.isEmpty()) "暂无患者记录" else "未找到匹配患者",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(
                items = visiblePatients,
                key = { it.patientId }
            ) { patient ->
                PatientCard(
                    patient = patient,
                    onClick = { onPatientClick(patient.patientId) },
                    onLongClick = { onPatientLongClick(patient) }
                )
            }
        }
    }
}

@Composable
private fun SearchIcon() {
    val color = MaterialTheme.colorScheme.primary
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(
            color = color,
            radius = 6.5.dp.toPx(),
            center = Offset(10.dp.toPx(), 10.dp.toPx()),
            style = androidx.compose.ui.graphics.drawscope.Stroke(
                width = 2.2.dp.toPx(),
                cap = StrokeCap.Round
            )
        )
        drawLine(
            color = color,
            start = Offset(15.dp.toPx(), 15.dp.toPx()),
            end = Offset(20.dp.toPx(), 20.dp.toPx()),
            strokeWidth = 2.2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun PatientCard(
    patient: Patient,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = patient.folderName.ifBlank { ClinicViewModel.FOLDER_UNCATEGORIZED },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${patient.gender} · ${patient.age}岁",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "既往史：${patient.pastHistory.ifBlank { "未记录" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.outline,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientActionSheet(
    patient: Patient,
    folders: List<String>,
    onDismiss: () -> Unit,
    onDelete: () -> Unit,
    onMoveTo: (String) -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(
                text = "已选择：${patient.name}",
                style = MaterialTheme.typography.titleLarge
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TextButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("删除")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("取消")
                }
            }
            Text(
                text = "移动到",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
            listOf(ClinicViewModel.FOLDER_UNCATEGORIZED).plus(folders).forEach { folderName ->
                TextButton(
                    onClick = { onMoveTo(folderName) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(folderName)
                }
            }
        }
    }
}

@Composable
private fun NewFolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "新建分类",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("文件夹名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                onConfirm(folderName.trim())
                            }
                        }
                    ) {
                        Text("确定")
                    }
                }
            }
        }
    }
}

@Composable
private fun RenameFolderDialog(
    oldName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var folderName by remember(oldName) { mutableStateOf(oldName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "重命名分类",
                    style = MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("新的分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            if (folderName.isNotBlank()) {
                                onConfirm(folderName.trim())
                            }
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

@Composable
private fun DeleteFolderDialog(
    folderName: String,
    onDismiss: () -> Unit,
    onDeleteFolderOnly: () -> Unit,
    onDeleteFolderAndPatients: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "删除此文件夹？",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "“$folderName” 删除后会进入最近删除并保留 7 日。你可以只删除文件夹，也可以同时删除其中患者。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                TextButton(
                    onClick = onDeleteFolderAndPatients,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "删除文件夹和患者",
                        color = MaterialTheme.colorScheme.error
                    )
                }
                TextButton(
                    onClick = onDeleteFolderOnly,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("仅删除文件夹")
                }
                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("取消")
                }
            }
        }
    }
}

private fun selectedFolderTitle(selectedFolder: String): String {
    return when (selectedFolder) {
        ClinicViewModel.FOLDER_ALL -> "全部患者"
        else -> selectedFolder
    }
}

private fun defaultAddFolder(selectedFolder: String): String {
    return when (selectedFolder) {
        ClinicViewModel.FOLDER_ALL -> ClinicViewModel.FOLDER_UNCATEGORIZED
        else -> selectedFolder
    }
}
