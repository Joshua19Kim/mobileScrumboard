package nz.ac.canterbury.seng303.scrumboardmobile.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import nz.ac.canterbury.seng303.scrumboardmobile.models.Story
import nz.ac.canterbury.seng303.scrumboardmobile.models.StoryWithTasks


@Dao
interface StoryDao {
    @Insert
    suspend fun insertStory(vararg story: Story)
    @Update
    suspend fun updateStory(vararg story: Story)
    @Delete
    suspend fun deleteStory(vararg story: Story)
    @Query("SELECT * FROM STORY")
    fun getAllStories(): Flow<List<Story>>
    @Transaction
    @Query("SELECT * FROM Story WHERE storyId = :storyId")
    fun getStoryWithTasks(storyId: Int): Flow<StoryWithTasks>
    @Query("SELECT * FROM Story WHERE storyId = :storyId")
    fun getStory(storyId: Int): Flow<Story>
    @Transaction
    @Query("SELECT * FROM Story")
    fun getAllStoriesWithTasks(): Flow<List<StoryWithTasks>>
}