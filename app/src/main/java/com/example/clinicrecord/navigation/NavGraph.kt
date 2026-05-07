package com.example.clinicrecord.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.Canvas
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.clinicrecord.ui.NewVisitScreen
import com.example.clinicrecord.ui.PatientDetailScreen
import com.example.clinicrecord.ui.PatientListScreen
import com.example.clinicrecord.ui.ProfileScreen
import com.example.clinicrecord.ui.SettingsScreen
import com.example.clinicrecord.ui.TrashScreen
import com.example.clinicrecord.ui.VisitDetailScreen
import com.example.clinicrecord.viewmodel.ClinicViewModel

object ClinicRoute {
    const val PatientList = "patient_list"
    const val PatientDetail = "patient_detail"
    const val NewVisit = "new_visit"
    const val VisitDetail = "visit_detail"
    const val Profile = "profile"
    const val Trash = "trash"
    const val Settings = "settings"
    const val PatientIdArg = "patientId"
    const val VisitIdArg = "visitId"

    fun patientDetail(patientId: Long): String {
        return "$PatientDetail/$patientId"
    }

    fun newVisit(patientId: Long): String {
        return "$NewVisit/$patientId"
    }

    fun visitDetail(visitId: Long): String {
        return "$VisitDetail/$visitId"
    }
}

@Composable
fun ClinicNavGraph(
    viewModel: ClinicViewModel
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = ClinicRoute.PatientList
    ) {
        composable(route = ClinicRoute.PatientList) {
            MainTabScaffold(
                selectedRoute = ClinicRoute.PatientList,
                onTabClick = { route ->
                    navController.navigate(route) {
                        popUpTo(ClinicRoute.PatientList) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                PatientListScreen(
                    viewModel = viewModel,
                    onPatientClick = { patientId ->
                        navController.navigate(ClinicRoute.patientDetail(patientId))
                    }
                )
            }
        }

        composable(route = ClinicRoute.Profile) {
            MainTabScaffold(
                selectedRoute = ClinicRoute.Profile,
                onTabClick = { route ->
                    navController.navigate(route) {
                        popUpTo(ClinicRoute.PatientList) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            ) {
                ProfileScreen(
                    viewModel = viewModel,
                    onTrashClick = { navController.navigate(ClinicRoute.Trash) },
                    onSettingsClick = { navController.navigate(ClinicRoute.Settings) }
                )
            }
        }

        composable(route = ClinicRoute.Trash) {
            TrashScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(route = ClinicRoute.Settings) {
            SettingsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = "${ClinicRoute.PatientDetail}/{${ClinicRoute.PatientIdArg}}",
            arguments = listOf(
                navArgument(ClinicRoute.PatientIdArg) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong(ClinicRoute.PatientIdArg) ?: return@composable
            PatientDetailScreen(
                patientId = patientId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onNewVisit = { id ->
                    navController.navigate(ClinicRoute.newVisit(id))
                },
                onVisitClick = { visitId ->
                    navController.navigate(ClinicRoute.visitDetail(visitId))
                }
            )
        }

        composable(
            route = "${ClinicRoute.NewVisit}/{${ClinicRoute.PatientIdArg}}",
            arguments = listOf(
                navArgument(ClinicRoute.PatientIdArg) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val patientId = backStackEntry.arguments?.getLong(ClinicRoute.PatientIdArg) ?: return@composable
            NewVisitScreen(
                patientId = patientId,
                viewModel = viewModel,
                onCancel = { navController.popBackStack() },
                onSaved = { navController.popBackStack() }
            )
        }

        composable(
            route = "${ClinicRoute.VisitDetail}/{${ClinicRoute.VisitIdArg}}",
            arguments = listOf(
                navArgument(ClinicRoute.VisitIdArg) {
                    type = NavType.LongType
                }
            )
        ) { backStackEntry ->
            val visitId = backStackEntry.arguments?.getLong(ClinicRoute.VisitIdArg) ?: return@composable
            VisitDetailScreen(
                visitId = visitId,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun MainTabScaffold(
    selectedRoute: String,
    onTabClick: (String) -> Unit,
    content: @Composable () -> Unit
) {
    Scaffold(
        contentWindowInsets = WindowInsets(0.dp),
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(82.dp),
                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                tonalElevation = 3.dp
            ) {
                NavigationBarItem(
                    selected = selectedRoute == ClinicRoute.PatientList,
                    onClick = { onTabClick(ClinicRoute.PatientList) },
                    icon = { NotebookTabIcon(selected = selectedRoute == ClinicRoute.PatientList) },
                    label = { Text("笔记") }
                )
                NavigationBarItem(
                    selected = selectedRoute == ClinicRoute.Profile,
                    onClick = { onTabClick(ClinicRoute.Profile) },
                    icon = { ProfileDotIcon(selected = selectedRoute == ClinicRoute.Profile) },
                    label = { Text("我的") }
                )
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Composable
private fun NotebookTabIcon(selected: Boolean) {
    val accent = if (selected) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    Canvas(modifier = Modifier.size(24.dp)) {
        val stroke = Stroke(width = 2.1.dp.toPx(), cap = StrokeCap.Round)
        drawRoundRect(
            color = accent,
            topLeft = Offset(5.dp.toPx(), 3.5.dp.toPx()),
            size = Size(14.dp.toPx(), 17.dp.toPx()),
            cornerRadius = CornerRadius(2.5.dp.toPx(), 2.5.dp.toPx()),
            style = stroke
        )
        drawLine(
            color = accent,
            start = Offset(8.dp.toPx(), 7.dp.toPx()),
            end = Offset(16.dp.toPx(), 7.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent,
            start = Offset(8.dp.toPx(), 11.dp.toPx()),
            end = Offset(14.dp.toPx(), 11.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent.copy(alpha = 0.55f),
            start = Offset(5.dp.toPx(), 5.5.dp.toPx()),
            end = Offset(2.5.dp.toPx(), 5.5.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = accent.copy(alpha = 0.55f),
            start = Offset(5.dp.toPx(), 10.5.dp.toPx()),
            end = Offset(2.5.dp.toPx(), 10.5.dp.toPx()),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
private fun ProfileDotIcon(selected: Boolean) {
    val color = if (selected) MaterialTheme.colorScheme.primary else Color(0xFF566173)
    Canvas(modifier = Modifier.size(24.dp)) {
        drawCircle(
            color = color,
            radius = 8.5.dp.toPx(),
            center = center
        )
    }
}
