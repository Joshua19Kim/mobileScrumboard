package nz.ac.canterbury.seng303.scrumboardmobile.util

import nz.ac.canterbury.seng303.scrumboardmobile.models.User
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

fun convertTimestampToReadableTime(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return dateFormat.format(calendar.time)
}

fun convertTimestampToReadableDate(timestamp: Long): String {
    val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    val calendar = Calendar.getInstance()
    calendar.timeInMillis = timestamp
    return dateFormat.format(calendar.time)
}

fun hashPassword(password: String, algorithm: String = "SHA-256"): String {
    val bytes = MessageDigest
        .getInstance(algorithm)
        .digest(password.toByteArray())

    return bytes.joinToString("") {
        "%02x".format(it) // Convert each byte to a hex string
    }
}

fun findUserWithId(
    users: List<User>,
    userId: Int
): User? {
    return users.find { it.userId == userId }
}

private val EMAIL_REGEX = Pattern.compile(
    "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
)

fun isValidEmail(email: String): Boolean {
    return email.isNotBlank() && EMAIL_REGEX.matcher(email).matches()
}