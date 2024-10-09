package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import nz.ac.canterbury.seng303.scrumboardmobile.models.User

class UserEditViewModel: ViewModel() {
    var username by mutableStateOf("")
    fun updateUsername(newUsername: String) {
        username = newUsername
    }
    var email by mutableStateOf("")
    fun updateEmail(newEmail: String) {
        email = newEmail
    }
    var password by mutableStateOf("")

    var oldPassword by mutableStateOf("")
    fun updateOldPassword(oldPasswordInput: String) {
        oldPassword = oldPasswordInput
    }

    var newPassword by mutableStateOf("")
    fun updateNewPassword(newPasswordInput: String) {
        newPassword = newPasswordInput
    }
    var firstName by mutableStateOf("")
    fun updateFirstName(newFirstName: String) {
        firstName = newFirstName
    }
    var lastName by mutableStateOf("")
    fun updateLastName(newLastName: String) {
        lastName = newLastName
    }

    fun setUserProperties(user: User) {
        username = user.username
        email = user.email
        password = user.password
        firstName = user.firstName
        lastName = user.lastName
    }
}