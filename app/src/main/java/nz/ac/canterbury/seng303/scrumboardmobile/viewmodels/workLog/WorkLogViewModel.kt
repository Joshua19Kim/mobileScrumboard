package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.dao.WorkLogDao
import nz.ac.canterbury.seng303.scrumboardmobile.models.WorkLog

class WorkLogViewModel(private val workLogDao: WorkLogDao) : ViewModel() {
    private val _selectedWorkLog = MutableStateFlow<WorkLog?>(null)
    val selectedWorkLog: StateFlow<WorkLog?> = _selectedWorkLog
    fun createWorkLog(
                userId: Int,
                taskId: Int,
                description: String,
                time: Long,
                workingHours: Int,
    ) {
        viewModelScope.launch {
            val workLog = WorkLog(0, userId, taskId, description, time, workingHours)
            workLogDao.insertWorkLog(workLog)
        }
    }

    fun getWorkLog(
        workLogId: Int?
    ) {
        viewModelScope.launch {
            if (workLogId != null) {
                _selectedWorkLog.value = workLogDao.getWorkLog(workLogId).first()
            } else {
                _selectedWorkLog.value = null
            }
        }
    }

    fun updateWorkLog(
        workLog: WorkLog
    ) {
        viewModelScope.launch {
            workLogDao.updateWorkLog(workLog)
        }
    }

}