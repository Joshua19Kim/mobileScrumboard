package nz.ac.canterbury.seng303.scrumboardmobile.screens.task

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.ScrumboardConstants

@Composable
fun CreateTaskScreen (
    navController: NavController,

    title: String,
    onTitleChange: (String) -> Unit,

    description: String,
    onDescriptionChange: (String) -> Unit,

    estimate: Int,
    onEstimateChange: (Int) -> Unit,

    selectedPriority: ScrumboardConstants.Priority,
    onPriorityChange: (ScrumboardConstants.Priority) -> Unit,

    selectedComplexity: ScrumboardConstants.Complexity,
    onComplexityChange: (ScrumboardConstants.Complexity) -> Unit,

    createTaskFn: (String, String, Int, ScrumboardConstants.Priority, ScrumboardConstants.Complexity) -> Unit,
    storyId: String
) {
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedComplexity by remember { mutableStateOf(false) }
    var integerValue by remember { mutableStateOf("") }
    val context = LocalContext.current
    val scrollState = rememberScrollState()


    val complexities = mapOf(
        Pair(ScrumboardConstants.Complexity.UNSET, context.getString(R.string.unset)),
        Pair(ScrumboardConstants.Complexity.LOW, context.getString(R.string.low_complexity)),
        Pair(ScrumboardConstants.Complexity.MEDIUM, context.getString(R.string.medium_complexity)),
        Pair(ScrumboardConstants.Complexity.HIGH, context.getString(R.string.high_complexity))
    )

    val priorities = mapOf(
        Pair(ScrumboardConstants.Priority.UNSET, context.getString(R.string.unset)),
        Pair(ScrumboardConstants.Priority.LOW, context.getString(R.string.low_priority)),
        Pair(ScrumboardConstants.Priority.NORMAL, context.getString(R.string.normal_priority)),
        Pair(ScrumboardConstants.Priority.HIGH, context.getString(R.string.high_priority)),
        Pair(ScrumboardConstants.Priority.CRITICAL, context.getString(R.string.critical_priority))
    )

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ContextCompat.getString(context, R.string.create_a_task_label))
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { onTitleChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.task_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = description,
                onValueChange = { onDescriptionChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.task_description)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            // Priority selection dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = priorities[selectedPriority]!!,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(ContextCompat.getString(context, R.string.task_priority)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedPriority = true },
                    trailingIcon = {
                        IconButton(onClick = { expandedPriority = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = ContextCompat.getString(context, R.string.task_priority))
                        }
                    }
                )

                DropdownMenu(
                    expanded = expandedPriority,
                    onDismissRequest = { expandedPriority = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Iterate through the enum values to create dropdown items
                    ScrumboardConstants.Priority.entries.forEach { priority ->
                        DropdownMenuItem(
                            text = {
                                Text(text = priorities[priority]!!)
                            },
                            onClick = {
                                onPriorityChange(priority)
                                expandedPriority = false
                            }
                        )
                    }
                }
            }

            // Complexity selection dropdown
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = complexities[selectedComplexity]!!,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text(ContextCompat.getString(context, R.string.task_complexity)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedComplexity = true },
                    trailingIcon = {
                        IconButton(onClick = { expandedComplexity = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = ContextCompat.getString(context, R.string.task_complexity))
                        }
                    }
                )

                DropdownMenu(
                    expanded = expandedComplexity,
                    onDismissRequest = { expandedComplexity = false },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Iterate through the enum values to create dropdown items
                    ScrumboardConstants.Complexity.entries.forEach { complexity ->
                        DropdownMenuItem(
                            text = {
                                Text(text = complexities[complexity]!!)
                            },
                            onClick = {
                                onComplexityChange(complexity)
                                expandedComplexity = false
                            }
                        )
                    }
                }
            }

            // Estimate
            OutlinedTextField(
                value = integerValue,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        integerValue = it
                    }

                    if (it.all { char -> char.isDigit()} && it != "") {
                        onEstimateChange(it.toInt())
                    }
                },
                label = { Text(ContextCompat.getString(context, R.string.task_estimate)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    when {
                        title.trim().isEmpty() -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.title_empty_message), Toast.LENGTH_SHORT).show()
                        }
                        description.trim().isEmpty() -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.description_empty_message), Toast.LENGTH_SHORT).show()
                        }
                        selectedPriority == ScrumboardConstants.Priority.UNSET -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.priority_unset_message), Toast.LENGTH_SHORT).show()
                        }
                        selectedComplexity == ScrumboardConstants.Complexity.UNSET -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.complexity_unset_message), Toast.LENGTH_SHORT).show()
                        }
                        integerValue == "" -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.estimate_empty_message), Toast.LENGTH_SHORT).show()
                        }
                        estimate <= 0 -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.estimate_negative_message), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            createTaskFn(title, description, estimate, selectedPriority, selectedComplexity)
                            onTitleChange("")
                            onDescriptionChange("")
                            onPriorityChange(ScrumboardConstants.Priority.UNSET)
                            onComplexityChange(ScrumboardConstants.Complexity.UNSET)
                            navController.navigate("Story/$storyId")
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(ContextCompat.getString(context, R.string.create_task_label))
            }
        }
    }
}