package nz.ac.canterbury.seng303.scrumboardmobile.screens.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import kotlinx.coroutines.launch
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

    userViewModel.getUsers()
    val users: List<User> by userViewModel.users.collectAsState(emptyList())
    val userNames: List<String> = users.map { it.username }
    val scrollState = rememberScrollState()


    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState),
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
                userViewModel.viewModelScope.launch {
                    when {
                        username.trim().isEmpty() -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.title_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        userEmail.trim().isEmpty() -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.email_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        password.trim().isEmpty() -> {
                            Toast.makeText(
                                context,
                                context.getString(R.string.email_empty),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        firstName.trim().isEmpty() || lastName.trim().isEmpty() -> {
                            Toast.makeText(context, context.getString(R.string.enter_name), Toast.LENGTH_SHORT).show()
                        }
                        userNames.contains(username) -> {
                            Toast.makeText(context, context.getString(R.string.invalid_username), Toast.LENGTH_SHORT).show()
                        }
                        !isValidEmail(userEmail) -> {
                            Toast.makeText(context,  context.getString(R.string.invalidEmail), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            createUserFn(username,userEmail, password, firstName, lastName)
                            onUserEmailChange("")
                            onUsernameChange("")
                            onPasswordChange("")
                            onFirstNameChange("")
                            onLastNameChange("")
                            navController.popBackStack()
                            //Grant Auth has to goes before current user
                            grantAuthentication()
                            userViewModel.currentUser.value?.let { editCurrentUser(it.userId) }
                        }
                    }
                }
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