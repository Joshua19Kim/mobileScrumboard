package nz.ac.canterbury.seng303.scrumboardmobile.viewmodels.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import nz.ac.canterbury.seng303.scrumboardmobile.dao.UserDao
import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import nz.ac.canterbury.seng303.scrumboardmobile.util.hashPassword

class UserViewModel(
    private val userDao: UserDao
): ViewModel() {
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> get() = _users
    fun getUsers() = viewModelScope.launch {
        userDao.getAllUsers().catch { Log.e("USER_VIEW_MODEL", it.toString()) }
            .collect { _users.emit(it) }
    }
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    fun createUser(username: String,
                   email: String,
                   password: String,
                   firstName: String,
                   lastName: String,
    ) = viewModelScope.launch {
        val user = User(
            username = username,
            email = email,
            password = password,
            firstName = firstName,
            lastName = lastName,
        )
        try {
            val userId = userDao.insertUser(user)
            _currentUser.value = userDao.findByUsername(user.username)
            Log.d("USER_VIEW_MODEL", "User inserted with id: $userId")
        } catch (e: Exception) {
            Log.e("USER_VIEW_MODEL", "Could not insert User", e)
        }
    }

    fun updateUser(user: User)= viewModelScope.launch {
        try {
            userDao.updateUser(user)
            Log.d("USER_VIEW_MODEL", "User status updated successfully")
        } catch (e: Exception) {
            Log.e("USER_VIEW_MODEL", "Error updating user status", e)
        }
    }

    suspend fun authenticateUser(username: String, password: String): Boolean {
        val user: User? = userDao.findByUsername(username)
        val isAuthenticated = !(user == null || user.password != hashPassword(password))
        if (isAuthenticated) {
            _currentUser.value = user
        }
        return isAuthenticated
    }
    suspend fun getUserById(userId : Int ): User? {
        return userDao.getUserByUserId(userId)
    }
    suspend fun getUserByName(username: String): User? {
        return userDao.findByUsername(username)
    }
    suspend fun setCurrentUser(userId: Int) {
        _currentUser.value = getUserById(userId)
    }
}