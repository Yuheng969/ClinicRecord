package com.example.clinicrecord.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp
import com.example.clinicrecord.viewmodel.ClinicViewModel
import com.example.clinicrecord.viewmodel.ConsultationMedicationInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewVisitScreen(
    patientId: Long,
    viewModel: ClinicViewModel,
    onCancel: () -> Unit,
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
    val initialVisitDateText = remember { defaultVisitDateText() }
    var visitDateText by remember { mutableStateOf(initialVisitDateText) }
    var solarTerm by remember {
        mutableStateOf(
            parseVisitDateTimeOrNull(initialVisitDateText)
                ?.let(::solarTermForVisitDate)
                .orEmpty()
        )
    }
    var weather by remember { mutableStateOf("") }
    var chiefComplaint by remember { mutableStateOf("") }
    var presentIllness by remember { mutableStateOf("") }
    var pulseDescription by remember { mutableStateOf("") }
    var corePathogenesis by remember { mutableStateOf("") }
    var treatmentMethod by remember { mutableStateOf("") }
    var formulaName by remember { mutableStateOf("") }
    var doseCountText by remember { mutableStateOf("7") }
    var usage by remember { mutableStateOf("水煎服") }
    var patentMedicineAcupuncture by remember { mutableStateOf("") }
    var medicationAdjustments by remember { mutableStateOf("") }
    var clinicalNote by remember { mutableStateOf("") }
    var pendingMedicationNameFocusId by remember { mutableStateOf<Long?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val medicationRows = remember {
        mutableStateListOf(MedicationRowState())
    }

    fun importLatestVisitInfo() {
        viewModel.importLatestPrescription(patientId) { draft ->
            weather = draft.weather
            chiefComplaint = draft.chiefComplaint
            presentIllness = draft.presentIllness
            pulseDescription = draft.pulseDescription
            corePathogenesis = draft.corePathogenesis
            treatmentMethod = draft.treatmentMethod
            formulaName = draft.formulaName.takeUnless { it == "自拟方" }.orEmpty()
            doseCountText = draft.doseCount.toString()
            usage = draft.usage
            patentMedicineAcupuncture = draft.patentMedicineAcupuncture
            medicationAdjustments = draft.medicationAdjustments
            clinicalNote = draft.clinicalNote
            medicationRows.clear()
            medicationRows.addAll(
                draft.medications.map {
                    MedicationRowState(
                        name = it.name,
                        dosage = it.dosage.toString(),
                        decoctionMethod = it.decoctionMethod
                    )
                }.ifEmpty { listOf(MedicationRowState()) }
            )
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            CenterAlignedTopAppBar(
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary,
                    actionIconContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("新增诊疗") },
                navigationIcon = {
                    TextButton(onClick = onCancel) {
                        Text("取消")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            viewModel.saveFullConsultation(
                                patientId = patientId,
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
                        }
                    ) {
                        Text("保存")
                    }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showImportConfirmDialog = true },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text("导入上次就诊信息")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                FormCard(title = "节气与天气") {
                    OutlinedTextField(
                        value = visitDateText,
                        onValueChange = { value ->
                            visitDateText = value
                            parseVisitDateTimeOrNull(value)?.let { dateTime ->
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
                FormCard(title = "四诊") {
                    OutlinedTextField(
                        value = chiefComplaint,
                        onValueChange = { chiefComplaint = it },
                        label = { Text("主诉") },
                        minLines = 2,
                        keyboardOptions = nextOptions,
                        keyboardActions = nextActions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(chiefComplaintRequester)
                            .moveFocusOnEnter(focusManager)
                    )
                    OutlinedTextField(
                        value = presentIllness,
                        onValueChange = { presentIllness = it },
                        label = { Text("现病史") },
                        minLines = 3,
                        keyboardOptions = nextOptions,
                        keyboardActions = nextActions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .moveFocusOnEnter(focusManager)
                    )
                    OutlinedTextField(
                        value = pulseDescription,
                        onValueChange = { pulseDescription = it },
                        label = { Text("舌脉") },
                        minLines = 2,
                        keyboardOptions = nextOptions,
                        keyboardActions = nextActions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .moveFocusOnEnter(focusManager)
                    )
                }
            }

            item {
                FormCard(title = "辨证") {
                    OutlinedTextField(
                        value = corePathogenesis,
                        onValueChange = { corePathogenesis = it },
                        label = { Text("核心病机") },
                        minLines = 2,
                        keyboardOptions = nextOptions,
                        keyboardActions = nextActions,
                        modifier = Modifier
                            .fillMaxWidth()
                            .moveFocusOnEnter(focusManager)
                    )
                    OutlinedTextField(
                        value = treatmentMethod,
                        onValueChange = { treatmentMethod = it },
                        label = { Text("治法") },
                        minLines = 2,
                        keyboardOptions = nextOptions,
                        keyboardActions = KeyboardActions(onNext = { formulaNameRequester.requestFocus() }),
                        modifier = Modifier
                            .fillMaxWidth()
                            .moveFocusOnEnter(formulaNameRequester)
                    )
                }
            }

            item {
                FormCard(title = "处方") {
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
                    }
                    medicationRows.forEachIndexed { index, row ->
                        val nextRow = medicationRows.getOrNull(index + 1)
                        MedicationInputRow(
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
                                    medicationRows[0] = MedicationRowState(id = row.id)
                                }
                            },
                            onAddNextRow = if (nextRow == null) {
                                {
                                    val newRow = MedicationRowState()
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
                    OutlinedButton(
                        onClick = { medicationRows.add(MedicationRowState()) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("+ 添加药材")
                    }
                }
            }

            item {
                FormCard(title = "剂数与煎服方法") {
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
                FormCard(title = "配合使用成药/针灸") {
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
                FormCard(title = "加减用药") {
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
                FormCard(title = "感悟") {
                    OutlinedTextField(
                        value = clinicalNote,
                        onValueChange = { clinicalNote = it },
                        placeholder = { Text("记录本次思路、预后判断或特殊观察点") },
                        minLines = 4,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }
    }

    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("导入上次就诊信息？") },
            text = { Text("导入后会覆盖当前页面中已填写的就诊信息。") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImportConfirmDialog = false
                        importLatestVisitInfo()
                    }
                ) {
                    Text("导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun MedicationInputRow(
    row: MedicationRowState,
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
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
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
}

@Composable
private fun FormCard(
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
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium
            )
            content()
        }
    }
}

private data class MedicationRowState(
    val id: Long = nextMedicationRowId(),
    val name: String = "",
    val dosage: String = "",
    val decoctionMethod: String = "",
    val nameRequester: FocusRequester = FocusRequester(),
    val dosageRequester: FocusRequester = FocusRequester()
)

private fun MutableList<MedicationRowState>.update(
    id: Long,
    transform: (MedicationRowState) -> MedicationRowState
) {
    val index = indexOfFirst { it.id == id }
    if (index != -1) {
        this[index] = transform(this[index])
    }
}

private var medicationRowIdSeed = 0L

private fun nextMedicationRowId(): Long {
    medicationRowIdSeed += 1
    return medicationRowIdSeed
}

private val visitDateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

private fun defaultVisitDateText(): String {
    return LocalDateTime.now().format(visitDateFormatter)
}

private fun parseVisitDateText(text: String): Long {
    return parseVisitDateTimeOrNull(text)
        ?.atZone(ZoneId.systemDefault())
        ?.toInstant()
        ?.toEpochMilli()
        ?: System.currentTimeMillis()
}

private fun parseVisitDateTimeOrNull(text: String): LocalDateTime? {
    return runCatching {
        LocalDateTime.parse(text.trim(), visitDateFormatter)
    }.getOrNull()
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
