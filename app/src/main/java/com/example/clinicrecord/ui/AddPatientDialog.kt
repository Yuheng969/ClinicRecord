package com.example.clinicrecord.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.clinicrecord.viewmodel.ClinicViewModel

@Composable
fun AddPatientDialog(
    viewModel: ClinicViewModel,
    initialFolderName: String,
    onDismiss: () -> Unit
) {
    val nextOptions = KeyboardOptions(imeAction = ImeAction.Next)
    val genderRequester = remember { FocusRequester() }
    val ageRequester = remember { FocusRequester() }
    val contactRequester = remember { FocusRequester() }
    val nativePlaceRequester = remember { FocusRequester() }
    val folderRequester = remember { FocusRequester() }
    val pastHistoryRequester = remember { FocusRequester() }
    var name by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("") }
    var ageText by remember { mutableStateOf("") }
    var contact by remember { mutableStateOf("") }
    var nativePlace by remember { mutableStateOf("") }
    var folderName by remember { mutableStateOf(initialFolderName) }
    var pastHistory by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = androidx.compose.material3.MaterialTheme.shapes.large,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "添加患者",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("姓名") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { genderRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .moveFocusOnEnter(genderRequester)
                )
                OutlinedTextField(
                    value = gender,
                    onValueChange = { gender = it },
                    label = { Text("性别") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { ageRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(genderRequester)
                        .moveFocusOnEnter(ageRequester)
                )
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter(Char::isDigit) },
                    label = { Text("年龄") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(onNext = { contactRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(ageRequester)
                        .moveFocusOnEnter(contactRequester)
                )
                OutlinedTextField(
                    value = contact,
                    onValueChange = { contact = it },
                    label = { Text("联系方式") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { nativePlaceRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(contactRequester)
                        .moveFocusOnEnter(nativePlaceRequester)
                )
                OutlinedTextField(
                    value = nativePlace,
                    onValueChange = { nativePlace = it },
                    label = { Text("籍贯") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { folderRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(nativePlaceRequester)
                        .moveFocusOnEnter(folderRequester)
                )
                OutlinedTextField(
                    value = folderName,
                    onValueChange = { folderName = it },
                    label = { Text("所在文件夹") },
                    singleLine = true,
                    keyboardOptions = nextOptions,
                    keyboardActions = KeyboardActions(onNext = { pastHistoryRequester.requestFocus() }),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(folderRequester)
                        .moveFocusOnEnter(pastHistoryRequester)
                )
                OutlinedTextField(
                    value = pastHistory,
                    onValueChange = { pastHistory = it },
                    label = { Text("既往史") },
                    minLines = 3,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(pastHistoryRequester)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("取消")
                    }
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                viewModel.addPatient(
                                    name = name,
                                    gender = gender,
                                    age = ageText.toIntOrNull() ?: 0,
                                    contact = contact,
                                    nativePlace = nativePlace,
                                    folderName = folderName,
                                    pastHistory = pastHistory
                                )
                                onDismiss()
                            }
                        }
                    ) {
                        Text("保存")
                    }
                }
            }
        }
    }
}

private fun Modifier.moveFocusOnEnter(nextRequester: FocusRequester): Modifier = onPreviewKeyEvent { event ->
    if (event.type == KeyEventType.KeyDown && event.key == Key.Enter) {
        nextRequester.requestFocus()
        true
    } else {
        false
    }
}
