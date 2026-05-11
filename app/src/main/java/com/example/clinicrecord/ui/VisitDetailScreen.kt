package com.example.clinicrecord.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.BaselineShift
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.clinicrecord.data.MedicationItem
import com.example.clinicrecord.data.VisitWithPrescriptionAndItems
import com.example.clinicrecord.viewmodel.ClinicViewModel
import com.example.clinicrecord.viewmodel.ConsultationMedicationInput
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VisitDetailScreen(
    visitId: Long,
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val detail by viewModel
        .getVisitWithPrescriptionAndItems(visitId)
        .collectAsState(initial = null)
    var isEditing by remember { mutableStateOf(false) }
    var showExitEditDialog by remember { mutableStateOf(false) }
    var showMoreMenu by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text(if (isEditing) "编辑就诊" else "就诊详情") },
                navigationIcon = {
                    TextButton(onClick = { if (isEditing) showExitEditDialog = true else onBack() }) {
                        Text(if (isEditing) "取消" else "返回")
                    }
                },
                actions = {
                    val currentDetail = detail
                    if (!isEditing && currentDetail != null) {
                        Box {
                            IconButton(onClick = { showMoreMenu = true }) {
                                Text("⋮", style = MaterialTheme.typography.titleLarge)
                            }
                            DropdownMenu(
                                expanded = showMoreMenu,
                                onDismissRequest = { showMoreMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("编辑") },
                                    onClick = {
                                        showMoreMenu = false
                                        isEditing = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("删除") },
                                    onClick = {
                                        showMoreMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        val visitDetail = detail
        if (visitDetail == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            ) {
                Text(
                    text = "未找到就诊记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            if (isEditing) {
                VisitEditContent(
                    detail = visitDetail,
                    viewModel = viewModel,
                    modifier = Modifier
                        .padding(innerPadding),
                    onSaved = { isEditing = false }
                )
            } else {
                VisitDetailContent(
                    detail = visitDetail,
                    modifier = Modifier
                        .padding(innerPadding)
                )
            }
        }
    }

    if (showExitEditDialog) {
        AlertDialog(
            onDismissRequest = { showExitEditDialog = false },
            title = { Text("保留当前修改？") },
            text = { Text("你正在编辑就诊内容，返回后未保存的修改不会写入病案。") },
            confirmButton = {
                TextButton(onClick = { showExitEditDialog = false }) {
                    Text("继续编辑")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showExitEditDialog = false
                        isEditing = false
                    }
                ) {
                    Text("放弃修改")
                }
            }
        )
    }

    val currentDetail = detail
    if (showDeleteDialog && currentDetail != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("删除此就诊记录？") },
            text = { Text("本次就诊和处方会移至最近删除，并保留 7 日。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteVisit(currentDetail)
                        showDeleteDialog = false
                        onBack()
                    }
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun VisitDetailContent(
    detail: VisitWithPrescriptionAndItems,
    modifier: Modifier = Modifier
) {
    val focusManager = LocalFocusManager.current
    val nextActions = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) }
    )
    val nextOptions = KeyboardOptions(imeAction = ImeAction.Next)
    val solarTermRequester = remember { FocusRequester() }
    val weatherRequester = remember { FocusRequester() }
    val chiefComplaintRequester = remember { FocusRequester() }
    val formulaNameRequester = remember { FocusRequester() }
    val doseCountRequester = remember { FocusRequester() }
    val usageRequester = remember { FocusRequester() }
    val medicationAdjustmentsRequester = remember { FocusRequester() }
    val visit = detail.visit
    val prescriptionWithItems = detail.prescriptionWithItems
    val noteParts = splitClinicalNote(visit.clinicalNote)

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DetailCard(title = "节气与天气") {
                Text(
                    text = formatVisitDate(visit.visitDate),
                    style = MaterialTheme.typography.titleSmall
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    DetailText(
                        label = "节气",
                        value = visit.solarTerm,
                        modifier = Modifier.weight(1f)
                    )
                    DetailText(
                        label = "天气",
                        value = visit.weather,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            DetailCard(title = "四诊") {
                DetailText("主诉", visit.chiefComplaint)
                DetailText("现病史", visit.presentIllness)
                DetailText("舌脉", visit.pulseDescription)
                if (visit.tongueImagePath.isNotBlank()) {
                    DetailText("舌象图片", visit.tongueImagePath)
                }
            }
        }

        item {
            DetailCard(title = "辨证") {
                DetailText("核心病机", visit.corePathogenesis)
                DetailText("治法", visit.treatmentMethod)
            }
        }

        item {
            DetailCard(title = "处方") {
                val prescription = prescriptionWithItems?.prescription
                if (prescription == null) {
                    Text(
                        text = "未记录处方",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    DetailText("主方名称", prescription.formulaName)
                }
                Text(
                    text = "药材明细",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                val items = prescriptionWithItems?.items.orEmpty()
                if (items.isEmpty()) {
                    Text(
                        text = "未记录药材",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    val rows = items.chunked(4)
                    rows.forEachIndexed { index, rowItems ->
                        MedicationGridRow(items = rowItems)
                        if (index != rows.lastIndex) {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        }
                    }
                }
                if (prescription != null) {
                    DetailText("剂数", "${prescription.doseCount}剂")
                    DetailText("煎服方法", prescription.usage)
                }
            }
        }

        item {
            DetailCard(title = "配合使用成药/针灸") {
                Text(
                    text = visit.patentMedicineAcupuncture.ifBlank { "未记录" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            DetailCard(title = "加减用药") {
                Text(
                    text = noteParts.medicationAdjustments.ifBlank { "未记录" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        item {
            DetailCard(title = "感悟") {
                Text(
                    text = noteParts.clinicalNote.ifBlank { "未记录" },
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

@Composable
private fun DetailText(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = value.ifBlank { "未记录" },
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Composable
private fun MedicationGridRow(
    items: List<MedicationItem>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        repeat(4) { index ->
            val item = items.getOrNull(index)
            MedicationGridCell(
                item = item,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MedicationGridCell(
    item: MedicationItem?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = item?.medicationName.orEmpty(),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = doseWithMethodTag(item),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Clip
        )
    }
}

private fun doseWithMethodTag(item: MedicationItem?): AnnotatedString {
    if (item == null) return AnnotatedString("")
    val tag = decoctionMethodShortTag(item.decoctionMethod)
    return AnnotatedString.Builder()
        .apply {
            append("${item.dosage}${item.unit}")
            if (tag.isNotBlank()) {
                append(" ")
                pushStyle(
                    SpanStyle(
                        baselineShift = BaselineShift.Superscript,
                        fontSize = 11.sp
                    )
                )
                append("($tag)")
                pop()
            }
        }
        .toAnnotatedString()
}

private fun decoctionMethodShortTag(method: String): String {
    return when (method.trim()) {
        "先煎" -> "先"
        "后下" -> "后"
        "包煎" -> "包"
        "打碎" -> "碎"
        "冲服" -> "冲"
        "烊化" -> "烊"
        "另煎" -> "另"
        else -> ""
    }
}

@Composable
private fun VisitEditContent(
    detail: VisitWithPrescriptionAndItems,
    viewModel: ClinicViewModel,
    modifier: Modifier = Modifier,
    onSaved: () -> Unit
) {
    val focusManager = LocalFocusManager.current
    val nextActions = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) }
    )
    val nextOptions = KeyboardOptions(imeAction = ImeAction.Next)
    val solarTermRequester = remember { FocusRequester() }
    val weatherRequester = remember { FocusRequester() }
    val chiefComplaintRequester = remember { FocusRequester() }
    val formulaNameRequester = remember { FocusRequester() }
    val doseCountRequester = remember { FocusRequester() }
    val usageRequester = remember { FocusRequester() }
    val patentMedicineAcupunctureRequester = remember { FocusRequester() }
    val medicationAdjustmentsRequester = remember { FocusRequester() }
    val visit = detail.visit
    val prescription = detail.prescriptionWithItems?.prescription
    val originalItems = detail.prescriptionWithItems?.items.orEmpty()
    val initialNoteParts = remember(visit.visitId) { splitClinicalNote(visit.clinicalNote) }

    var visitDateText by remember(visit.visitId) { mutableStateOf(formatEditVisitDate(visit.visitDate)) }
    var solarTerm by remember(visit.visitId) {
        mutableStateOf(
            parseEditVisitDateTimeOrNull(formatEditVisitDate(visit.visitDate))
                ?.let(::solarTermForVisitDate)
                ?: visit.solarTerm
        )
    }
    var weather by remember(visit.visitId) { mutableStateOf(visit.weather) }
    var chiefComplaint by remember(visit.visitId) { mutableStateOf(visit.chiefComplaint) }
    var presentIllness by remember(visit.visitId) { mutableStateOf(visit.presentIllness) }
    var pulseDescription by remember(visit.visitId) { mutableStateOf(visit.pulseDescription) }
    var corePathogenesis by remember(visit.visitId) { mutableStateOf(visit.corePathogenesis) }
    var treatmentMethod by remember(visit.visitId) { mutableStateOf(visit.treatmentMethod) }
    var medicationAdjustments by remember(visit.visitId) {
        mutableStateOf(initialNoteParts.medicationAdjustments)
    }
    var clinicalNote by remember(visit.visitId) {
        mutableStateOf(initialNoteParts.clinicalNote)
    }
    var formulaName by remember(visit.visitId) { mutableStateOf(prescription?.formulaName ?: "自拟方") }
    var doseCountText by remember(visit.visitId) { mutableStateOf((prescription?.doseCount ?: 7).toString()) }
    var usage by remember(visit.visitId) { mutableStateOf(prescription?.usage ?: "水煎服") }
    var patentMedicineAcupuncture by remember(visit.visitId) { mutableStateOf(visit.patentMedicineAcupuncture) }
    var pendingMedicationNameFocusId by remember(visit.visitId) { mutableStateOf<Long?>(null) }
    val medicationRows = remember(visit.visitId) {
        mutableStateListOf<VisitMedicationEditRow>().apply {
            if (originalItems.isEmpty()) {
                add(VisitMedicationEditRow())
            } else {
                addAll(
                    originalItems.map {
                        VisitMedicationEditRow(
                            name = it.medicationName,
                            dosage = it.dosage.toString(),
                            decoctionMethod = it.decoctionMethod
                        )
                    }
                )
            }
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            DetailCard(title = "节气与天气") {
                Text(
                    text = "可按实际就诊时间填写",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedTextField(
                    value = visitDateText,
                    onValueChange = { value ->
                        visitDateText = value
                        parseEditVisitDateTimeOrNull(value)?.let { dateTime ->
                            solarTerm = solarTermForVisitDate(dateTime)
                        }
                    },
                    label = { Text("就诊时间") },
                    placeholder = { Text("2026-05-06 14:30") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { solarTermRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .moveFocusOnEnter(solarTermRequester)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = solarTerm,
                        onValueChange = { solarTerm = it },
                        label = { Text("节气") },
                        singleLine = true,
                        keyboardOptions = nextOptions,
                        keyboardActions = KeyboardActions(onNext = { weatherRequester.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(solarTermRequester)
                            .moveFocusOnEnter(weatherRequester)
                    )
                    OutlinedTextField(
                        value = weather,
                        onValueChange = { weather = it },
                        label = { Text("天气") },
                        singleLine = true,
                        keyboardOptions = nextOptions,
                        keyboardActions = KeyboardActions(onNext = { chiefComplaintRequester.requestFocus() }),
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(weatherRequester)
                            .moveFocusOnEnter(chiefComplaintRequester)
                    )
                }
            }
        }

        item {
            DetailCard(title = "四诊") {
                EditTextField(
                    label = "主诉",
                    value = chiefComplaint,
                    onValueChange = { chiefComplaint = it },
                    minLines = 2,
                    requester = chiefComplaintRequester
                )
                EditTextField("现病史", presentIllness, { presentIllness = it }, 3)
                EditTextField("舌脉", pulseDescription, { pulseDescription = it }, 2)
            }
        }

        item {
            DetailCard(title = "辨证") {
                EditTextField("核心病机", corePathogenesis, { corePathogenesis = it }, 2)
                EditTextField(
                    label = "治法",
                    value = treatmentMethod,
                    onValueChange = { treatmentMethod = it },
                    minLines = 2,
                    nextRequester = formulaNameRequester
                )
            }
        }

        item {
            DetailCard(title = "处方") {
                OutlinedTextField(
                    value = formulaName,
                    onValueChange = { formulaName = it },
                    label = { Text("主方名称") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(
                        onNext = {
                            medicationRows.firstOrNull()?.nameRequester?.requestFocus()
                                ?: doseCountRequester.requestFocus()
                        }
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(formulaNameRequester)
                        .moveFocusOnEnter(
                            medicationRows.firstOrNull()?.nameRequester ?: doseCountRequester
                        )
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "药材明细",
                        style = MaterialTheme.typography.titleSmall
                    )
                    OutlinedButton(onClick = { medicationRows.add(VisitMedicationEditRow()) }) {
                        Text("+ 添加药材")
                    }
                }
                medicationRows.forEachIndexed { index, row ->
                    val nextRow = medicationRows.getOrNull(index + 1)
                    MedicationEditRow(
                        row = row,
                        nextRequester = nextRow?.nameRequester ?: doseCountRequester,
                        onNameChange = { value ->
                            medicationRows.update(row.id) { it.copy(name = value) }
                        },
                        onDosageChange = { value ->
                            medicationRows.update(row.id) { it.copy(dosage = value) }
                        },
                        onDecoctionMethodChange = { value ->
                            medicationRows.update(row.id) { it.copy(decoctionMethod = value) }
                        },
                        onRemove = {
                            if (medicationRows.size > 1) {
                                medicationRows.removeAll { it.id == row.id }
                            } else {
                                medicationRows[0] = VisitMedicationEditRow(id = row.id)
                            }
                        },
                        onAddNextRow = if (nextRow == null) {
                            {
                                val newRow = VisitMedicationEditRow()
                                medicationRows.add(newRow)
                                pendingMedicationNameFocusId = newRow.id
                            }
                        } else {
                            null
                        },
                        requestNameFocus = pendingMedicationNameFocusId == row.id,
                        onNameFocusHandled = { pendingMedicationNameFocusId = null }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = doseCountText,
                        onValueChange = { doseCountText = it.filter(Char::isDigit) },
                        label = { Text("剂数") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = nextActions,
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(doseCountRequester)
                            .moveFocusOnEnter(usageRequester)
                    )
                    OutlinedTextField(
                        value = usage,
                        onValueChange = { usage = it },
                        label = { Text("煎服方法") },
                        singleLine = true,
                        keyboardOptions = nextOptions,
                        keyboardActions = KeyboardActions(onNext = { patentMedicineAcupunctureRequester.requestFocus() }),
                        modifier = Modifier
                            .weight(2f)
                            .focusRequester(usageRequester)
                            .moveFocusOnEnter(patentMedicineAcupunctureRequester)
                    )
                }
            }
        }

        item {
            DetailCard(title = "配合使用成药/针灸") {
                OutlinedTextField(
                    value = patentMedicineAcupuncture,
                    onValueChange = { patentMedicineAcupuncture = it },
                    placeholder = { Text("记录中成药、针灸、外治或其他配合方案") },
                    minLines = 3,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { medicationAdjustmentsRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(patentMedicineAcupunctureRequester)
                        .moveFocusOnEnter(medicationAdjustmentsRequester)
                )
            }
        }

        item {
            DetailCard(title = "加减用药") {
                OutlinedTextField(
                    value = medicationAdjustments,
                    onValueChange = { medicationAdjustments = it },
                    placeholder = { Text("记录本次处方加减、替换或临证用药变化") },
                    minLines = 3,
                    keyboardOptions = nextOptions,
                    keyboardActions = nextActions,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(medicationAdjustmentsRequester)
                        .moveFocusOnEnter(focusManager)
                )
            }
        }

        item {
            DetailCard(title = "感悟") {
                OutlinedTextField(
                    value = clinicalNote,
                    onValueChange = { clinicalNote = it },
                    placeholder = { Text("记录本次思路、预后判断或特殊观察点") },
                    minLines = 4,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            Button(
                onClick = {
                    viewModel.updateFullConsultation(
                        current = detail,
                        visitDate = parseVisitDateText(visitDateText),
                        solarTerm = solarTerm,
                        weather = weather,
                        chiefComplaint = chiefComplaint,
                        presentIllness = presentIllness,
                        pulseDescription = pulseDescription,
                        corePathogenesis = corePathogenesis,
                        treatmentMethod = treatmentMethod,
                        patentMedicineAcupuncture = patentMedicineAcupuncture,
                        clinicalNote = buildClinicalNote(
                            medicationAdjustments = medicationAdjustments,
                            clinicalNote = clinicalNote
                        ),
                        formulaName = formulaName,
                        doseCount = doseCountText.toIntOrNull() ?: 7,
                        usage = usage,
                        medicationInputs = medicationRows.mapNotNull { row ->
                            if (row.name.isBlank()) {
                                null
                            } else {
                                ConsultationMedicationInput(
                                    name = row.name,
                                    dosage = row.dosage.toDoubleOrNull() ?: 0.0,
                                    decoctionMethod = row.decoctionMethod
                                )
                            }
                        },
                        onSaved = onSaved
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("保存修改")
            }
        }
    }
}

@Composable
private fun EditTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    minLines: Int,
    requester: FocusRequester? = null,
    nextRequester: FocusRequester? = null
) {
    val focusManager = LocalFocusManager.current
    val nextActions = KeyboardActions(
        onNext = {
            if (nextRequester != null) {
                nextRequester.requestFocus()
            } else {
                focusManager.moveFocus(FocusDirection.Down)
            }
        }
    )
    val fieldModifier = if (requester != null) {
        Modifier.fillMaxWidth().focusRequester(requester)
    } else {
        Modifier.fillMaxWidth()
    }
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        minLines = minLines,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = nextActions,
        modifier = if (nextRequester != null) {
            fieldModifier.moveFocusOnEnter(nextRequester)
        } else {
            fieldModifier.moveFocusOnEnter(focusManager)
        }
    )
}

@Composable
private fun MedicationEditRow(
    row: VisitMedicationEditRow,
    nextRequester: FocusRequester,
    onNameChange: (String) -> Unit,
    onDosageChange: (String) -> Unit,
    onDecoctionMethodChange: (String) -> Unit,
    onRemove: () -> Unit,
    onAddNextRow: (() -> Unit)?,
    requestNameFocus: Boolean,
    onNameFocusHandled: () -> Unit
) {
    val nextOptions = KeyboardOptions(imeAction = ImeAction.Next)
    LaunchedEffect(requestNameFocus) {
        if (requestNameFocus) {
            row.nameRequester.requestFocus()
            onNameFocusHandled()
        }
    }
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        OutlinedTextField(
            value = row.name,
            onValueChange = onNameChange,
            label = { Text("药材名") },
            singleLine = true,
            keyboardOptions = nextOptions,
            keyboardActions = KeyboardActions(onNext = { row.dosageRequester.requestFocus() }),
            modifier = Modifier
                .weight(1.45f)
                .height(64.dp)
                .focusRequester(row.nameRequester)
                .moveFocusOnEnter(row.dosageRequester)
        )
        Row(
            modifier = Modifier.weight(1.15f),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            OutlinedTextField(
                value = row.dosage,
                onValueChange = { value ->
                    onDosageChange(value.filter { it.isDigit() || it == '.' })
                },
                label = { Text("剂量", maxLines = 1) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(onNext = { onAddNextRow?.invoke() ?: nextRequester.requestFocus() }),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .focusRequester(row.dosageRequester)
                    .moveFocusOnEnter(onAddNextRow, nextRequester)
            )
            Text(
                text = "g",
                modifier = Modifier.padding(top = 20.dp),
                fontSize = 16.sp
            )
        }
        DecoctionMethodDropdown(
            value = row.decoctionMethod,
            onValueChange = onDecoctionMethodChange,
            modifier = Modifier
                .weight(0.95f)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier.size(34.dp)
        ) {
            Text(
                text = "×",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

private data class VisitMedicationEditRow(
    val id: Long = nextVisitMedicationRowId(),
    val name: String = "",
    val dosage: String = "",
    val decoctionMethod: String = "",
    val nameRequester: FocusRequester = FocusRequester(),
    val dosageRequester: FocusRequester = FocusRequester()
)

private fun MutableList<VisitMedicationEditRow>.update(
    id: Long,
    transform: (VisitMedicationEditRow) -> VisitMedicationEditRow
) {
    val index = indexOfFirst { it.id == id }
    if (index != -1) {
        this[index] = transform(this[index])
    }
}

private var visitMedicationRowIdSeed = 0L

private fun nextVisitMedicationRowId(): Long {
    visitMedicationRowIdSeed += 1
    return visitMedicationRowIdSeed
}

private val editVisitDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun formatEditVisitDate(timestamp: Long): String {
    if (timestamp <= 0L) return LocalDateTime.now().format(editVisitDateFormatter)
    return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        .format(editVisitDateFormatter)
}

private fun parseVisitDateText(text: String): Long {
    return parseEditVisitDateTimeOrNull(text)
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: System.currentTimeMillis()
}

private fun parseEditVisitDateTimeOrNull(text: String): LocalDateTime? {
    return runCatching {
        LocalDateTime.parse(text.trim(), editVisitDateFormatter)
    }.getOrNull()
}

private fun formatVisitDate(timestamp: Long): String {
    if (timestamp <= 0L) return "日期未记录"
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm"))
}

private fun Modifier.moveFocusOnEnter(focusManager: FocusManager): Modifier = onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
        focusManager.moveFocus(FocusDirection.Down)
        true
    } else {
        false
    }
}

private fun Modifier.moveFocusOnEnter(nextRequester: FocusRequester): Modifier = onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
        nextRequester.requestFocus()
        true
    } else {
        false
    }
}

private fun Modifier.moveFocusOnEnter(
    onEnter: (() -> Unit)?,
    nextRequester: FocusRequester
): Modifier = onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
        onEnter?.invoke() ?: nextRequester.requestFocus()
        true
    } else {
        false
    }
}

private data class ClinicalNoteParts(
    val medicationAdjustments: String,
    val clinicalNote: String
)

private fun splitClinicalNote(text: String): ClinicalNoteParts {
    val trimmed = text.trim()
    val prefix = "加减用药："
    if (!trimmed.startsWith(prefix)) {
        return ClinicalNoteParts(
            medicationAdjustments = "",
            clinicalNote = trimmed
        )
    }
    val content = trimmed.removePrefix(prefix)
    val separatorIndex = content.indexOf("\n\n")
    return if (separatorIndex == -1) {
        ClinicalNoteParts(
            medicationAdjustments = content.trim(),
            clinicalNote = ""
        )
    } else {
        ClinicalNoteParts(
            medicationAdjustments = content.substring(0, separatorIndex).trim(),
            clinicalNote = content.substring(separatorIndex + 2).trim()
        )
    }
}

private fun buildClinicalNote(
    medicationAdjustments: String,
    clinicalNote: String
): String {
    val adjustments = medicationAdjustments.trim()
    val note = clinicalNote.trim()
    return when {
        adjustments.isNotBlank() && note.isNotBlank() -> "加减用药：$adjustments\n\n$note"
        adjustments.isNotBlank() -> "加减用药：$adjustments"
        else -> note
    }
}
