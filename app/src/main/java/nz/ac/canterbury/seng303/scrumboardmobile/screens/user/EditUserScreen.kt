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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.coroutineScope
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.util.hashPassword
import nz.ac.canterbury.seng303.scrumboardmobile.util.isValidEmail
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserEditViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel

@Composable
fun EditUserScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    userEditViewModel: UserEditViewModel,
    currentUserId: Int
) {
    LaunchedEffect(currentUserId) {
        coroutineScope { userViewModel.setCurrentUser(currentUserId) }
    }

    val context = LocalContext.current
    val userState by userViewModel.currentUser.collectAsState(null)
    val user: User? = userState
    val scrollState = rememberScrollState()

    LaunchedEffect(user) {
        if (user != null) {
            userEditViewModel.setUserProperties(user)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize()
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(context.getString(R.string.profile))

        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = userEditViewModel.username,
                onValueChange = { userEditViewModel.updateUsername(it) },
                label = { Text(context.getString(R.string.username)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = userEditViewModel.email,
                onValueChange = { userEditViewModel.updateEmail(it) },
                label = { Text(context.getString(R.string.email)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = userEditViewModel.oldPassword,
                onValueChange = { userEditViewModel.updateOldPassword(it) },
                label = { Text(context.getString(R.string.old_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = userEditViewModel.newPassword,
                onValueChange = { userEditViewModel.updateNewPassword(it) },
                label = { Text(context.getString(R.string.new_password)) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )


            OutlinedTextField(
                value = userEditViewModel.firstName,
                onValueChange = { userEditViewModel.updateFirstName(it) },
                label = { Text(context.getString(R.string.first_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            OutlinedTextField(
                value = userEditViewModel.lastName,
                onValueChange = { userEditViewModel.updateLastName(it) },
                label = { Text(context.getString(R.string.last_name)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )

            Button(
                onClick = {
                    when {
                        userEditViewModel.username.trim().isEmpty() -> {
                            Toast.makeText(context, context.getString(R.string.title_empty), Toast.LENGTH_SHORT).show()
                        }
                        userEditViewModel.email.trim().isEmpty() -> {
                            Toast.makeText(context, context.getString(R.string.email_empty), Toast.LENGTH_SHORT).show()
                        }
                        (userEditViewModel.oldPassword != "" && hashPassword(userEditViewModel.oldPassword) != userEditViewModel.password) ||
                        (userEditViewModel.oldPassword == "" && userEditViewModel.newPassword != "")  -> {
                            Toast.makeText(context, context.getString(R.string.incorrect_password), Toast.LENGTH_LONG).show()
                            userEditViewModel.updateOldPassword("")
                            userEditViewModel.updateNewPassword("")
                        }
                        userEditViewModel.oldPassword != "" && userEditViewModel.newPassword == "" -> {
                            Toast.makeText(context, context.getString(R.string.no_empty_password), Toast.LENGTH_LONG).show()
                            userEditViewModel.updateOldPassword("")
                            userEditViewModel.updateNewPassword("")
                        }
                        !isValidEmail(userEditViewModel.email) -> {
                            Toast.makeText(context, context.getString(R.string.invalidEmail), Toast.LENGTH_SHORT).show()
                        }
                        else -> {
                            if (user != null) {
                                userViewModel.updateUser(
                                    user.copy(
                                        username = userEditViewModel.username,
                                        email = userEditViewModel.email,
                                        password = hashPassword(userEditViewModel.newPassword),
                                        firstName = userEditViewModel.firstName,
                                        lastName = userEditViewModel.lastName
                                    )
                                )
                                userEditViewModel.updateOldPassword("")
                                userEditViewModel.updateNewPassword("")
                                navController.popBackStack()
                                Toast.makeText(context, context.getString(R.string.user_details_saved), Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                Text(context.getString(R.string.save_label))
            }
        }
    }
}
