package nz.ac.canterbury.seng303.scrumboardmobile.screens.story

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.Story
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story.StoryViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditStoryScreen(
    navController: NavController,
    storyViewModel: StoryViewModel,
    storyId: String,
    title: String,
    onTitleChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    dateTime: LocalDateTime,
    onDateTimeChange: (LocalDateTime) -> Unit,
    clearFields: () -> Unit,
    populateFields: (Story?) -> Unit
) {
    storyViewModel.getStory(storyId.toIntOrNull())
    val selectedStoryState by storyViewModel.selectedStory.collectAsState(null)
    val story: Story? = selectedStoryState
    LaunchedEffect(story) {
        if (story != null) {
            populateFields(story)
        }
    }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    val timeCreated = remember { mutableLongStateOf(0L) }

    val context = LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = ContextCompat.getString(context, R.string.edit_story_label),
            style = MaterialTheme.typography.headlineLarge
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { onTitleChange(it) },
                label = { Text(text = ContextCompat.getString(context, R.string.story_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { onDescriptionChange(it) },
                label = { Text(text = ContextCompat.getString(context, R.string.story_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
            Divider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = ContextCompat.getString(context, R.string.story_datetime_helper_message),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = dateTime.date.toString(),
                        onValueChange = {  },
                        label = { Text(text = ContextCompat.getString(context, R.string.story_due_date)) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = ContextCompat.getString(context, R.string.story_due_date)
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
                Box(modifier = Modifier.weight(1f)) {
                    OutlinedTextField(
                        value = "%02d:%02d".format(dateTime.hour, dateTime.minute),
                        onValueChange = {  },
                        label = { Text(text = ContextCompat.getString(context, R.string.story_due_time)) },
                        readOnly = true,
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = ContextCompat.getString(context, R.string.story_due_time)
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable { showTimePicker = true }
                    )
                }
            }

            Button(
                onClick = {
                    timeCreated.longValue = System.currentTimeMillis()
                    if (story != null) {
                        storyViewModel.updateStory(
                            Story(
                                storyId = storyId.toInt(),
                                title = title,
                                description = description,
                                dueAt = dateTime.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                                timeCreated = story.timeCreated

                            )
                        )
                    }
                    navController.navigate("Story/$storyId")

                    clearFields()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(text = ContextCompat.getString(context, R.string.save_label))
            }
        }

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(initialSelectedDateMillis = dateTime.toInstant(
                TimeZone.currentSystemDefault()).toEpochMilliseconds())
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let {
                                val newDate = Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.currentSystemDefault()).date
                                onDateTimeChange(LocalDateTime(newDate, dateTime.time))
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text(text = ContextCompat.getString(context, R.string.ok_label))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text(text = ContextCompat.getString(context, R.string.cancel_label))
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }

        if (showTimePicker) {
            val timePickerState = rememberTimePickerState(
                initialHour = dateTime.hour,
                initialMinute = dateTime.minute
            )
            AlertDialog(
                onDismissRequest = { showTimePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val newTime = LocalTime(timePickerState.hour, timePickerState.minute)
                            onDateTimeChange(LocalDateTime(dateTime.date, newTime))
                            showTimePicker = false
                        }
                    ) {
                        Text(text = ContextCompat.getString(context, R.string.ok_label))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showTimePicker = false }) {
                        Text(text = ContextCompat.getString(context, R.string.cancel_label))
                    }
                },
                text = {
                    TimePicker(state = timePickerState)
                }
            )
        }
    }
}