package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.datetime.*
import kotlinx.datetime.TimeZone
import nz.ac.canterbury.seng303.scrumboardmobile.models.Story

class EditStoryViewModel: ViewModel() {

    var title by mutableStateOf("")
    fun updateTitle(newTitle: String) {
        title = newTitle
    }

    var description by mutableStateOf("")
    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    var dueAt by mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()))
    fun updateDueAt(newDueAt: LocalDateTime) {
        dueAt = newDueAt
    }

    fun clearFields() {
        title = ""
        description = ""
        dueAt = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault())
    }

    fun setDefaultValues(story: Story?) {
        story?.let {
            title = it.title
            description = it.description
            dueAt = Instant.fromEpochMilliseconds(it.dueAt).toLocalDateTime(TimeZone.currentSystemDefault())
        }
    }

}