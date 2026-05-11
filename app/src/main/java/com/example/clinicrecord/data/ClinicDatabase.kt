package com.example.clinicrecord.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        Patient::class,
        ClinicFolder::class,
        DeletedRecord::class,
        Visit::class,
        Prescription::class,
        MedicationItem::class,
        ExperienceFormula::class,
        ExperienceFormulaItem::class
    ],
    version = 5,
    exportSchema = false
)
abstract class ClinicDatabase : RoomDatabase() {
    abstract fun clinicDao(): ClinicDao
}
