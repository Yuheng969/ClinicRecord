package com.example.clinicrecord.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "patients")
data class Patient(
    @PrimaryKey(autoGenerate = true)
    val patientId: Long = 0,
    val name: String,
    val gender: String,
    val age: Int,
    val contact: String,
    val nativePlace: String,
    @ColumnInfo(defaultValue = "'未分类'")
    val folderName: String = "未分类",
    val pastHistory: String
)

@Entity(tableName = "clinic_folders")
data class ClinicFolder(
    @PrimaryKey
    val folderName: String
)

@Entity(tableName = "deleted_records")
data class DeletedRecord(
    @PrimaryKey(autoGenerate = true)
    val deletedRecordId: Long = 0,
    val recordType: String,
    val title: String,
    val summary: String,
    @ColumnInfo(defaultValue = "''")
    val payload: String = "",
    val deletedAt: Long
)

@Entity(
    tableName = "visits",
    foreignKeys = [
        ForeignKey(
            entity = Patient::class,
            parentColumns = ["patientId"],
            childColumns = ["patientOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("patientOwnerId")]
)
data class Visit(
    @PrimaryKey(autoGenerate = true)
    val visitId: Long = 0,
    val patientOwnerId: Long,
    val visitDate: Long,
    val solarTerm: String,
    val weather: String,
    val chiefComplaint: String,
    val presentIllness: String,
    val tongueImagePath: String,
    val pulseDescription: String,
    val corePathogenesis: String,
    val treatmentMethod: String,
    @ColumnInfo(defaultValue = "''")
    val patentMedicineAcupuncture: String = "",
    val clinicalNote: String
)

@Entity(
    tableName = "prescriptions",
    foreignKeys = [
        ForeignKey(
            entity = Visit::class,
            parentColumns = ["visitId"],
            childColumns = ["visitOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["visitOwnerId"], unique = true)
    ]
)
data class Prescription(
    @PrimaryKey(autoGenerate = true)
    val prescriptionId: Long = 0,
    val visitOwnerId: Long,
    val formulaName: String,
    val doseCount: Int,
    val usage: String
)

@Entity(
    tableName = "medication_items",
    foreignKeys = [
        ForeignKey(
            entity = Prescription::class,
            parentColumns = ["prescriptionId"],
            childColumns = ["prescriptionOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("prescriptionOwnerId")]
)
data class MedicationItem(
    @PrimaryKey(autoGenerate = true)
    val medicationItemId: Long = 0,
    val prescriptionOwnerId: Long,
    val medicationName: String,
    val dosage: Double,
    val unit: String = "g",
    val decoctionMethod: String
)

@Entity(tableName = "experience_formulas")
data class ExperienceFormula(
    @PrimaryKey(autoGenerate = true)
    val experienceFormulaId: Long = 0,
    val formulaName: String,
    val indicatedPathogenesis: String
)

@Entity(
    tableName = "experience_formula_items",
    foreignKeys = [
        ForeignKey(
            entity = ExperienceFormula::class,
            parentColumns = ["experienceFormulaId"],
            childColumns = ["experienceFormulaOwnerId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("experienceFormulaOwnerId")]
)
data class ExperienceFormulaItem(
    @PrimaryKey(autoGenerate = true)
    val experienceFormulaItemId: Long = 0,
    val experienceFormulaOwnerId: Long,
    val medicationName: String,
    val dosage: Double,
    val unit: String = "g",
    val decoctionMethod: String
)
