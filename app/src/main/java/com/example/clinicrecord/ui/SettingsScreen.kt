package com.example.clinicrecord.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.clinicrecord.ui.theme.AppColorStyle
import com.example.clinicrecord.viewmodel.ClinicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: ClinicViewModel,
    onBack: () -> Unit
) {
    val selectedStyle by viewModel.selectedColorStyle.collectAsState()

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground,
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                ),
                title = { Text("设置") },
                navigationIcon = {
                    TextButton(onClick = onBack) {
                        Text("返回")
                    }
                }
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "风格颜色",
                    style = MaterialTheme.typography.titleLarge
                )
            }
            item {
                Text(
                    text = "选择一种接近示例图的柔和卡片配色。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            items(AppColorStyle.values()) { style ->
                ColorStyleCard(
                    style = style,
                    selected = selectedStyle == style,
                    onClick = { viewModel.selectColorStyle(style) }
                )
            }
        }
    }
}

@Composable
private fun ColorStyleCard(
    style: AppColorStyle,
    selected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (selected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = if (selected) 6.dp else 3.dp),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            LayeredSwatch(seed = style.seed)
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = style.title,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = style.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = if (selected) "已选" else "切换",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun LayeredSwatch(seed: Color) {
    Box(
        modifier = Modifier.size(58.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .size(48.dp)
                .align(Alignment.BottomEnd),
            shape = MaterialTheme.shapes.medium,
            color = seed.copy(alpha = 0.22f)
        ) {}
        Surface(
            modifier = Modifier
                .size(44.dp)
                .align(Alignment.TopStart),
            shape = MaterialTheme.shapes.medium,
            color = seed.copy(alpha = 0.42f)
        ) {}
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(CircleShape)
                .background(seed)
                .align(Alignment.Center)
        )
        Spacer(modifier = Modifier.size(1.dp))
    }
}
