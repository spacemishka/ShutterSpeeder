package com.spacemishka.app.shutterspeeder.data.entity
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Camera(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val manufacturer: String,
    val model: String,
    val serialNumber: String,
    val createdAt: Long = System.currentTimeMillis()
) {
    // Ensure unique camera identification by serial number
    @Ignore
    val uniqueIdentifier = "$manufacturer-$model-$serialNumber"
} 