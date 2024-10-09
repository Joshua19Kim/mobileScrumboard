package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.PrimaryKey
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import nz.ac.canterbury.seng303.scrumboardmobile.dao.WorkLogDao
import nz.ac.canterbury.seng303.scrumboardmobile.models.Story
import nz.ac.canterbury.seng303.scrumboardmobile.models.WorkLog

class EditWorkLogViewModel : ViewModel() {
    var description by mutableStateOf("")
        private set

    var time by mutableStateOf(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date)
        private set

    var workingHours by mutableStateOf("")
        private set

    fun updateDescription(newDescription: String) {
        description = newDescription
    }

    fun updateTime(newTime: LocalDate) {
        time = newTime
    }

    fun updateWorkingHours(newWorkingHours: String) {
        workingHours = newWorkingHours
    }

    fun clearInputs() {
        description = ""
        time = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        workingHours = ""
    }

    fun setDefaultValues(workLog: WorkLog?) {
        workLog?.let {
            description = it.description
            time = Instant.fromEpochMilliseconds(it.time)
                .toLocalDateTime(TimeZone.currentSystemDefault()).date
            workingHours = it.workingHours.toString()
        }
    }
}