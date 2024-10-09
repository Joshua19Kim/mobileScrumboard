package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.models.ScrumboardConstants
import nz.ac.canterbury.seng303.scrumboardmobile.models.TaskWithWorkLogs

class EditTaskViewModel: ViewModel() {
    var taskTitle by mutableStateOf("")
    fun updateTaskTitle(newTitle: String) {
        taskTitle = newTitle
    }

    var taskDescription by mutableStateOf("")
    fun updateTaskDescription(newDescription: String) {
        taskDescription = newDescription
    }

    var status by mutableStateOf(ScrumboardConstants.Status.TO_DO)
    fun updateStatus(newStatus: ScrumboardConstants.Status) {
        status = newStatus
    }

    var estimate by mutableStateOf("")
    fun updateEstimate(newEstimate: String) {
        estimate = newEstimate
    }

    var priority by mutableStateOf(ScrumboardConstants.Priority.UNSET)
    fun updatePriority(newPriority: ScrumboardConstants.Priority) {
        priority = newPriority
    }

    var complexity by mutableStateOf(ScrumboardConstants.Complexity.UNSET)
    fun updateComplexity(newComplexity: ScrumboardConstants.Complexity) {
        complexity = newComplexity
    }

    var assignedTo by mutableStateOf<Int?>(null)
    fun updateAssignedTo(newAssignedTo: Int?) {
        assignedTo = newAssignedTo
    }

    var assignedToString by mutableStateOf<String?>(null)
    fun updateAssignedToString(newAssignedToString: String?) {
        assignedToString = newAssignedToString
    }

    var reviewerId by mutableStateOf<Int?>(null)
    fun updateReviewerId(newReviewerId: Int?) {
        reviewerId = newReviewerId
    }

    var reviewerString by mutableStateOf<String?>(null)
    fun updateReviewerString(newReviewerString: String?) {
        reviewerString = newReviewerString
    }

    fun setTaskProperties(taskWithWorkLogs: TaskWithWorkLogs) {
        val task = taskWithWorkLogs.task
        taskTitle = task.title
        taskDescription = task.description
        status = task.status
        estimate = task.estimate.toString()
        priority = task.priority
        complexity = task.complexity
        assignedTo = task.assignedTo
        reviewerId = task.reviewerId
    }

}