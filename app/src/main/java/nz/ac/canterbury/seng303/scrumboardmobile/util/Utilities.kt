package nz.ac.canterbury.seng303.scrumboardmobile.util

import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

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