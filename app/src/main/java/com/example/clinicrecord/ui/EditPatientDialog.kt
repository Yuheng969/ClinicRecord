package com.example.clinicrecord.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.clinicrecord.data.Patient
import com.example.clinicrecord.viewmodel.ClinicViewModel

@Composable
fun EditPatientDialog(
    patient: Patient,
    viewModel: ClinicViewModel,
    onDismiss: () -> Unit
) {
    var name by remember { mutableStateOf(patient.name) }
    var gender by remember { mutableStateOf(patient.gender) }
    var ageText by remember { mutableStateOf(patient.age.toString()) }
    var contact by remember { mutableStateOf(patient.contact) }
    var nativePlace by remember { mutableStateOf(patient.nativePlace) }
    var folderName by remember { mutableStateOf(patient.folderName) }
    var pastHistory by remember { mutableStateOf(patient.pastHistory) }

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
                    text = "编辑患者",
                    style = androidx.compose.material3.MaterialTheme.typography.titleLarge
                )
                OutlinedTextField(name, { name = it }, label = { Text("姓名") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(gender, { gender = it }, label = { Text("性别") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(
                    value = ageText,
                    onValueChange = { ageText = it.filter(Char::isDigit) },
                    label = { Text("年龄") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(contact, { contact = it }, label = { Text("联系方式") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(nativePlace, { nativePlace = it }, label = { Text("籍贯") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(folderName, { folderName = it }, label = { Text("所在文件夹") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(pastHistory, { pastHistory = it }, label = { Text("既往史") }, minLines = 3, modifier = Modifier.fillMaxWidth())
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
                                viewModel.updatePatientProfile(
                                    patient = patient,
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
