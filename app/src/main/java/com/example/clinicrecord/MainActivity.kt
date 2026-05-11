package com.example.clinicrecord

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.room.Room
import com.example.clinicrecord.data.ClinicDatabase
import com.example.clinicrecord.data.MIGRATION_1_2
import com.example.clinicrecord.data.MIGRATION_2_3
import com.example.clinicrecord.data.MIGRATION_3_4
import com.example.clinicrecord.data.MIGRATION_4_5
import com.example.clinicrecord.navigation.ClinicNavGraph
import com.example.clinicrecord.ui.theme.AppColorStyle
import com.example.clinicrecord.ui.theme.ClinicRecordTheme
import com.example.clinicrecord.viewmodel.ClinicViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val database = Room.databaseBuilder(
            applicationContext,
            ClinicDatabase::class.java,
            "clinic_record.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5)
            .build()
        val preferences = getSharedPreferences("clinic_record_settings", MODE_PRIVATE)
        val initialStyle = runCatching {
            AppColorStyle.valueOf(
                preferences.getString("color_style", AppColorStyle.Sage.name) ?: AppColorStyle.Sage.name
            )
        }.getOrDefault(AppColorStyle.Sage)

        val viewModel = ViewModelProvider(
            this,
            ClinicViewModelFactory(database, initialStyle)
        )[ClinicViewModel::class.java]

        setContent {
            val colorStyle by viewModel.selectedColorStyle.collectAsState()
            LaunchedEffect(colorStyle) {
                preferences.edit().putString("color_style", colorStyle.name).apply()
            }
            ClinicRecordTheme(colorStyle = colorStyle) {
                ClinicNavGraph(viewModel = viewModel)
            }
        }
    }
}

private class ClinicViewModelFactory(
    private val database: ClinicDatabase,
    private val initialStyle: AppColorStyle
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClinicViewModel::class.java)) {
            return ClinicViewModel(database.clinicDao(), initialStyle) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
