package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class CreateUserViewModel: ViewModel() {
    var username by mutableStateOf("")
        private set
    fun updateUsername(newUsername: String) {
        username = newUsername
    }
    var password by mutableStateOf("")
    fun updatePassword(newPassword: String) {
        password = newPassword
    }
    var firstName by mutableStateOf("")
    fun updateFirstName(newFirstName: String) {
        firstName = newFirstName
    }
    var lastName by mutableStateOf("")
    fun updateLastName(newLastName: String) {
        lastName = newLastName
    }
    var userEmail by mutableStateOf("")
    fun updateUserEmail(newUserEmail: String) {
        userEmail = newUserEmail
    }
}