package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM
import android.util.Log
import androidx.core.content.ContextCompat.startActivity
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.dao.StoryDao
import nz.ac.canterbury.seng303.scrumboardmobile.models.Story
import nz.ac.canterbury.seng303.scrumboardmobile.models.StoryWithTasks
import nz.ac.canterbury.seng303.scrumboardmobile.notification.NotificationReceiver

class StoryViewModel (private val storyDao: StoryDao, private val context: Context): ViewModel() {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    private val _stories = MutableStateFlow<List<Story>>(emptyList())
    val stories: StateFlow<List<Story>> get() = _stories

    private val _selectedStoryWithTasks = MutableStateFlow<StoryWithTasks?>(null)
    val selectedStoryWithTasks: StateFlow<StoryWithTasks?> = _selectedStoryWithTasks

    private val _selectedStory = MutableStateFlow<Story?>(null)
    val selectedStory: StateFlow<Story?> = _selectedStory

    private val _storiesWithTasks = MutableStateFlow<List<StoryWithTasks>>(emptyList())
    val storiesWithTasks: StateFlow<List<StoryWithTasks>> get() = _storiesWithTasks

    fun getStoriesWithTasks() {
        viewModelScope.launch {
            storyDao.getAllStories().collect { stories ->
                val tasksFlows = stories.map { story ->
                    storyDao.getStoryWithTasks(story.storyId)
                }
                combine(tasksFlows) { storyWithTasksList ->
                    storyWithTasksList.toList()
                }.collect { combinedList ->
                    _storiesWithTasks.value = combinedList
                }
            }
        }
    }

    fun getStories() = viewModelScope.launch {
        storyDao.getAllStoriesWithTasks().catch { Log.e("STORY_VIEW_MODEL", it.toString()) }
            .collect {_storiesWithTasks.emit(it) }
    }

    fun createStory(title: String,
                    description: String,
                    timeCreated: Long,
                    dueAt: LocalDateTime
    ) = viewModelScope.launch {
        val story = Story(
            title = title,
            description = description,
            timeCreated = timeCreated,
            dueAt = dueAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds()
        )
        try {
            val storyId = storyDao.insertStory(story)
            Log.d("STORY_VIEW_MODEL", "Story has been created with id: $storyId")
            if (dueAt.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds() >= System.currentTimeMillis()){
                scheduleNotifications(context.getString(R.string.story_due_message, title), dueAt)

            }
        } catch (e: Exception) {
            Log.e("STORY_VIEW_MODEL", "Could not insert Story", e)
        }
    }

    fun getStoryWithTasks(storyId: Int?) = viewModelScope.launch {
        if (storyId != null) {
            _selectedStoryWithTasks.value = storyDao.getStoryWithTasks(storyId).first()
        } else {
            _selectedStoryWithTasks.value = null
        }
    }

    fun getStory(storyId: Int?) = viewModelScope.launch {
        if (storyId != null) {
            _selectedStory.value = storyDao.getStory(storyId).first()
        } else {
            _selectedStory.value = null
        }
    }

    fun updateStory(story: Story) = viewModelScope.launch {

        try {
            storyDao.updateStory(story)
            Log.d("STORY_VIEW_MODEL", "Story has been updated with id: ${story.storyId}")
            if (story.dueAt >= System.currentTimeMillis()){
                scheduleNotifications(
                    context.getString(R.string.story_due_message, story.title),
                    Instant.fromEpochMilliseconds(story.dueAt)
                        .toLocalDateTime(TimeZone.currentSystemDefault())
                )

            }
        } catch (e: Exception) {
            Log.e("STORY_VIEW_MODEL", "Could not update Story", e)
        }
    }

    private fun scheduleNotifications(message: String, scheduleDate: LocalDateTime) {
        val requestId = System.currentTimeMillis().toInt()
        val intent = Intent(context.applicationContext, NotificationReceiver::class.java).apply {
            val bundle = bundleOf(
                Pair("notificationTitle", context.getString(R.string.notification_title)),
                Pair("notificationDescription", message)
            )
            putExtra(Intent.EXTRA_TEXT, bundle)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context.applicationContext,
            requestId,
            intent,
            PendingIntent.FLAG_MUTABLE
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when {
                alarmManager.canScheduleExactAlarms() -> {
                    Log.d("STORY_VIEW_MODEL", "Scheduling notification for $scheduleDate")
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        scheduleDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                        pendingIntent
                    )
                }
            }
        } else {
            Log.d("STORY_VIEW_MODEL", "Scheduling notification for $scheduleDate")
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                scheduleDate.toInstant(TimeZone.currentSystemDefault()).toEpochMilliseconds(),
                pendingIntent
            )
        }


    }


}

