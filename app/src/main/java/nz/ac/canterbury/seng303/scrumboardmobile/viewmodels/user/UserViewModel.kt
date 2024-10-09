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
                   password: String,
                   firstName: String,
                   lastName: String,
                   userEmail: String
    ) = viewModelScope.launch {
        val user = User(
            username = username,
            password = password,
            firstName = firstName,
            lastName = lastName,
            userEmail = userEmail,
        )
        try {
            val userId = userDao.insertUser(user)
            _currentUser.value = userDao.findByUsername(user.username)
            Log.d("USER_VIEW_MODEL", "User inserted with id: $userId")
        } catch (e: Exception) {
            Log.e("USER_VIEW_MODEL", "Could not insert User", e)
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
}