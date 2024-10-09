package nz.ac.canterbury.seng303.scrumboardmobile.screens.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.util.isValidEmail
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel

@Composable
fun RegisterUserScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    userEmail: String,
    onUserEmailChange: (String) -> Unit,
    username: String,
    onUsernameChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    firstName: String,
    onFirstNameChange: (String) -> Unit,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    createUserFn: (String, String, String, String, String) -> Unit,
    grantAuthentication: suspend () -> Unit,
    editCurrentUser: suspend (Int) -> Unit,
) {
    val context = LocalContext.current
    var errorMessage = ""
    var isRegistering by remember { mutableStateOf(false) }
    var registrationError by remember { mutableStateOf<String?>(null) }

    userViewModel.getUsers()
    val users: List<User> by userViewModel.users.collectAsState(emptyList())

    //it refresh all the input fields when app leaves this screen
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                onUserEmailChange("")
                onUsernameChange("")
                onPasswordChange("")
                onFirstNameChange("")
                onLastNameChange("")
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }


    LaunchedEffect(isRegistering) {
        if (isRegistering) {
            try {
                errorMessage = ""
                if (username.isBlank() || userEmail.isBlank() || password.isBlank() || firstName.isBlank() || lastName.isBlank()) {
                    errorMessage += "All fields are required. "
                }
                if (!isValidEmail(userEmail)) {
                    errorMessage += "Invalid email format. "
                }
                users.forEach { user ->
                    if (username == user.username) {
                        errorMessage += "Username is already taken. "
                    }
                    if (userEmail == user.userEmail) {
                        errorMessage += "Email is already in use. "
                    }
                }

                if (errorMessage.isEmpty()) {
                    coroutineScope {
                        createUserFn(username, password, firstName, lastName, userEmail)
                    }

                    val user = userViewModel.getUserByName(username)
                    if (user != null) {
                        editCurrentUser(user.userId)
                        grantAuthentication()
                        onUserEmailChange("")
                        onUsernameChange("")
                        onPasswordChange("")
                        onFirstNameChange("")
                        onLastNameChange("")
                        navController.popBackStack()
                    } else {
                        registrationError = "Failed to retrieve user after registration"
                    }
                } else {
                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                registrationError = e.message ?: "An error occurred during registration"
            } finally {
                isRegistering = false
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(ContextCompat.getString(context, R.string.register_header))
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = userEmail,
                onValueChange = { onUserEmailChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.useremail)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = username,
                onValueChange = { onUsernameChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.username)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = password,
                onValueChange = { onPasswordChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = firstName,
                onValueChange = { onFirstNameChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.first_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = lastName,
                onValueChange = { onLastNameChange(it) },
                label = { Text(ContextCompat.getString(context, R.string.last_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    isRegistering = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(ContextCompat.getString(context, R.string.register_label))
            }
        }
    }
}