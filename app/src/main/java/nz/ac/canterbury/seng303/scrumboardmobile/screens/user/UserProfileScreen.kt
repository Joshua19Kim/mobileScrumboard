package nz.ac.canterbury.seng303.scrumboardmobile.screens.user

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.R
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user.UserViewModel

@Composable
fun UserProfileScreen(
    navController: NavController,
    userViewModel: UserViewModel,
    currentUserId: Int,
    removeAuthenticationFn: suspend () -> Unit,
    editCurrentUser: suspend(Int) -> Unit
) {
    val context = LocalContext.current
    val userState by userViewModel.currentUser.collectAsState(null)
    val user: User? = userState

    LaunchedEffect(currentUserId) {
        userViewModel.setCurrentUser(currentUserId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {
               Row(
                   modifier = Modifier
                       .fillMaxWidth(),
                   horizontalArrangement = Arrangement.Center
               ) {
                   Icon(
                       Icons.Default.Person,
                       contentDescription = context.getString(R.string.profile),
                       modifier = Modifier.size(96.dp)
                   )
               }
                Spacer(modifier = Modifier.height(8.dp))

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    Row {
                        Text(
                            text = buildAnnotatedString {
                                append("${context.getString(R.string.username)}: ")
                                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                append("${user?.username}")
                                pop()
                            }
                        )
                    }
                    Row {
                        Text(
                            text = buildAnnotatedString {
                                append("${context.getString(R.string.email)}: ")
                                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                append("${user?.email}")
                                pop()
                            }
                        )
                    }
                    Row {
                        Text(
                            text = buildAnnotatedString {
                                append("${context.getString(R.string.name)}: ")
                                pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                                append("${user?.firstName} ${user?.lastName}")
                                pop()
                            }
                        )
                    }
                }
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            Button(
                onClick = {
                    navController.navigate("EditUser")
                },
                modifier = Modifier.padding(vertical = 8.dp) // Add vertical padding
            ) {
                Text(context.getString(R.string.edit_label))
            }
            Button(
                onClick = {
                    navController.navigate("Preference")
                },
                modifier = Modifier.padding(vertical = 8.dp) // Add vertical padding
            ) {
                Text(context.getString(R.string.change_preference))
            }
            Button(
                modifier = Modifier.padding(vertical = 8.dp), // Add vertical padding
                onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    removeAuthenticationFn()
                    editCurrentUser(-1)
                }
                navController.navigate("Home")
            }) {
                Text(ContextCompat.getString(context, R.string.log_out_label))
            }
        }
    }
}
