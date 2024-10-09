package nz.ac.canterbury.seng303.scrumboardmobile.screens.workLog

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.datetime.Instant
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.CreateWorkLogViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.WorkLogViewModel
import kotlinx.datetime.*
import nz.ac.canterbury.seng303.scrumboardmobile.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateWorkLogScreen(
    currentUserId: Int,
    navController: NavController,
    createWorkLogViewModel: CreateWorkLogViewModel,
    workLogViewModel: WorkLogViewModel,
    taskId: Int,
) {
    var showDatePicker by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = createWorkLogViewModel.description,
            onValueChange = { createWorkLogViewModel.updateDescription(it) },
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
                    value = createWorkLogViewModel.time.toString(),
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
                value = createWorkLogViewModel.workingHours,
                onValueChange = { newValue ->
                    if (newValue.isEmpty() || newValue.all { it.isDigit() }) {
                        createWorkLogViewModel.updateWorkingHours(newValue)
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
                val timeMillis = createWorkLogViewModel.time.atStartOfDayIn(TimeZone.currentSystemDefault()).toEpochMilliseconds()

                when {
                    createWorkLogViewModel.description.trim().isEmpty() -> {
                        Toast.makeText(context,
                            ContextCompat.getString(context, R.string.description_empty_message),
                            Toast.LENGTH_SHORT).show()
                    }
                    createWorkLogViewModel.workingHours.toIntOrNull()?.let { it <= 0 } ?: true -> {
                        Toast.makeText(context,
                            ContextCompat.getString(context, R.string.hours_invalid_message),
                            Toast.LENGTH_SHORT).show()
                    }
                    else -> {

                            workLogViewModel.createWorkLog(
                                userId = currentUserId,
                                taskId = taskId,
                                description = createWorkLogViewModel.description,
                                time = timeMillis,
                                workingHours = createWorkLogViewModel.workingHours.toIntOrNull() ?: 0,
                            )
                            createWorkLogViewModel.clearInputs()
                            navController.popBackStack()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(ContextCompat.getString(context, R.string.create_work_log_label))
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
                            createWorkLogViewModel.updateTime(localDate)
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
            createWorkLogViewModel.clearInputs()
        }
    }
}
