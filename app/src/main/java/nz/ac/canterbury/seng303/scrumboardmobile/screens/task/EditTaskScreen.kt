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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.ScrumboardConstants
import nz.ac.canterbury.seng303.scrumboardmobile.models.TaskWithWorkLogs
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.util.findUserWithId
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.EditTaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.TaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel

@Composable
fun EditTaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    userViewModel: UserViewModel,
    editTaskViewModel: EditTaskViewModel
) {
    val selectedTaskState by taskViewModel.selectedTaskWithWorkLogs.collectAsState(null)
    val taskWithWorkLogs: TaskWithWorkLogs? = selectedTaskState

    userViewModel.getUsers()
    val users: List<User> by userViewModel.users.collectAsState(emptyList())
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    var usernames = remember { mutableMapOf<Int, String>() }
    val context = LocalContext.current

    val statuses = mapOf(
        Pair(ScrumboardConstants.Status.TO_DO, context.getString(R.string.todo)),
        Pair(ScrumboardConstants.Status.IN_PROGRESS, context.getString(R.string.in_progress)),
        Pair(ScrumboardConstants.Status.UNDER_REVIEW, context.getString(R.string.in_review)),
        Pair(ScrumboardConstants.Status.DONE, context.getString(R.string.done))
    )

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
        Pair(ScrumboardConstants.Priority.CRITICAL, context.getString(R.string.high_priority))
    )

    LaunchedEffect(taskWithWorkLogs) {
        if (taskWithWorkLogs != null) {
            editTaskViewModel.setTaskProperties(taskWithWorkLogs)
        }
        taskWithWorkLogs?.workLogs?.forEach { workLog ->
            coroutineScope.launch {
                val username = userViewModel.getUserById(workLog.userId)?.username ?: context.getString(R.string.unknown_user)
                usernames = usernames.apply { put(workLog.userId, username) }
            }
        }

    }

    var expandedPriority by remember { mutableStateOf(false) }
    var expandedComplexity by remember { mutableStateOf(false) }
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedAssignee by remember { mutableStateOf(false) }
    var expandedReviewer by remember { mutableStateOf(false) }


    LaunchedEffect(taskWithWorkLogs, users) {
        taskWithWorkLogs?.let {
            it.task.assignedTo?.let { userId ->
                editTaskViewModel.updateAssignedToString(findUserWithId(users, userId)?.firstName)
            } ?: ""
            it.task.reviewerId?.let { userId ->
                editTaskViewModel.updateReviewerString(findUserWithId(users, userId)?.firstName)
            } ?: ""
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState)
        ,
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ContextCompat.getString(context, R.string.edit_task_label))
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = editTaskViewModel.taskTitle,
                onValueChange = { editTaskViewModel.updateTaskTitle(it) },
                label = { Text(ContextCompat.getString(context, R.string.task_title)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = editTaskViewModel.taskDescription,
                onValueChange = { editTaskViewModel.updateTaskDescription(it) },
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
                    value = priorities[editTaskViewModel.priority]!!,
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
                                editTaskViewModel.updatePriority(priority)
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
                    value = complexities[editTaskViewModel.complexity]!!,
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
                                editTaskViewModel.updateComplexity(complexity)
                                expandedComplexity = false
                            }
                        )
                    }
                }
            }

            // Estimate
            OutlinedTextField(
                value = editTaskViewModel.estimate,
                onValueChange = {
                    if (it.all { char -> char.isDigit() } || it.isEmpty()) {
                        editTaskViewModel.updateEstimate(it)
                    }

                    if (it.all { char -> char.isDigit()} && it != "") {
                        editTaskViewModel.updateEstimate(it)
                    }
                },
                label = { Text(ContextCompat.getString(context, R.string.task_estimate)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)

            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = statuses[editTaskViewModel.status]!!,
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(fontSize = 12.sp),
                    label = { Text(context.getString(R.string.task_status)) },
                    modifier = Modifier
                        .padding(vertical = 8.dp)
                        .fillMaxWidth()
                        .clickable { expandedStatus = true },
                    trailingIcon = {
                        IconButton(onClick = { expandedStatus = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = context.getString(R.string.task_status))
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedStatus,
                    onDismissRequest = { expandedStatus = false },
                    offset = DpOffset(x = 180.dp, y = 0.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ScrumboardConstants.Status.entries.forEach { status ->
                        DropdownMenuItem(
                            text = {
                                Text(text = statuses[status]!!)
                            },
                            onClick = {
                                editTaskViewModel.updateStatus(status)
                                expandedStatus = false
                            }
                        )
                    }
                }
            }

            Divider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = editTaskViewModel.assignedToString ?: "",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(fontSize = 12.sp),
                    label = { Text(ContextCompat.getString(context, R.string.assignee)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { expandedAssignee = true },
                    trailingIcon = {
                        IconButton(onClick = { expandedAssignee = true }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = context.getString(R.string.assignee)
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedAssignee,
                    onDismissRequest = { expandedAssignee = false },
                    modifier = Modifier.fillMaxWidth()

                ) {
                    // Iterate through the user values to create dropdown items
                    users.forEach { user ->
                        DropdownMenuItem(
                            text = {
                                Text(text = user.firstName)
                            },
                            onClick = {
                                editTaskViewModel.updateAssignedTo(user.userId)
                                editTaskViewModel.updateAssignedToString(user.firstName)
                                expandedAssignee = false
                            }
                        )
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                OutlinedTextField(
                    value = editTaskViewModel.reviewerString ?: "",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = TextStyle(fontSize = 12.sp),
                    label = { Text(ContextCompat.getString(context, R.string.reviewer)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                        .clickable { expandedReviewer = true },
                    trailingIcon = {
                        IconButton(onClick = { expandedReviewer = true }) {
                            Icon(
                                Icons.Default.ArrowDropDown,
                                contentDescription = context.getString(R.string.reviewer)
                            )
                        }
                    }
                )
                DropdownMenu(
                    expanded = expandedReviewer,
                    onDismissRequest = { expandedReviewer = false },
                    modifier = Modifier.fillMaxWidth()

                ) {
                    // Iterate through the user values to create dropdown items
                    users.forEach { user ->
                        DropdownMenuItem(
                            text = {
                                Text(text = user.firstName)
                            },
                            onClick = {
                                editTaskViewModel.updateReviewerId(user.userId)
                                editTaskViewModel.updateReviewerString(user.firstName)
                                expandedReviewer = false
                            }
                        )
                    }
                }
            }
            Button(
                onClick = {
                    when {
                        editTaskViewModel.taskTitle.trim().isEmpty() -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.title_empty_message), Toast.LENGTH_SHORT).show()
                        }
                        editTaskViewModel.taskDescription.trim().isEmpty() -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.description_empty_message), Toast.LENGTH_SHORT).show()
                        }
                        editTaskViewModel.priority == ScrumboardConstants.Priority.UNSET -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.priority_unset_message), Toast.LENGTH_SHORT).show()
                        }
                        editTaskViewModel.complexity == ScrumboardConstants.Complexity.UNSET -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.complexity_unset_message), Toast.LENGTH_SHORT).show()
                        }
                        editTaskViewModel.estimate == "" -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.estimate_empty_message), Toast.LENGTH_SHORT).show()
                        }
                        editTaskViewModel.estimate.toInt() <= 0 -> {
                            Toast.makeText(context, ContextCompat.getString(context, R.string.estimate_negative_message), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            if (taskWithWorkLogs != null) {
                                taskViewModel.updateTask(
                                    taskWithWorkLogs.task.copy(
                                        title = editTaskViewModel.taskTitle,
                                        description = editTaskViewModel.taskDescription,
                                        complexity = editTaskViewModel.complexity,
                                        priority = editTaskViewModel.priority,
                                        status = editTaskViewModel.status,
                                        assignedTo = editTaskViewModel.assignedTo,
                                        reviewerId = editTaskViewModel.reviewerId,
                                        estimate = editTaskViewModel.estimate.toInt()

                                    )
                                )
                            }
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(ContextCompat.getString(context, R.string.save_label))
            }
        }
    }
}
