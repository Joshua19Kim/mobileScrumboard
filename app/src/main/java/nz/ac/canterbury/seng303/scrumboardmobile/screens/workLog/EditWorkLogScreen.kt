package nz.ac.canterbury.seng303.scrumboardmobile.screens.workLog

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.WorkLog
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.EditWorkLogViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.WorkLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditWorkLogScreen(
    workLogId: String,
    navController: NavController,
    editWorkLogViewModel: EditWorkLogViewModel,
    workLogViewModel: WorkLogViewModel,
) {
    workLogViewModel.getWorkLog(workLogId.toIntOrNull())
    val selectedWorkLogState by workLogViewModel.selectedWorkLog.collectAsState(null)
    val workLog: WorkLog? = selectedWorkLogState
    LaunchedEffect(workLog) {
        if (workLog != null) {
            editWorkLogViewModel.setDefaultValues(workLog)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = ContextCompat.getString(context, R.string.edit_work_log_label),
            style = MaterialTheme.typography.headlineMedium
        )
        OutlinedTextField(
            value = editWorkLogViewModel.description,
            onValueChange = { editWorkLogViewModel.updateDescription(it) },
            label = { Text(ContextCompat.getString(context, R.string.description)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            maxLines = 5
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.weight(1f)
            ) {
                OutlinedTextField(
                    value = editWorkLogViewModel.time.toString(),
                    onValueChange = { },
                    label = { Text(ContextCompat.getString(context, R.string.date)) },
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = ContextCompat.getString(context, R.string.select_date)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable { showDatePicker = true }
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = editWorkLogViewModel.workingHours,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        editWorkLogViewModel.updateWorkingHours(newValue)
                    }
                },
                label = { Text(ContextCompat.getString(context, R.string.hours)) },
                modifier = Modifier.weight(1f),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val timeMillis = editWorkLogViewModel.time.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                when {
                    editWorkLogViewModel.description.trim().isEmpty() -> {
                        Toast.makeText(context,
                            ContextCompat.getString(context, R.string.description_empty_message),
                            Toast.LENGTH_SHORT).show()
                    }
                    editWorkLogViewModel.workingHours.toIntOrNull()?.let { it <= 0 } ?: true -> {
                        Toast.makeText(context,
                            ContextCompat.getString(context, R.string.hours_invalid_message),
                            Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                        if (workLog != null) {
                            workLogViewModel.updateWorkLog(
                                workLog.copy(
                                    description = editWorkLogViewModel.description,
                                    time = timeMillis,
                                    workingHours = editWorkLogViewModel.workingHours.toIntOrNull()
                                        ?: 0,
                                )

                            )
                        }
                        editWorkLogViewModel.clearInputs()
                        navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(ContextCompat.getString(context, R.string.save_label))
        }

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(ContextCompat.getString(context, R.string.cancel_label))
        }
    }

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            val localDate = Instant.fromEpochMilliseconds(it)
                                .toLocalDateTime(TimeZone.currentSystemDefault())
                                .date
                            editWorkLogViewModel.updateTime(localDate)
                        }
                        showDatePicker = false
                    }
                ) {
                    Text(ContextCompat.getString(context, R.string.ok_label))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(ContextCompat.getString(context, R.string.cancel_label))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            editWorkLogViewModel.clearInputs()
        }
    }







}