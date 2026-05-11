package com.example.clinicrecord.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.clinicrecord.data.PatientWithFullVisits
import com.example.clinicrecord.data.VisitWithPrescriptionAndItems
import com.example.clinicrecord.viewmodel.ClinicViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientDetailScreen(
    patientId: Long,
    viewModel: ClinicViewModel,
    onBack: () -> Unit,
    onNewVisit: (Long) -> Unit,
    onVisitClick: (Long) -> Unit
) {
    val patientWithVisits by viewModel
        .getPatientWithFullVisits(patientId)
        .collectAsState(initial = null)
    var showEditPatientDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("患者详情") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNewVisit(patientId) },
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Text("新诊")
            }
        }
    ) { innerPadding ->
        val detail = patientWithVisits
        if (detail == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(20.dp)
            ) {
                Text(
                    text = "未找到患者记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            PatientDetailContent(
                detail = detail,
                modifier = Modifier
                    .padding(innerPadding),
                onVisitClick = onVisitClick,
                onEditPatient = { showEditPatientDialog = true }
            )
        }
    }

    val currentDetail = patientWithVisits
    if (showEditPatientDialog && currentDetail != null) {
        EditPatientDialog(
            patient = currentDetail.patient,
            viewModel = viewModel,
            onDismiss = { showEditPatientDialog = false }
        )
    }
}

@Composable
private fun PatientDetailContent(
    detail: PatientWithFullVisits,
    modifier: Modifier = Modifier,
    onVisitClick: (Long) -> Unit,
    onEditPatient: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            PatientProfileCard(
                detail = detail,
                onEdit = onEditPatient
            )
        }

        item {
            Text(
                text = "就诊历史",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (detail.visits.isEmpty()) {
            item {
                Text(
                    text = "暂无就诊记录",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            }
        } else {
            items(
                items = detail.visits.sortedByDescending { it.visit.visitDate },
                key = { it.visit.visitId }
            ) { visitWithPrescription ->
                VisitHistoryCard(
                    visitWithPrescription = visitWithPrescription,
                    onClick = { onVisitClick(visitWithPrescription.visit.visitId) }
                )
            }
        }
    }
}

@Composable
private fun PatientProfileCard(
    detail: PatientWithFullVisits,
    onEdit: () -> Unit
) {
    val patient = detail.patient
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = patient.name,
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onEdit) {
                    Text("编辑")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "${patient.gender} · ${patient.age}岁",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "籍贯：${patient.nativePlace.ifBlank { "未记录" }}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "既往史：${patient.pastHistory.ifBlank { "未记录" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun VisitHistoryCard(
    visitWithPrescription: VisitWithPrescriptionAndItems,
    onClick: () -> Unit
) {
    val visit = visitWithPrescription.visit
    val prescription = visitWithPrescription.prescriptionWithItems?.prescription

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            Text(
                text = formatVisitDate(visit.visitDate),
                style = MaterialTheme.typography.titleSmall
            )
            Text(
                text = "${visit.solarTerm.ifBlank { "节气未记录" }} · ${visit.weather.ifBlank { "天气未记录" }}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "主诉：${visit.chiefComplaint.ifBlank { "未记录" }}",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "核心病机：${visit.corePathogenesis.ifBlank { "未记录" }}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (prescription != null) {
                Text(
                    text = "处方：${prescription.formulaName.ifBlank { "未命名" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
            }
            if (visit.clinicalNote.isNotBlank()) {
                Text(
                    text = "按语：${visit.clinicalNote}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun formatVisitDate(timestamp: Long): String {
    if (timestamp <= 0L) return "日期未记录"
    return Instant.ofEpochMilli(timestamp)
        .atZone(ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("yyyy年MM月dd日"))
}
