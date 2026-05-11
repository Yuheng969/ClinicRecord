package com.example.clinicrecord.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE patients ADD COLUMN folderName TEXT NOT NULL DEFAULT '未分类'"
        )
    }
}

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE TABLE IF NOT EXISTS clinic_folders (folderName TEXT NOT NULL, PRIMARY KEY(folderName))")
        db.execSQL(
            "INSERT OR IGNORE INTO clinic_folders(folderName) " +
                "SELECT DISTINCT folderName FROM patients " +
                "WHERE TRIM(folderName) != '' AND folderName != '未分类'"
        )
    }
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "CREATE TABLE IF NOT EXISTS deleted_records (" +
                "deletedRecordId INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "recordType TEXT NOT NULL, " +
                "title TEXT NOT NULL, " +
                "summary TEXT NOT NULL, " +
                "payload TEXT NOT NULL DEFAULT '', " +
                "deletedAt INTEGER NOT NULL" +
                ")"
        )
    }
}

val MIGRATION_4_5 = object : Migration(4, 5) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            "ALTER TABLE visits ADD COLUMN patentMedicineAcupuncture TEXT NOT NULL DEFAULT ''"
        )
    }
}
