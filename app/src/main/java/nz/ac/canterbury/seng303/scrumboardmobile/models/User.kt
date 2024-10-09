package nz.ac.canterbury.seng303.scrumboardmobile.models

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Int = 0,
    val username: String,
    val password: String,
    val firstName: String,
    val lastName: String,
    val userEmail: String,
)
