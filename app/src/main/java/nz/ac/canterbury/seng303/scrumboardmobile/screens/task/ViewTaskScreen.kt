package nz.ac.canterbury.seng303.scrumboardmobile.screens.task

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.ScrumboardConstants
import nz.ac.canterbury.seng303.scrumboardmobile.models.TaskWithWorkLogs
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.util.convertTimestampToReadableDate
import nz.ac.canterbury.seng303.scrumboardmobile.util.convertTimestampToReadableTime
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.TaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
@ExperimentalMaterial3Api
fun ViewTaskScreen(
    navController: NavController,
    taskViewModel: TaskViewModel,
    userViewModel: UserViewModel,
    storyId: String,
    taskId: String
) {
    taskViewModel.getTaskWithWorkLogs(
        storyId = storyId.toIntOrNull(),
        taskId = taskId.toIntOrNull()
    )
    val selectedTaskState by taskViewModel.selectedTaskWithWorkLogs.collectAsState(null)
    val taskWithWorkLogs: TaskWithWorkLogs? = selectedTaskState
    if (taskWithWorkLogs != null) {
        taskViewModel.setTaskProperties(taskWithWorkLogs)
    }
    userViewModel.getUsers()
    val users: List<User> by userViewModel.users.collectAsState(emptyList())

    var isTitleFocused by remember { mutableStateOf(false) }
    var isDescriptionFocused by remember { mutableStateOf(false) }


    val coroutineScope = rememberCoroutineScope()
    var expandedStatus by remember { mutableStateOf(false) }
    var expandedPriority by remember { mutableStateOf(false) }
    var expandedComplexity by remember { mutableStateOf(false) }

    var expandedAssignee by remember { mutableStateOf(false) }
    var expandedReviewer by remember { mutableStateOf(false) }

    var assignedStatus by remember { mutableStateOf<String?>(null) }
    var assignedTo by remember { mutableStateOf<String?>(null) }
    var reviewer by remember { mutableStateOf<String?>(null) }
    val usernames = remember { mutableStateOf(mutableMapOf<Int, String>()) }

    LaunchedEffect(taskWithWorkLogs) {
        taskWithWorkLogs?.workLogs?.forEach { workLog ->
            coroutineScope.launch {
                val username = userViewModel.getUserById(workLog.userId)?.username ?: "Unknown User"
                usernames.value = usernames.value.toMutableMap().apply { put(workLog.userId, username) }
            }
        }
    }

    val scrollState = rememberScrollState()

    var titleChanged by remember { mutableStateOf(false) }
    var descriptionChanged by remember { mutableStateOf(false) }
    var statusChanged by remember { mutableStateOf(false) }
    var estimateChanged by remember { mutableStateOf(false) }
    var priorityChanged by remember { mutableStateOf(false) }
    var complexityChanged by remember { mutableStateOf(false) }

    val context = LocalContext.current


    LaunchedEffect(taskWithWorkLogs, users) {
        taskWithWorkLogs?.let {
            assignedStatus = it.task.status.status
            assignedTo = it.task.assignedTo?.let { userId ->
                findUserWithId(users, userId)?.firstName
            } ?: ""
            reviewer = it.task.reviewerId?.let { userId ->
                findUserWithId(users, userId)?.firstName
            } ?: ""
        }
    }

    if (taskWithWorkLogs == null || users.isEmpty()) {
        // Display a loading state until the data is ready
        CircularProgressIndicator()
    } else {
        Scaffold(
            floatingActionButton = {
                ExtendedCreateWorkLogFab(
                    navController = navController,
                    storyId = storyId,
                    taskId = taskId
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                // Title
                OutlinedTextField(
                    value = taskViewModel.taskTitle,
                    onValueChange = { newTitle ->
                        taskViewModel.updateTaskTittle(newTitle)
                        titleChanged = taskViewModel.selectedTaskWithWorkLogs.value?.task?.title != newTitle
                    },
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                        .onFocusChanged { focusState ->
                            isTitleFocused = focusState.isFocused
                        },
                    colors = TextFieldDefaults.colors(
                        focusedIndicatorColor = if (isTitleFocused) MaterialTheme.colorScheme.primary else Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.headlineSmall
                )
                ElevatedCard(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .padding(8.dp)
                            .border(
                                BorderStroke(
                                    width = 2.dp,
                                    color = if (isDescriptionFocused) MaterialTheme.colorScheme.primary else Color.Transparent
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .wrapContentHeight()
                    ) {
                        TextField(
                            value = taskViewModel.taskDescription,
                            onValueChange = { newDescription ->
                                taskViewModel.updateTaskDescription(newDescription)
                                descriptionChanged = taskViewModel.selectedTaskWithWorkLogs.value?.task?.description != newDescription

                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .onFocusChanged { focusState ->
                                    isDescriptionFocused = focusState.isFocused
                                },
                            colors = TextFieldDefaults.colors(
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent,
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent
                            ),
                            textStyle = MaterialTheme.typography.bodyMedium,
                        )
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ){
                        Box(
                            modifier = Modifier
                                .fillMaxWidth(),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            OutlinedTextField(
                                value = taskViewModel.status.status,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .padding(0.dp)
                                    .wrapContentWidth()
                                    .widthIn(max = 150.dp)
                                    .clickable { expandedStatus = true }
                                    .align(Alignment.CenterEnd),
                                trailingIcon = {
                                    IconButton(onClick = { expandedStatus = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Task Priority")
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expandedStatus,
                                onDismissRequest = { expandedStatus = false },
                                offset = DpOffset(x = 180.dp, y = 0.dp),
                                modifier = Modifier.width(150.dp)
                            ) {
                                ScrumboardConstants.Status.entries.forEach { status ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = status.status)
                                        },
                                        onClick = {
                                            taskViewModel.updateStatus(status)
                                            statusChanged = taskViewModel.selectedTaskWithWorkLogs.value?.task?.status != status
                                            expandedStatus = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Column {
                            Text(text = ContextCompat.getString(context, R.string.priority))
                        }
                        Spacer(modifier = Modifier.width(160.dp))
                        Column {
                            OutlinedTextField(
                                value = taskViewModel.priority.priority,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .padding(0.dp)
                                    .wrapContentWidth()
                                    .widthIn(max = 150.dp)
                                    .clickable { expandedPriority = true },
                                trailingIcon = {
                                    IconButton(onClick = { expandedPriority = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Task Priority")
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expandedPriority,
                                onDismissRequest = { expandedPriority = false }
                            ) {
                                ScrumboardConstants.Priority.entries.forEach { priority ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = priority.priority)
                                        },
                                        onClick = {
                                            taskViewModel.updatePriority(priority)
                                            priorityChanged = taskViewModel.selectedTaskWithWorkLogs.value?.task?.priority != priority
                                            expandedPriority = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Column {
                            Text(text = ContextCompat.getString(context, R.string.complexity))
                        }
                        Spacer(modifier = Modifier.width(130.dp))
                        Column {
                            OutlinedTextField(
                                value = taskViewModel.complexity.complexity,
                                onValueChange = {},
                                readOnly = true,
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .padding(0.dp)
                                    .wrapContentWidth()
                                    .widthIn(max = 150.dp)
                                    .clickable { expandedComplexity = true },
                                trailingIcon = {
                                    IconButton(onClick = { expandedComplexity = true }) {
                                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Task Priority")
                                    }
                                }
                            )
                            DropdownMenu(
                                expanded = expandedComplexity,
                                onDismissRequest = { expandedComplexity = false }
                            ) {
                                ScrumboardConstants.Complexity.entries.forEach { complexity ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(text = complexity.complexity)
                                        },
                                        onClick = {
                                            taskViewModel.updateComplexity(complexity)
                                            complexityChanged = taskViewModel.selectedTaskWithWorkLogs.value?.task?.complexity != complexity
                                            expandedComplexity = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    ) {
                        Column {
                            Text(text = ContextCompat.getString(context, R.string.estimate))
                        }
                        Spacer(modifier = Modifier.width(130.dp))
                        Column {
                            OutlinedTextField(
                                value = taskViewModel.estimate,
                                onValueChange = { newEstimate ->
                                    taskViewModel.updateEstimate(newEstimate)
                                    estimateChanged = taskViewModel.selectedTaskWithWorkLogs.value?.task?.estimate.toString() != newEstimate
                                },
                                keyboardOptions = KeyboardOptions.Default.copy(
                                    keyboardType = KeyboardType.Number
                                ),
                                textStyle = TextStyle(fontSize = 12.sp),
                                modifier = Modifier
                                    .padding(0.dp)
                                    .wrapContentWidth()
                            )
                        }
                    }
                    if (titleChanged || descriptionChanged || statusChanged ||
                        estimateChanged || priorityChanged || complexityChanged) {
                        Row(
                            modifier = Modifier
                                .padding(8.dp)
                                .fillMaxWidth()
                        ) {
                            Button(
                                onClick = {
                                    when {
                                        taskViewModel.taskTitle.trim().isEmpty() -> {
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.title_empty_message), Toast.LENGTH_SHORT).show()
                                        }
                                        taskViewModel.taskDescription.trim().isEmpty() -> {
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.description_empty_message), Toast.LENGTH_SHORT).show()
                                        }
                                        taskViewModel.priority == ScrumboardConstants.Priority.UNSET -> {
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.priority_unset_message), Toast.LENGTH_SHORT).show()
                                        }
                                        taskViewModel.complexity == ScrumboardConstants.Complexity.UNSET -> {
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.complexity_unset_message), Toast.LENGTH_SHORT).show()
                                        }
                                        taskViewModel.estimate == "" -> {
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.estimate_empty_message), Toast.LENGTH_SHORT).show()
                                        }
                                        taskViewModel.estimate.toInt() <= 0 -> {
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.estimate_negative_message), Toast.LENGTH_SHORT).show()
                                        }
                                        else -> {
                                            taskViewModel.updateTaskFromState()
                                            Toast.makeText(context, ContextCompat.getString(context, R.string.task_updated), Toast.LENGTH_SHORT).show()
                                            titleChanged = false
                                            descriptionChanged = false
                                            statusChanged = false
                                            estimateChanged = false
                                            priorityChanged = false
                                            complexityChanged = false
                                        }
                                    }
                                }) {
                                Text(text = (ContextCompat.getString(context, R.string.save_label)))
                            }
                        }
                    }
                    Row (
                        modifier = Modifier
                            .padding(
                                start = 4.dp,
                                top = 4.dp,
                                end = 4.dp,
                                bottom = 16.dp
                            ),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column (
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = assignedTo.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    label = { Text(ContextCompat.getString(context, R.string.assignee)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(0.dp)
                                        .clickable { expandedAssignee = true },
                                    trailingIcon = {
                                        IconButton(onClick = { expandedAssignee = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Assignee")
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = expandedAssignee,
                                    onDismissRequest = { expandedAssignee = false }
                                ) {
                                    // Iterate through the user values to create dropdown items
                                    users.forEach { user ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = user.firstName)
                                            },
                                            onClick = {
                                                assignedTo = user.firstName
                                                val updatedTask = taskWithWorkLogs.task.copy(assignedTo = user.userId) // Create a copy of the task with updated status
                                                taskViewModel.updateTask(updatedTask)
                                                expandedAssignee = false
                                            }
                                        )
                                    }
                                }
                            }
                        }
                        Column (
                            Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 8.dp)
                                    .fillMaxWidth()
                            ) {
                                OutlinedTextField(
                                    value = reviewer.toString(),
                                    onValueChange = {},
                                    readOnly = true,
                                    textStyle = TextStyle(fontSize = 12.sp),
                                    label = { Text(ContextCompat.getString(context, R.string.reviewer)) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(0.dp)
                                        .clickable { expandedReviewer = true },
                                    trailingIcon = {
                                        IconButton(onClick = { expandedReviewer = true }) {
                                            Icon(Icons.Default.ArrowDropDown, contentDescription = "Reviewer")
                                        }
                                    }
                                )
                                DropdownMenu(
                                    expanded = expandedReviewer,
                                    onDismissRequest = { expandedReviewer = false }
                                ) {
                                    // Iterate through the user values to create dropdown items
                                    users.forEach { user ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(text = user.firstName)
                                            },
                                            onClick = {
                                                expandedReviewer = false
                                                reviewer = user.firstName
                                                val updatedTask2 = taskWithWorkLogs.task.copy(reviewerId = user.userId)
                                                taskViewModel.updateTask(updatedTask2)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Display work logs
                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = ContextCompat.getString(context, R.string.worklogs),
                    style = MaterialTheme.typography.headlineMedium
                )
                taskWithWorkLogs.workLogs.forEach { workLog ->
                    ElevatedCard(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                // Date in the top-left
                                Text(
                                    text =
                                        convertTimestampToReadableDate(workLog.time),
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.align(Alignment.CenterVertically)
                                )

                                // Hours in a chip at the top-right
                                AssistChip(
                                    onClick = {},
                                    label = {
                                        Text("${workLog.workingHours}h")
                                    },
                                    modifier = Modifier.align(Alignment.CenterVertically),
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.primary, // Use primary theme color
                                        labelColor = MaterialTheme.colorScheme.onPrimary
                                    )
                                )
                            }


                            // Description
                            Text(workLog.description)
                            Divider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(text = "${ContextCompat.getString(context, R.string.work_log_added_by)} ${usernames.value[workLog.userId] ?: ContextCompat.getString(context, R.string.work_log_added_by_loading)}")
                                IconButton(
                                    onClick = {navController.navigate("Story/$storyId/Task/$taskId/WorkLog/${workLog.workLogId}/edit")}
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit"
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }
}

@Composable
fun ExtendedCreateWorkLogFab(
    navController: NavController,
    storyId: String,
    taskId: String
) {
    ExtendedFloatingActionButton(
        onClick = { navController.navigate("Story/$storyId/Task/$taskId/CreateWorkLog") },
        text = { Text(text = "Add Work Log") },
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = "Add WorkLog") }
    )
}

fun findUserWithId(
    users: List<User>,
    userId: Int
): User? {
    return users.find { it.userId == userId }
}