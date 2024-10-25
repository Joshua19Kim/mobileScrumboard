package nz.ac.canterbury.seng303.scrumboardmobile

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.screens.story.CreateStoryScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.story.EditStoryScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.story.ViewAllStories
import nz.ac.canterbury.seng303.scrumboardmobile.screens.story.ViewStoryScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.task.CreateTaskScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.task.EditTaskScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.task.ViewTaskScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.user.EditUserScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.user.LoginUserScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.user.RegisterUserScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.user.UserPreferenceScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.user.UserProfileScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.workLog.CreateWorkLogScreen
import nz.ac.canterbury.seng303.scrumboardmobile.screens.workLog.EditWorkLogScreen
import nz.ac.canterbury.seng303.scrumboardmobile.ui.theme.ScrumBoardTheme
import nz.ac.canterbury.seng303.scrumboardmobile.util.hashPassword
import nz.ac.canterbury.seng303.scrumboardmobile.util.setLocale
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.common.AppBarViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story.CreateStoryViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story.EditStoryViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.story.StoryViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.CreateTaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.EditTaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.TaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.task.ViewTaskViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.CreateUserViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserEditViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserLoginModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.CreateWorkLogViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.EditWorkLogViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.workLog.WorkLogViewModel
import java.util.Locale
import org.koin.androidx.viewmodel.ext.android.viewModel as koinViewModel


class MainActivity : ComponentActivity() {
    private val userViewModel: UserViewModel by koinViewModel()
    private val storyViewModel: StoryViewModel by koinViewModel()
    private val taskViewModel: TaskViewModel by koinViewModel()
    private val workLogViewModel: WorkLogViewModel by koinViewModel()

    private val AUTHENTICATION = booleanPreferencesKey("authentication")
    private val CURRENT_USER = intPreferencesKey("userId")
    private val LANGUAGE_KEY = stringPreferencesKey("app_language")
    private val DARKMODE_KEY = booleanPreferencesKey("darkMode")

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

