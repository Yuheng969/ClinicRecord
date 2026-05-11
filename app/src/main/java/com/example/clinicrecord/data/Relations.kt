package com.example.clinicrecord.data

import androidx.room.Embedded
import androidx.room.Relation

data class PatientWithVisits(
    @Embedded
    val patient: Patient,
    @Relation(
        parentColumn = "patientId",
        entityColumn = "patientOwnerId"
    )
    val visits: List<Visit>
)

data class PatientWithFullVisits(
    @Embedded
    val patient: Patient,
    @Relation(
        entity = Visit::class,
        parentColumn = "patientId",
        entityColumn = "patientOwnerId"
    )
    val visits: List<VisitWithPrescriptionAndItems>
)

data class VisitWithPrescription(
    @Embedded
    val visit: Visit,
    @Relation(
        parentColumn = "visitId",
        entityColumn = "visitOwnerId"
    )
    val prescription: Prescription?
)

data class PrescriptionWithItems(
    @Embedded
    val prescription: Prescription,
    @Relation(
        parentColumn = "prescriptionId",
        entityColumn = "prescriptionOwnerId"
    )
    val items: List<MedicationItem>
)

data class VisitWithPrescriptionAndItems(
    @Embedded
    val visit: Visit,
    @Relation(
        entity = Prescription::class,
        parentColumn = "visitId",
        entityColumn = "visitOwnerId"
    )
    val prescriptionWithItems: PrescriptionWithItems?
)

data class ExperienceFormulaWithItems(
    @Embedded
    val experienceFormula: ExperienceFormula,
    @Relation(
        parentColumn = "experienceFormulaId",
        entityColumn = "experienceFormulaOwnerId"
    )
    val items: List<ExperienceFormulaItem>
)

data class MedicationUsageStat(
    val medicationName: String,
    val usageCount: Long
)
