package nz.ac.canterbury.seng303.scrumboardmobile

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import androidx.room.Room
import nz.ac.canterbury.seng303.scrumboardmobile.datastore.Database
import nz.ac.canterbury.seng303.scrumboardmobile.notification.channelId
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story.StoryViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.TaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.WorkLogViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext.startKoin
import org.koin.dsl.module

class MainApplication : Application() {


    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        startKoin {
            androidLogger()
            androidContext(this@MainApplication)
            modules(appModule)
        }
    }

    private fun createNotificationChannel() {
        val descriptionText = getString(R.string.notification_title)
        val mChannel = NotificationChannel(
            channelId,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_HIGH
        )
        mChannel.description = descriptionText
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }
}

val appModule = module {
    single {
        Room.databaseBuilder(
            androidContext(),
            Database::class.java,
            "app_database"
        ).build()
    }

    single { get<Database>().storyDao() }
    single { get<Database>().taskDao() }
    single { get<Database>().workLogDao() }
    single { get<Database>().userDao() }

    // ViewModels
    viewModel { UserViewModel(get()) }
    viewModel { StoryViewModel(get(), androidContext()) }
    viewModel { TaskViewModel(get()) }
    viewModel { WorkLogViewModel(get()) }

}