package com.example.clinicrecord.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ClinicDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPatient(patient: Patient): Long

    @Update
    abstract suspend fun updatePatient(patient: Patient)

    @Delete
    abstract suspend fun deletePatient(patient: Patient)

    @Delete
    abstract suspend fun deletePatients(patients: List<Patient>)

    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    abstract suspend fun getPatientById(patientId: Long): Patient?

    @Query("SELECT * FROM patients ORDER BY patientId DESC")
    abstract suspend fun getAllPatients(): List<Patient>

    @Query("SELECT * FROM patients ORDER BY patientId DESC")
    abstract fun observeAllPatients(): Flow<List<Patient>>

    @Query(
        "SELECT folderName FROM clinic_folders " +
            "WHERE TRIM(folderName) != '' AND folderName != '未分类' " +
            "ORDER BY folderName"
    )
    abstract fun getAllFolders(): Flow<List<String>>

    @Query("SELECT * FROM patients WHERE folderName = :folderName ORDER BY patientId DESC")
    abstract fun getPatientsByFolder(folderName: String): Flow<List<Patient>>

    @Query("SELECT * FROM patients WHERE folderName = :folderName ORDER BY patientId DESC")
    abstract suspend fun getPatientListByFolder(folderName: String): List<Patient>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertDeletedRecord(record: DeletedRecord): Long

    @Query("SELECT * FROM deleted_records WHERE deletedAt >= :since ORDER BY deletedAt DESC")
    abstract fun observeDeletedRecordsSince(since: Long): Flow<List<DeletedRecord>>

    @Query("DELETE FROM deleted_records WHERE deletedAt < :threshold")
    abstract suspend fun purgeDeletedRecordsBefore(threshold: Long)

    @Delete
    abstract suspend fun deleteDeletedRecord(record: DeletedRecord)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract suspend fun insertFolder(folder: ClinicFolder)

    @Query("DELETE FROM clinic_folders WHERE folderName = :folderName")
    abstract suspend fun deleteFolderByName(folderName: String)

    @Query("UPDATE patients SET folderName = '未分类' WHERE folderName = :folderName")
    abstract suspend fun movePatientsToUncategorized(folderName: String)

    @Query("UPDATE patients SET folderName = :newName WHERE folderName = :oldName")
    abstract suspend fun updatePatientFolderName(oldName: String, newName: String)

    @Transaction
    open suspend fun renameFolder(oldName: String, newName: String) {
        if (oldName == newName || oldName == "未分类" || oldName.isBlank() || newName.isBlank()) return
        insertFolder(ClinicFolder(newName))
        updatePatientFolderName(oldName, newName)
        deleteFolderByName(oldName)
    }

    @Transaction
    open suspend fun deleteFolder(folderName: String) {
        if (folderName == "未分类" || folderName.isBlank()) return
        movePatientsToUncategorized(folderName)
        deleteFolderByName(folderName)
    }

    @Transaction
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    abstract suspend fun getPatientWithVisits(patientId: Long): PatientWithVisits?

    @Transaction
    @Query("SELECT * FROM patients WHERE patientId = :patientId")
    abstract fun observePatientWithFullVisits(patientId: Long): Flow<PatientWithFullVisits?>

    @Transaction
    @Query("SELECT * FROM patients ORDER BY patientId DESC")
    abstract suspend fun getAllPatientsWithVisits(): List<PatientWithVisits>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertVisit(visit: Visit): Long

    @Update
    abstract suspend fun updateVisit(visit: Visit)

    @Delete
    abstract suspend fun deleteVisit(visit: Visit)

    @Query("SELECT * FROM visits WHERE visitId = :visitId")
    abstract suspend fun getVisitById(visitId: Long): Visit?

    @Query("SELECT * FROM visits WHERE patientOwnerId = :patientId ORDER BY visitDate DESC")
    abstract suspend fun getVisitsForPatient(patientId: Long): List<Visit>

    @Transaction
    @Query("SELECT * FROM visits WHERE visitId = :visitId")
    abstract suspend fun getVisitWithPrescription(visitId: Long): VisitWithPrescription?

    @Transaction
    @Query("SELECT * FROM visits WHERE visitId = :visitId")
    abstract suspend fun getVisitWithPrescriptionAndItems(visitId: Long): VisitWithPrescriptionAndItems?

    @Transaction
    @Query("SELECT * FROM visits WHERE visitId = :visitId")
    abstract fun observeVisitWithPrescriptionAndItems(visitId: Long): Flow<VisitWithPrescriptionAndItems?>

    @Transaction
    @Query("SELECT * FROM visits WHERE patientOwnerId = :patientId ORDER BY visitDate DESC LIMIT 1")
    abstract suspend fun getLatestVisitWithPrescriptionAndItems(patientId: Long): VisitWithPrescriptionAndItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertPrescription(prescription: Prescription): Long

    @Update
    abstract suspend fun updatePrescription(prescription: Prescription)

    @Delete
    abstract suspend fun deletePrescription(prescription: Prescription)

    @Query("SELECT * FROM prescriptions WHERE prescriptionId = :prescriptionId")
    abstract suspend fun getPrescriptionById(prescriptionId: Long): Prescription?

    @Query("SELECT * FROM prescriptions WHERE visitOwnerId = :visitId")
    abstract suspend fun getPrescriptionForVisit(visitId: Long): Prescription?

    @Transaction
    @Query("SELECT * FROM prescriptions WHERE prescriptionId = :prescriptionId")
    abstract suspend fun getPrescriptionWithItems(prescriptionId: Long): PrescriptionWithItems?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMedicationItem(item: MedicationItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertMedicationItems(items: List<MedicationItem>): List<Long>

    @Update
    abstract suspend fun updateMedicationItem(item: MedicationItem)

    @Delete
    abstract suspend fun deleteMedicationItem(item: MedicationItem)

    @Query("SELECT * FROM medication_items WHERE medicationItemId = :itemId")
    abstract suspend fun getMedicationItemById(itemId: Long): MedicationItem?

    @Query("SELECT * FROM medication_items WHERE prescriptionOwnerId = :prescriptionId")
    abstract suspend fun getMedicationItemsForPrescription(prescriptionId: Long): List<MedicationItem>

    @Query(
        "SELECT TRIM(medicationName) AS medicationName, COUNT(*) AS usageCount " +
            "FROM medication_items " +
            "WHERE TRIM(medicationName) != '' " +
            "GROUP BY TRIM(medicationName) " +
            "ORDER BY usageCount DESC, medicationName ASC"
    )
    abstract fun observeMedicationUsageStats(): Flow<List<MedicationUsageStat>>

    @Query("DELETE FROM medication_items WHERE prescriptionOwnerId = :prescriptionId")
    abstract suspend fun deleteMedicationItemsForPrescription(prescriptionId: Long)

    @Transaction
    open suspend fun insertFullConsultation(
        visit: Visit,
        prescription: Prescription,
        medicationItems: List<MedicationItem>
    ) {
        val visitId = insertVisit(visit)
        val prescriptionId = insertPrescription(prescription.copy(visitOwnerId = visitId))
        if (medicationItems.isNotEmpty()) {
            insertMedicationItems(
                medicationItems.map { item ->
                    item.copy(prescriptionOwnerId = prescriptionId)
                }
            )
        }
    }

    @Transaction
    open suspend fun updateFullConsultation(
        visit: Visit,
        prescription: Prescription,
        medicationItems: List<MedicationItem>
    ) {
        updateVisit(visit)
        val prescriptionId = if (prescription.prescriptionId == 0L) {
            insertPrescription(prescription)
        } else {
            updatePrescription(prescription)
            prescription.prescriptionId
        }
        deleteMedicationItemsForPrescription(prescriptionId)
        if (medicationItems.isNotEmpty()) {
            insertMedicationItems(
                medicationItems.map { item ->
                    item.copy(
                        medicationItemId = 0,
                        prescriptionOwnerId = prescriptionId
                    )
                }
            )
        }
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExperienceFormula(formula: ExperienceFormula): Long

    @Update
    abstract suspend fun updateExperienceFormula(formula: ExperienceFormula)

    @Delete
    abstract suspend fun deleteExperienceFormula(formula: ExperienceFormula)

    @Query("SELECT * FROM experience_formulas WHERE experienceFormulaId = :formulaId")
    abstract suspend fun getExperienceFormulaById(formulaId: Long): ExperienceFormula?

    @Query("SELECT * FROM experience_formulas ORDER BY experienceFormulaId DESC")
    abstract suspend fun getAllExperienceFormulas(): List<ExperienceFormula>

    @Transaction
    @Query("SELECT * FROM experience_formulas WHERE experienceFormulaId = :formulaId")
    abstract suspend fun getExperienceFormulaWithItems(formulaId: Long): ExperienceFormulaWithItems?

    @Transaction
    @Query("SELECT * FROM experience_formulas ORDER BY experienceFormulaId DESC")
    abstract suspend fun getAllExperienceFormulasWithItems(): List<ExperienceFormulaWithItems>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExperienceFormulaItem(item: ExperienceFormulaItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract suspend fun insertExperienceFormulaItems(items: List<ExperienceFormulaItem>): List<Long>

    @Update
    abstract suspend fun updateExperienceFormulaItem(item: ExperienceFormulaItem)

    @Delete
    abstract suspend fun deleteExperienceFormulaItem(item: ExperienceFormulaItem)

    @Query("SELECT * FROM experience_formula_items WHERE experienceFormulaItemId = :itemId")
    abstract suspend fun getExperienceFormulaItemById(itemId: Long): ExperienceFormulaItem?

    @Query("SELECT * FROM experience_formula_items WHERE experienceFormulaOwnerId = :formulaId")
    abstract suspend fun getExperienceFormulaItems(formulaId: Long): List<ExperienceFormulaItem>
}
