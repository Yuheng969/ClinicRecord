package com.example.clinicrecord.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val DecoctionMethodOptions = listOf(
    "先煎",
    "后下",
    "包煎",
    "打碎",
    "冲服",
    "烊化",
    "另煎"
)

@Composable
internal fun DecoctionMethodDropdown(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val shape = RoundedCornerShape(10.dp)
    val outlineColor = MaterialTheme.colorScheme.outlineVariant

    Box(modifier = modifier.height(64.dp)) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .offset(y = 8.dp)
                .clip(shape)
                .border(
                    BorderStroke(1.dp, outlineColor),
                    shape
                )
                .clickable { expanded = true }
                .padding(horizontal = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = value,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                fontSize = 16.sp,
                color = outlineColor
            )
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("空白") },
                onClick = {
                    onValueChange("")
                    expanded = false
                }
            )
            DecoctionMethodOptions.forEach { method ->
                DropdownMenuItem(
                    text = { Text(method) },
                    onClick = {
                        onValueChange(method)
                        expanded = false
                    }
                )
            }
        }
    }
}
