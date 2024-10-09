package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ViewTaskViewModel: ViewModel() {

    var assignedToString by mutableStateOf<String?>(null)
    fun updateAssignedToString(newAssignedToString: String?) {
        assignedToString = newAssignedToString
    }

    var reviewerString by mutableStateOf<String?>(null)
    fun updateReviewerString(newReviewerString: String?) {
        reviewerString = newReviewerString
    }
}