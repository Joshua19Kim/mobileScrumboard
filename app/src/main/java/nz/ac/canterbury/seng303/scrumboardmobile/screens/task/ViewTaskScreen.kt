package nz.ac.canterbury.seng303.scrumboardmobile.screens.task

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.ScrumboardConstants
import nz.ac.canterbury.seng303.scrumboardmobile.models.TaskWithWorkLogs
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.models.WorkLog
import nz.ac.canterbury.seng303.scrumboardmobile.util.convertTimestampToReadableDate
import nz.ac.canterbury.seng303.scrumboardmobile.util.findUserWithId
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.TaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.ViewTaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel

@Composable
@ExperimentalMaterial3Api
fun ViewTaskScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    taskViewModel: TaskViewModel,
    viewTaskViewModel: ViewTaskViewModel,
    storyId: String,
    taskId: String
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var usernames = remember { mutableMapOf<Int, String>() }

    taskViewModel.getTaskWithWorkLogs(
        storyId = storyId.toIntOrNull(),
        taskId = taskId.toIntOrNull()
    )

    val selectedTaskState by taskViewModel.selectedTaskWithWorkLogs.collectAsState(null)
    val taskWithWorkLogs: TaskWithWorkLogs? = selectedTaskState
    userViewModel.getUsers()
    val users: List<User> by userViewModel.users.collectAsState(emptyList())
    LaunchedEffect(taskWithWorkLogs) {
        taskWithWorkLogs?.workLogs?.forEach { workLog ->
            coroutineScope.launch {
                val username = userViewModel.getUserById(workLog.userId)?.username ?: ContextCompat.getString(context, R.string.unknown_user)
                usernames = usernames.apply { put(workLog.userId, username) }
            }
        }
    }
    LaunchedEffect(taskWithWorkLogs, users) {
        taskWithWorkLogs?.let {
            it.task.assignedTo?.let { userId ->
                viewTaskViewModel.updateAssignedToString(findUserWithId(users, userId)?.firstName)
            } ?: ""
            it.task.reviewerId?.let { userId ->
                viewTaskViewModel.updateReviewerString(findUserWithId(users, userId)?.firstName)
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
                    taskId = taskId,
                    context = context
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
            ) {
                TaskCard(
                    navController = navController,
                    taskWithWorkLogs = taskWithWorkLogs,
                    context = context,
                    assignedTo = viewTaskViewModel.assignedToString,
                    reviewer = viewTaskViewModel.reviewerString
                )
                WorkLogList(
                    navController = navController,
                    workLogs = taskWithWorkLogs.workLogs,
                    storyId = storyId,
                    taskId = taskId,
                    context = context,
                    usernames = usernames
                )
            }
        }
    }
}

@Composable
fun TaskCard(
    navController: NavController,
    taskWithWorkLogs: TaskWithWorkLogs,
    context: Context,
    assignedTo: String?,
    reviewer: String?
) {
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
        Pair(ScrumboardConstants.Priority.CRITICAL, context.getString(R.string.critical_priority))
    )
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = taskWithWorkLogs.task.title,
                    style = MaterialTheme.typography.headlineSmall
                )
                IconButton(onClick = {
                    navController.navigate("Story/${taskWithWorkLogs.task.storyId}/Task/${taskWithWorkLogs.task.taskId}/edit")
                }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = ContextCompat.getString(context, R.string.edit_label)
                    )
                }
            }

            Text(
                text = taskWithWorkLogs.task.description,
                style = MaterialTheme.typography.bodyLarge
            )
            Divider(
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = statuses[taskWithWorkLogs.task.status]!!,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ContextCompat.getString(context, R.string.complexity)}: ${complexities[taskWithWorkLogs.task.complexity]}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${ContextCompat.getString(context, R.string.priority)}: ${priorities[taskWithWorkLogs.task.priority]}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = context.getString(R.string.estimated_hours, taskWithWorkLogs.task.estimate),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold
                )

            }
            Divider(
                modifier = Modifier.padding(vertical = 8.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = context.getString(R.string.assignee))
                    Text(
                        text = assignedTo ?: context.getString(R.string.no_one)
                    )

                }
                Column {
                    Text(text = context.getString(R.string.reviewer))
                    Text(
                        text = reviewer ?: context.getString(R.string.no_one)
                    )
                }
            }



        }
    }
}


@Composable
fun ExtendedCreateWorkLogFab(
    navController: NavController,
    storyId: String,
    taskId: String,
    context: Context
) {
    ExtendedFloatingActionButton(
        onClick = { navController.navigate("Story/$storyId/Task/$taskId/CreateWorkLog") },
        text = { Text(text = ContextCompat.getString(context, R.string.create_work_log_label)) },
        icon = { Icon(imageVector = Icons.Default.Add, contentDescription = ContextCompat.getString(context, R.string.create_work_log_label)) }
    )
}

@Composable
fun WorkLogList(
    navController: NavController,
    workLogs: List<WorkLog>,
    storyId: String,
    taskId: String,
    context: Context,
    usernames: Map<Int, String>
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Divider(modifier = Modifier.padding(8.dp))
        Row(
            modifier = Modifier.padding(8.dp)
        ) {
            Text(
                text = ContextCompat.getString(context, R.string.worklogs),
                style = MaterialTheme.typography.headlineSmall,
            )
        }
        if (workLogs.isEmpty()) {
            Text(
                text = ContextCompat.getString(context, R.string.no_work_logs_message),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            workLogs.forEach { workLog ->
                WorkLogCard(
                    navController = navController,
                    workLog = workLog,
                    storyId = storyId,
                    taskId = taskId,
                    context = context,
                    usernames = usernames
                )
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

    }
}

@Composable
fun WorkLogCard(
    navController: NavController,
    workLog: WorkLog,
    storyId: String,
    taskId: String,
    context: Context,
    usernames: Map<Int, String>
) {
    ElevatedCard(
        modifier = Modifier
            .padding(8.dp)
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
                        Text(context.getString(R.string.num_hours, workLog.workingHours))
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
                Text(text = "${ContextCompat.getString(context, R.string.work_log_added_by)} ${usernames[workLog.userId] ?: ContextCompat.getString(context, R.string.work_log_added_by_loading)}")
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