    @RequiresApi(Build.VERSION_CODES.S)
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isAuth: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[AUTHENTICATION] ?: false
            }

        val currentUserId: Flow<Int> = dataStore.data
            .map { preferences ->
                preferences[CURRENT_USER] ?: -1
            }
        val selectedLanguage: Flow<String> = dataStore.data
            .map { preferences ->
                preferences[LANGUAGE_KEY] ?: Locale.getDefault().language
            }
        val isDarkMode: Flow<Boolean> = dataStore.data
            .map { preferences ->
                preferences[DARKMODE_KEY] ?: true
            }

        suspend fun editCurrentUser(userId:Int) {
            dataStore.edit { settings ->
                settings[CURRENT_USER] = userId
            }
        }

        suspend fun grantAuthentication() {
            dataStore.edit { settings ->
                settings[AUTHENTICATION] = true
            }
        }

        suspend fun removeAuthentication() {
            dataStore.edit { settings ->
                settings[AUTHENTICATION] = false
            }
        }

        suspend fun setLanguage(language: String) {
            dataStore.edit { settings ->
                settings[LANGUAGE_KEY] = language
            }
        }

        suspend fun setDarkModeState(newDarkModeState: Boolean) {
            dataStore.edit { settings ->
                settings[DARKMODE_KEY] = newDarkModeState
            }
        }
        lifecycleScope.launch {
            selectedLanguage.collect { languageCode ->
                setLocale(this@MainActivity, languageCode)
            }
        }

        setContent {
            val isDarkModeState by isDarkMode.collectAsState(initial = true)
            ScrumBoardTheme(darkTheme = isDarkModeState) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val appBarViewModel: AppBarViewModel = viewModel()
                val isAuthenticated by isAuth.collectAsState(initial = false)
                appBarViewModel.init()
                Scaffold(
                    bottomBar = {
                        if (isAuthenticated) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = {
                                    navController.navigate("AllStories") {
                                        popUpTo(0) { inclusive = true }
                                    }
                                }) {
                                    Icon(Icons.Default.Home, contentDescription = "Home", modifier = Modifier.size(48.dp))
                                }
                                IconButton(onClick = {
                                    navController.navigate("CreateStory")
                                }) {
                                    Icon(Icons.Default.Add, contentDescription = "Create Story", modifier = Modifier.size(48.dp))
                                }
                                IconButton(onClick = {
                                    navController.navigate("Profile")
                                }) {
                                    Icon(Icons.Default.Person, contentDescription = "Profile", modifier = Modifier.size(48.dp))
                                }
                            }
                        }
                    },
                    topBar = {
                        // Add your AppBar content here
                        TopAppBar(
                            title = {
                                navBackStackEntry?.destination?.route?.let { route ->
                                    appBarViewModel.getNameById(route, applicationContext)?.run {
                                        Text(this)
                                    }
                                }
                            },
                            navigationIcon = {
                                if (navBackStackEntry?.destination?.route != "AllStories" && navBackStackEntry?.destination?.route != "Home") {
                                    IconButton(onClick = { navController.popBackStack() }) {
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = "Back"
                                        )
                                    }
                                }
                            }
                        )

                    }

                ) {
                    Box(modifier = Modifier.padding(it)) {
                        val createUserViewModel: CreateUserViewModel = viewModel()
                        val createStoryViewModel: CreateStoryViewModel = viewModel()
                        val editStoryViewModel: EditStoryViewModel = viewModel()

                        val createTaskViewModel: CreateTaskViewModel = viewModel()
                        val editTaskViewModel: EditTaskViewModel = viewModel()
                        val viewTaskViewModel: ViewTaskViewModel = viewModel()

                        val createWorkLogViewModel: CreateWorkLogViewModel = viewModel()
                        val editWorkLogViewModel: EditWorkLogViewModel = viewModel()

                        val userLoginModel: UserLoginModel = viewModel()
                        val userEditViewModel: UserEditViewModel = viewModel()

                        NavHost(navController = navController, startDestination = "Home") {
                            composable("Home") {
                                Home(navController = navController,
                                    isAuth = isAuth
                            ) }
                            composable("Register") {
                                RegisterUserScreen(
                                    userViewModel = userViewModel,
                                    navController = navController,
                                    username = createUserViewModel.username,
                                    onUsernameChange = { newUsername ->
                                        createUserViewModel.updateUsername(newUsername)
                                    },
                                    userEmail  = createUserViewModel.email,
                                    onUserEmailChange = { newEmail ->
                                        createUserViewModel.updateEmail(newEmail)
                                    },
                                    password = createUserViewModel.password,
                                    onPasswordChange = { newPassword ->
                                        createUserViewModel.updatePassword(newPassword)
                                    },
                                    firstName = createUserViewModel.firstName,
                                    onFirstNameChange = { newFirstName ->
                                        createUserViewModel.updateFirstName(newFirstName)
                                    },
                                    lastName = createUserViewModel.lastName,
                                    onLastNameChange = { newLastName ->
                                        createUserViewModel.updateLastName(newLastName)
                                    },
                                    createUserFn = { username, email, password, firstName, lastName ->
                                        userViewModel.createUser(
                                            username,
                                            email,
                                            hashPassword(password),
                                            firstName,
                                            lastName
                                        )
                                    },
                                    grantAuthentication = { grantAuthentication() },
                                    editCurrentUser = {userId -> editCurrentUser(userId)}
                                )
                            }
                            composable("Login") {
                                LoginUserScreen(
                                    userViewModel = userViewModel,
                                    navController = navController,
                                    username = userLoginModel.loginUsername,
                                    onUsernameChange = { newName ->
                                        userLoginModel.updateUsername(newName)
                                    },
                                    password = userLoginModel.loginPassword,
                                    onPasswordChange = { newPassword ->
                                        userLoginModel.updatePassword(newPassword)
                                    },
                                    grantAuthentication = { grantAuthentication() },
                                    editCurrentUser = {userId -> editCurrentUser(userId)}
                                )
                            }

                            composable("AllStories") {
                                ViewAllStories(navController = navController, storyViewModel = storyViewModel)
                            }

                            composable("CreateStory") {
                                CreateStoryScreen(
                                    navController = navController,
                                    title = createStoryViewModel.title,
                                    onTitleChange = { newTitle ->
                                        createStoryViewModel.updateTitle(newTitle)
                                    },
                                    description = createStoryViewModel.description,
                                    onDescriptionChange = { newDescription ->
                                        createStoryViewModel.updateDescription(newDescription)
                                    },
                                    dateTime = createStoryViewModel.dueAt,
                                    onDateTimeChange = { newDate -> createStoryViewModel.updateDueAt(newDate)},

                                    createStoryFn = { title, description, timeCreated, date  ->
                                        storyViewModel.createStory(
                                            title,
                                            description,
                                            timeCreated,
                                            date
                                        )
                                    },
                                    clearFields = { createStoryViewModel.clearFields() }
                                )
                            }

                            composable("Story/{storyId}/edit", arguments = listOf(navArgument("storyId") {
                                type = NavType.StringType
                            })) { backStackEntry ->
                            val storyId = backStackEntry.arguments?.getString("storyId")
                            storyId?.let { storyIdParam: String ->
                                EditStoryScreen(
                                    navController = navController,
                                    storyViewModel = storyViewModel,
                                    storyId = storyIdParam,
                                    title = editStoryViewModel.title,
                                    onTitleChange = { newTitle ->
                                        editStoryViewModel.updateTitle(newTitle)
                                    },
                                    description = editStoryViewModel.description,
                                    onDescriptionChange = { newDescription ->
                                        editStoryViewModel.updateDescription(newDescription)
                                    },
                                    dateTime = editStoryViewModel.dueAt,
                                    onDateTimeChange = { newDate -> editStoryViewModel.updateDueAt(newDate)},

                                    clearFields = { editStoryViewModel.clearFields() },
                                    populateFields = { story -> editStoryViewModel.setDefaultValues(story)}
                                )
                            }

                        }


                            composable("Story/{storyId}", arguments = listOf(navArgument("storyId") {
                                type = NavType.StringType
                            })) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getString("storyId")
                                storyId?.let { storyIdParam: String ->
                                    ViewStoryScreen(navController = navController, storyId = storyIdParam, storyViewModel = storyViewModel)
                                }

                            }

                            composable(
                                route = "Story/{storyId}/CreateTask",
                                arguments = listOf(navArgument("storyId") { type = NavType.StringType })
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getString("storyId")

                                storyId?.let { storyIdParam: String ->
                                    CreateTaskScreen(
                                        navController = navController,
                                        title = createTaskViewModel.title,
                                        onTitleChange = { newTitle ->
                                            createTaskViewModel.updateTitle(newTitle)
                                        },
                                        description = createTaskViewModel.description,
                                        onDescriptionChange = { newDescription ->
                                            createTaskViewModel.updateDescription(newDescription)
                                        },
                                        estimate = createTaskViewModel.estimate,
                                        onEstimateChange = { newEstimate ->
                                            createTaskViewModel.updateEstimate(newEstimate)
                                        },
                                        selectedPriority = createTaskViewModel.priority,
                                        onPriorityChange = { newPriority ->
                                            createTaskViewModel.updatePriority(newPriority)
                                        },
                                        selectedComplexity = createTaskViewModel.complexity,
                                        onComplexityChange = { newComplexity ->
                                            createTaskViewModel.updateComplexity(newComplexity)
                                        },
                                        createTaskFn = { title, description, estimate, selectedPriority, selectedComplexity ->
                                            taskViewModel.createTask(
                                                title,
                                                description,
                                                selectedComplexity,
                                                selectedPriority,
                                                estimate,
                                                storyIdParam.toInt()
                                            )
                                        },
                                        storyId = storyId
                                    )
                                }
                            }
                            composable(
                                "Story/{storyId}/Task/{taskId}",
                                arguments = listOf(
                                    navArgument("storyId") { type = NavType.StringType },
                                    navArgument("taskId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getString("storyId")
                                val taskId = backStackEntry.arguments?.getString("taskId")

                                if (storyId != null && taskId != null) {
                                    ViewTaskScreen(
                                        navController = navController,
                                        taskViewModel = taskViewModel,
                                        userViewModel = userViewModel,
                                        viewTaskViewModel = viewTaskViewModel,
                                        storyId = storyId,
                                        taskId = taskId
                                    )
                                }
                            }

                            composable(
                                "Story/{storyId}/Task/{taskId}/edit",
                                arguments = listOf(
                                    navArgument("storyId") { type = NavType.StringType },
                                    navArgument("taskId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getString("storyId")

                                val taskId = backStackEntry.arguments?.getString("taskId")
                                val currentUserIdState by currentUserId.collectAsState(initial = -1)

                                if (storyId != null && taskId != null) {
                                    EditTaskScreen(
                                        navController = navController,
                                        taskViewModel = taskViewModel,
                                        userViewModel = userViewModel,
                                        editTaskViewModel = editTaskViewModel,
                                        currentUserId = currentUserIdState
                                    )
                                }
                            }

                            composable(
                                "Story/{storyId}/Task/{taskId}/CreateWorkLog",
                                arguments = listOf(
                                    navArgument("storyId") { type = NavType.StringType },
                                    navArgument("taskId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val storyId = backStackEntry.arguments?.getString("storyId")
                                val taskId = backStackEntry.arguments?.getString("taskId")
                                val currentUserIdState by currentUserId.collectAsState(initial = -1)

                                if (storyId != null && taskId != null) {
                                    CreateWorkLogScreen(
                                        currentUserId = currentUserIdState,
                                        navController = navController,
                                        createWorkLogViewModel = createWorkLogViewModel,
                                        workLogViewModel = workLogViewModel,
                                        taskId = taskId.toInt()
                                    )
                                }
                            }

                            composable(
                                "Story/{storyId}/Task/{taskId}/WorkLog/{workLogId}/edit",
                                arguments = listOf(
                                    navArgument("storyId") { type = NavType.StringType },
                                    navArgument("taskId") { type = NavType.StringType },
                                    navArgument("workLogId") { type = NavType.StringType }
                                )
                            ) { backStackEntry ->
                                val workLogId = backStackEntry.arguments?.getString("workLogId")

                                if (workLogId != null) {
                                    EditWorkLogScreen(
                                        workLogId = workLogId,
                                        navController = navController,
                                        editWorkLogViewModel = editWorkLogViewModel,
                                        workLogViewModel = workLogViewModel,
                                    )
                                }
                            }
                            composable("Profile") {
                                val currentUserIdState by currentUserId.collectAsState(initial = -1)
                                UserProfileScreen(navController = navController,
                                    userViewModel = userViewModel,
                                    currentUserId = currentUserIdState,
                                    removeAuthenticationFn = { removeAuthentication()},
                                    editCurrentUser = {userId -> editCurrentUser(userId)}
                                )
                            }
                            composable("EditUser") {
                                val currentUserIdState by currentUserId.collectAsState(initial = -1)
                                EditUserScreen(
                                    navController = navController,
                                    userViewModel = userViewModel,
                                    userEditViewModel = userEditViewModel,
                                    currentUserId = currentUserIdState
                                )
                            }
                            composable("Preference") {
                                UserPreferenceScreen(
                                    navController = navController,
                                    language = selectedLanguage,
                                    isDarkMode = isDarkModeState,
                                    onLanguageChangeFn = { selectedLanguage ->
                                        lifecycleScope.launch {
                                            setLanguage(selectedLanguage)
                                        }
                                    },
                                    onDarkModeChangeFn = { newDarkModeState ->
                                        lifecycleScope.launch {
                                            setDarkModeState(newDarkModeState)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
fun Home(navController: NavController,
         isAuth: Flow<Boolean>
         ) {
    val isAuthenticated by isAuth.collectAsState(initial = false)
    val context = LocalContext.current
    NotificationPermissionHandler(context = context)
    BackHandler {
        //Disable the swipe right to go back gesture
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ContextCompat.getString(context, R.string.welcome))
        if (!isAuthenticated) {
            Button(onClick = { navController.navigate("Register") }) {
                Text(ContextCompat.getString(context, R.string.register_label))
            }

            Button(onClick = { navController.navigate("Login") }) {
                Text(ContextCompat.getString(context, R.string.login_label))
            }
        } else {
            navController.navigate("AllStories")
        }
    }
}


@Composable
fun NotificationPermissionHandler(context: Context) {
    var hasNotificationPermission by remember {
        mutableStateOf(checkNotificationPermission(context))
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            hasNotificationPermission = isGranted
        }
    )

    DisposableEffect(key1 = permissionLauncher) {
        if (!hasNotificationPermission && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch("android.permission.POST_NOTIFICATIONS")
        }
        onDispose { }
    }
}

private fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(
            context,
            "android.permission.POST_NOTIFICATIONS"
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        // For versions below Android 13, notifications are enabled by default
        true
    }
}
