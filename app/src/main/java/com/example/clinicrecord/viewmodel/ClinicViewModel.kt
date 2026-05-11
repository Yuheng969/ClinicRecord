package com.example.clinicrecord.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.clinicrecord.data.ClinicDao
import com.example.clinicrecord.data.ClinicFolder
import com.example.clinicrecord.data.DeepSeekClinicalApi
import com.example.clinicrecord.data.DeepSeekClinicalRequest
import com.example.clinicrecord.data.DeletedRecord
import com.example.clinicrecord.data.MedicationItem
import com.example.clinicrecord.data.MedicationUsageStat
import com.example.clinicrecord.data.Patient
import com.example.clinicrecord.data.PatientWithFullVisits
import com.example.clinicrecord.data.Prescription
import com.example.clinicrecord.data.Visit
import com.example.clinicrecord.data.VisitWithPrescriptionAndItems
import com.example.clinicrecord.ui.theme.AppColorStyle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.Collator
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalCoroutinesApi::class)
class ClinicViewModel(
    private val clinicDao: ClinicDao,
    initialColorStyle: AppColorStyle = AppColorStyle.Sage
) : ViewModel() {
    val selectedFolder = MutableStateFlow(FOLDER_ALL)
    val aiClinicalReference = MutableStateFlow("")
    val selectedColorStyle = MutableStateFlow(initialColorStyle)

    val allPatients: StateFlow<List<Patient>> = clinicDao.observeAllPatients()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val folders: StateFlow<List<String>> = clinicDao.getAllFolders()
        .map { folders ->
            folders.sortedWith(Collator.getInstance(Locale.CHINA))
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val patients: StateFlow<List<Patient>> = selectedFolder
        .flatMapLatest { folder ->
            when (folder) {
                FOLDER_ALL -> clinicDao.observeAllPatients()
                else -> clinicDao.getPatientsByFolder(folder)
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val recentDeletedRecords: StateFlow<List<DeletedRecord>> = clinicDao
        .observeDeletedRecordsSince(System.currentTimeMillis() - RECENT_DELETE_RETENTION_MILLIS)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    val medicationUsageStats: StateFlow<List<MedicationUsageStat>> = clinicDao
        .observeMedicationUsageStats()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun selectFolder(folderName: String) {
        selectedFolder.value = folderName.ifBlank { FOLDER_UNCATEGORIZED }
    }

    fun selectColorStyle(style: AppColorStyle) {
        selectedColorStyle.value = style
    }

    fun createFolder(folderName: String) {
        val normalized = folderName.trim()
        if (normalized.isBlank() || normalized == FOLDER_ALL || normalized == FOLDER_UNCATEGORIZED) return
        viewModelScope.launch {
            clinicDao.insertFolder(ClinicFolder(normalized))
            selectedFolder.value = normalized
        }
    }

    fun renameFolder(oldName: String, newName: String) {
        val normalized = newName.trim()
        if (normalized.isBlank() || oldName == FOLDER_UNCATEGORIZED || oldName == FOLDER_ALL) return
        viewModelScope.launch {
            clinicDao.renameFolder(oldName, normalized)
            if (selectedFolder.value == oldName) {
                selectedFolder.value = normalized
            }
        }
    }

    fun deleteFolder(folderName: String, deletePatients: Boolean = false) {
        if (folderName == FOLDER_UNCATEGORIZED || folderName == FOLDER_ALL) return
        viewModelScope.launch {
            if (deletePatients) {
                val patientsInFolder = clinicDao.getPatientListByFolder(folderName)
                clinicDao.insertDeletedRecord(
                    DeletedRecord(
                        recordType = "文件夹",
                        title = folderName,
                        summary = "删除文件夹和其中 ${patientsInFolder.size} 位患者",
                        payload = patientsInFolder.joinToString(separator = "\n") { it.name },
                        deletedAt = System.currentTimeMillis()
                    )
                )
                patientsInFolder.forEach { patient ->
                    clinicDao.insertDeletedRecord(patient.toDeletedRecord())
                }
                if (patientsInFolder.isNotEmpty()) {
                    clinicDao.deletePatients(patientsInFolder)
                }
                clinicDao.deleteFolderByName(folderName)
            } else {
                clinicDao.insertDeletedRecord(
                    DeletedRecord(
                        recordType = "文件夹",
                        title = folderName,
                        summary = "仅删除文件夹，患者已移动到未分类",
                        payload = folderName,
                        deletedAt = System.currentTimeMillis()
                    )
                )
                clinicDao.deleteFolder(folderName)
            }
            if (selectedFolder.value == folderName) {
                selectedFolder.value = FOLDER_ALL
            }
        }
    }

    fun addPatient(
        name: String,
        gender: String,
        age: Int,
        contact: String,
        nativePlace: String,
        folderName: String,
        pastHistory: String
    ) {
        viewModelScope.launch {
            clinicDao.insertPatient(
                Patient(
                    name = name.trim(),
                    gender = gender.trim(),
                    age = age,
                    contact = contact.trim(),
                    nativePlace = nativePlace.trim(),
                    folderName = folderName.trim().ifBlank { FOLDER_UNCATEGORIZED },
                    pastHistory = pastHistory.trim()
                )
            )
        }
    }

    fun updatePatientProfile(
        patient: Patient,
        name: String,
        gender: String,
        age: Int,
        contact: String,
        nativePlace: String,
        folderName: String,
        pastHistory: String
    ) {
        viewModelScope.launch {
            clinicDao.updatePatient(
                patient.copy(
                    name = name.trim(),
                    gender = gender.trim(),
                    age = age,
                    contact = contact.trim(),
                    nativePlace = nativePlace.trim(),
                    folderName = folderName.trim().ifBlank { FOLDER_UNCATEGORIZED },
                    pastHistory = pastHistory.trim()
                )
            )
        }
    }

    fun deletePatient(patient: Patient) {
        viewModelScope.launch {
            clinicDao.insertDeletedRecord(patient.toDeletedRecord())
            clinicDao.deletePatient(patient)
        }
    }

    fun getPatientWithFullVisits(patientId: Long): Flow<PatientWithFullVisits?> {
        return clinicDao.observePatientWithFullVisits(patientId)
    }

    fun getVisitWithPrescriptionAndItems(visitId: Long): Flow<VisitWithPrescriptionAndItems?> {
        return clinicDao.observeVisitWithPrescriptionAndItems(visitId)
    }

    fun importLatestPrescription(
        patientId: Long,
        onImported: (PrescriptionDraft) -> Unit
    ) {
        viewModelScope.launch {
            val latest = clinicDao.getLatestVisitWithPrescriptionAndItems(patientId) ?: return@launch
            val prescription = latest.prescriptionWithItems?.prescription
            val noteParts = latest.visit.clinicalNote.toClinicalNoteParts()
            onImported(
                PrescriptionDraft(
                    weather = latest.visit.weather,
                    chiefComplaint = latest.visit.chiefComplaint,
                    presentIllness = latest.visit.presentIllness,
                    pulseDescription = latest.visit.pulseDescription,
                    corePathogenesis = latest.visit.corePathogenesis,
                    treatmentMethod = latest.visit.treatmentMethod,
                    formulaName = prescription?.formulaName ?: "自拟方",
                    doseCount = prescription?.doseCount ?: 7,
                    usage = prescription?.usage ?: "水煎服",
                    patentMedicineAcupuncture = latest.visit.patentMedicineAcupuncture,
                    medicationAdjustments = noteParts.medicationAdjustments,
                    clinicalNote = noteParts.clinicalNote,
                    medications = latest.prescriptionWithItems?.items.orEmpty().map {
                        ConsultationMedicationInput(
                            name = it.medicationName,
                            dosage = it.dosage,
                            decoctionMethod = it.decoctionMethod
                        )
                    }
                )
            )
        }
    }

    fun requestAiClinicalReference(
        apiKey: String,
        chiefComplaint: String,
        presentIllness: String,
        pulseDescription: String,
        corePathogenesis: String
    ) {
        viewModelScope.launch {
            aiClinicalReference.value = "正在生成参考..."
            val result = DeepSeekClinicalApi.requestClinicalReference(
                apiKey = apiKey,
                request = DeepSeekClinicalRequest(
                    chiefComplaint = chiefComplaint,
                    presentIllness = presentIllness,
                    pulseDescription = pulseDescription,
                    corePathogenesis = corePathogenesis
                )
            )
            aiClinicalReference.value = result.getOrElse { error ->
                "AI参考暂不可用：${error.message ?: "请求失败"}"
            }
        }
    }

    fun deleteVisit(detail: VisitWithPrescriptionAndItems) {
        viewModelScope.launch {
            clinicDao.insertDeletedRecord(detail.toDeletedRecord())
            clinicDao.deleteVisit(detail.visit)
        }
    }

    fun purgeExpiredDeletedRecords() {
        viewModelScope.launch {
            clinicDao.purgeDeletedRecordsBefore(System.currentTimeMillis() - RECENT_DELETE_RETENTION_MILLIS)
        }
    }

    fun restoreDeletedRecord(record: DeletedRecord, onRestored: () -> Unit = {}) {
        viewModelScope.launch {
            when (record.recordType) {
                "文件夹" -> {
                    clinicDao.insertFolder(ClinicFolder(record.title))
                }
                "患者" -> {
                    val payload = record.payload.toPayloadMap()
                    val folderName = payload["folderName"] ?: payload["分类"] ?: FOLDER_UNCATEGORIZED
                    if (folderName != FOLDER_UNCATEGORIZED && folderName.isNotBlank()) {
                        clinicDao.insertFolder(ClinicFolder(folderName))
                    }
                    clinicDao.insertPatient(
                        Patient(
                            name = payload["name"] ?: payload["姓名"] ?: record.title,
                            gender = payload["gender"] ?: payload["性别"] ?: "",
                            age = (payload["age"] ?: payload["年龄"]).orEmpty().toIntOrNull() ?: 0,
                            contact = payload["contact"] ?: payload["联系方式"] ?: "",
                            nativePlace = payload["nativePlace"] ?: payload["籍贯"] ?: "",
                            folderName = folderName,
                            pastHistory = payload["pastHistory"] ?: payload["既往史"] ?: ""
                        )
                    )
                }
                "就诊" -> {
                    val payload = record.payload.toPayloadMap()
                    val patientOwnerId = payload["patientOwnerId"]?.toLongOrNull() ?: return@launch
                    val visit = Visit(
                        patientOwnerId = patientOwnerId,
                        visitDate = payload["visitDate"]?.toLongOrNull() ?: System.currentTimeMillis(),
                        solarTerm = payload["solarTerm"].orEmpty(),
                        weather = payload["weather"].orEmpty(),
                        chiefComplaint = payload["chiefComplaint"].orEmpty(),
                        presentIllness = payload["presentIllness"].orEmpty(),
                        tongueImagePath = "",
                        pulseDescription = payload["pulseDescription"].orEmpty(),
                        corePathogenesis = payload["corePathogenesis"].orEmpty(),
                        treatmentMethod = payload["treatmentMethod"].orEmpty(),
                        patentMedicineAcupuncture = payload["patentMedicineAcupuncture"].orEmpty(),
                        clinicalNote = payload["clinicalNote"].orEmpty()
                    )
                    val prescription = Prescription(
                        visitOwnerId = 0,
                        formulaName = payload["formulaName"] ?: "自拟方",
                        doseCount = payload["doseCount"]?.toIntOrNull() ?: 7,
                        usage = payload["usage"] ?: "水煎服"
                    )
                    val medications = record.payload
                        .lineSequence()
                        .filter { it.startsWith("medication=") }
                        .mapNotNull { line ->
                            val parts = line.removePrefix("medication=").split("|")
                            if (parts.isEmpty() || parts[0].isBlank()) {
                                null
                            } else {
                                MedicationItem(
                                    prescriptionOwnerId = 0,
                                    medicationName = parts.getOrNull(0).orEmpty(),
                                    dosage = parts.getOrNull(1)?.toDoubleOrNull() ?: 0.0,
                                    unit = parts.getOrNull(2)?.ifBlank { "g" } ?: "g",
                                    decoctionMethod = parts.getOrNull(3).orEmpty()
                                )
                            }
                        }
                        .toList()
                    clinicDao.insertFullConsultation(visit, prescription, medications)
                }
            }
            clinicDao.deleteDeletedRecord(record)
            onRestored()
        }
    }

    fun permanentlyDeleteRecord(record: DeletedRecord) {
        viewModelScope.launch {
            clinicDao.deleteDeletedRecord(record)
        }
    }

    fun moveEssayToTrash(text: String) {
        val normalized = text.trim()
        if (normalized.isBlank()) return
        viewModelScope.launch {
            clinicDao.insertDeletedRecord(
                DeletedRecord(
                    recordType = "随笔",
                    title = normalized.lineSequence().firstOrNull().orEmpty().take(18).ifBlank { "未命名随笔" },
                    summary = normalized.replace('\n', ' ').take(60),
                    payload = normalized,
                    deletedAt = System.currentTimeMillis()
                )
            )
        }
    }

    fun saveFullConsultation(
        patientId: Long,
        visitDate: Long,
        solarTerm: String,
        weather: String,
        chiefComplaint: String,
        presentIllness: String,
        pulseDescription: String,
        corePathogenesis: String,
        treatmentMethod: String,
        patentMedicineAcupuncture: String,
        clinicalNote: String,
        formulaName: String,
        doseCount: Int,
        usage: String,
        medicationInputs: List<ConsultationMedicationInput>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val visit = Visit(
                patientOwnerId = patientId,
                visitDate = visitDate,
                solarTerm = solarTerm.trim(),
                weather = weather.trim(),
                chiefComplaint = chiefComplaint.trim(),
                presentIllness = presentIllness.trim(),
                tongueImagePath = "",
                pulseDescription = pulseDescription.trim(),
                corePathogenesis = corePathogenesis.trim(),
                treatmentMethod = treatmentMethod.trim(),
                patentMedicineAcupuncture = patentMedicineAcupuncture.trim(),
                clinicalNote = clinicalNote.trim()
            )
            val prescription = Prescription(
                visitOwnerId = 0,
                formulaName = formulaName.trim().ifBlank { "自拟方" },
                doseCount = doseCount,
                usage = usage.trim().ifBlank { "水煎服" }
            )
            val medicationItems = medicationInputs
                .filter { it.name.isNotBlank() }
                .map { input ->
                    MedicationItem(
                        prescriptionOwnerId = 0,
                        medicationName = input.name.trim(),
                        dosage = input.dosage,
                        decoctionMethod = input.decoctionMethod.trim()
                    )
                }

            clinicDao.insertFullConsultation(
                visit = visit,
                prescription = prescription,
                medicationItems = medicationItems
            )
            onSaved()
        }
    }

    fun updateFullConsultation(
        current: VisitWithPrescriptionAndItems,
        visitDate: Long,
        solarTerm: String,
        weather: String,
        chiefComplaint: String,
        presentIllness: String,
        pulseDescription: String,
        corePathogenesis: String,
        treatmentMethod: String,
        patentMedicineAcupuncture: String,
        clinicalNote: String,
        formulaName: String,
        doseCount: Int,
        usage: String,
        medicationInputs: List<ConsultationMedicationInput>,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            val visit = current.visit.copy(
                visitDate = visitDate,
                solarTerm = solarTerm.trim(),
                weather = weather.trim(),
                chiefComplaint = chiefComplaint.trim(),
                presentIllness = presentIllness.trim(),
                pulseDescription = pulseDescription.trim(),
                corePathogenesis = corePathogenesis.trim(),
                treatmentMethod = treatmentMethod.trim(),
                patentMedicineAcupuncture = patentMedicineAcupuncture.trim(),
                clinicalNote = clinicalNote.trim()
            )
            val oldPrescription = current.prescriptionWithItems?.prescription
            val prescription = Prescription(
                prescriptionId = oldPrescription?.prescriptionId ?: 0,
                visitOwnerId = current.visit.visitId,
                formulaName = formulaName.trim().ifBlank { "自拟方" },
                doseCount = doseCount,
                usage = usage.trim().ifBlank { "水煎服" }
            )
            val medicationItems = medicationInputs
                .filter { it.name.isNotBlank() }
                .map { input ->
                    MedicationItem(
                        prescriptionOwnerId = oldPrescription?.prescriptionId ?: 0,
                        medicationName = input.name.trim(),
                        dosage = input.dosage,
                        decoctionMethod = input.decoctionMethod.trim()
                    )
                }

            clinicDao.updateFullConsultation(
                visit = visit,
                prescription = prescription,
                medicationItems = medicationItems
            )
            onSaved()
        }
    }

    companion object {
        const val FOLDER_ALL = "全部"
        const val FOLDER_UNCATEGORIZED = "未分类"
        private const val RECENT_DELETE_RETENTION_MILLIS = 7L * 24L * 60L * 60L * 1000L
    }
}

private fun Patient.toDeletedRecord(): DeletedRecord {
    return DeletedRecord(
        recordType = "患者",
        title = name.ifBlank { "未命名患者" },
        summary = "${gender.ifBlank { "性别未记录" }} · ${age}岁 · ${folderName.ifBlank { ClinicViewModel.FOLDER_UNCATEGORIZED }}",
        payload = buildString {
            appendLine("name=$name")
            appendLine("gender=$gender")
            appendLine("age=$age")
            appendLine("contact=$contact")
            appendLine("nativePlace=$nativePlace")
            appendLine("folderName=$folderName")
            appendLine("pastHistory=$pastHistory")
        },
        deletedAt = System.currentTimeMillis()
    )
}

private fun VisitWithPrescriptionAndItems.toDeletedRecord(): DeletedRecord {
    val formatter = DateTimeFormatter.ofPattern("yyyy年MM月dd日 HH:mm")
    val dateText = LocalDateTime.ofInstant(
        java.time.Instant.ofEpochMilli(visit.visitDate),
        ZoneId.systemDefault()
    ).format(formatter)
    return DeletedRecord(
        recordType = "就诊",
        title = dateText,
        summary = visit.chiefComplaint.ifBlank {
            prescriptionWithItems?.prescription?.formulaName?.ifBlank { "未记录主诉" } ?: "未记录主诉"
        },
        payload = buildString {
            appendLine("patientOwnerId=${visit.patientOwnerId}")
            appendLine("visitDate=${visit.visitDate}")
            appendLine("solarTerm=${visit.solarTerm}")
            appendLine("weather=${visit.weather}")
            appendLine("chiefComplaint=${visit.chiefComplaint}")
            appendLine("presentIllness=${visit.presentIllness}")
            appendLine("pulseDescription=${visit.pulseDescription}")
            appendLine("corePathogenesis=${visit.corePathogenesis}")
            appendLine("treatmentMethod=${visit.treatmentMethod}")
            appendLine("patentMedicineAcupuncture=${visit.patentMedicineAcupuncture}")
            appendLine("clinicalNote=${visit.clinicalNote}")
            prescriptionWithItems?.prescription?.let { prescription ->
                appendLine("formulaName=${prescription.formulaName}")
                appendLine("doseCount=${prescription.doseCount}")
                appendLine("usage=${prescription.usage}")
            }
            prescriptionWithItems?.items.orEmpty().forEach { item ->
                appendLine("medication=${item.medicationName}|${item.dosage}|${item.unit}|${item.decoctionMethod}")
            }
        },
        deletedAt = System.currentTimeMillis()
    )
}

private fun String.toPayloadMap(): Map<String, String> {
    return lineSequence()
        .mapNotNull { line ->
            val separatorIndex = line.indexOf('=').takeIf { it > 0 }
            if (separatorIndex != null) {
                line.substring(0, separatorIndex) to line.substring(separatorIndex + 1)
            } else {
                val chineseSeparatorIndex = line.indexOf('：').takeIf { it > 0 }
                chineseSeparatorIndex?.let {
                    line.substring(0, it) to line.substring(it + 1)
                }
            }
        }
        .toMap()
}

data class ConsultationMedicationInput(
    val name: String,
    val dosage: Double,
    val decoctionMethod: String
)

data class PrescriptionDraft(
    val weather: String,
    val chiefComplaint: String,
    val presentIllness: String,
    val pulseDescription: String,
    val corePathogenesis: String,
    val treatmentMethod: String,
    val formulaName: String,
    val doseCount: Int,
    val usage: String,
    val patentMedicineAcupuncture: String,
    val medicationAdjustments: String,
    val clinicalNote: String,
    val medications: List<ConsultationMedicationInput>
)

private data class ClinicalNoteParts(
    val medicationAdjustments: String,
    val clinicalNote: String
)

private fun String.toClinicalNoteParts(): ClinicalNoteParts {
    val trimmed = trim()
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
