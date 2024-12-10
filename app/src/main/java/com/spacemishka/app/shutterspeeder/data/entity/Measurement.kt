package com.spacemishka.app.shutterspeeder.data.entity
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

@Entity
data class Measurement(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cameraId: Long,  // Foreign key to Camera
    
    // Raw sensor data
    val bottomLeftOpen: Long,
    val bottomLeftClose: Long,
    val centerOpen: Long,
    val centerClose: Long,
    val topRightOpen: Long,
    val topRightClose: Long,
    
    // Offset values
    val bottomLeftOpenOffset: Int,
    val bottomLeftCloseOffset: Int,
    val topRightOpenOffset: Int,
    val topRightCloseOffset: Int,

    // Metadata
    val firmwareVersion: String,
    val measurementUnit: String = "microsecond",
    val timestamp: Long = System.currentTimeMillis(),
    val selectedShutterSpeed: String,  // The shutter speed selected on the camera
    val referenceShutterSpeed: String,
    val referenceSpeedMicros: Long,

    // Durations (in microseconds)
    val bottomLeftDuration: Long,
    val centerDuration: Long,
    val topRightDuration: Long,

    // Deviations (in microseconds)
    val bottomLeftDeviation: Long,
    val centerDeviation: Long,
    val topRightDeviation: Long,

    // Deviations (in percentage)
    val bottomLeftDeviationPercent: Double,
    val centerDeviationPercent: Double,
    val topRightDeviationPercent: Double
) {
    constructor(
        id: Long = 0,
        cameraId: Long,
        bottomLeftOpen: Long,
        bottomLeftClose: Long,
        centerOpen: Long,
        centerClose: Long,
        topRightOpen: Long,
        topRightClose: Long,
        bottomLeftOpenOffset: Int,
        bottomLeftCloseOffset: Int,
        topRightOpenOffset: Int,
        topRightCloseOffset: Int,
        firmwareVersion: String,
        measurementUnit: String = "microsecond",
        timestamp: Long = System.currentTimeMillis(),
        selectedShutterSpeed: String,
        referenceShutterSpeed: String,
        referenceSpeedMicros: Long
    ) : this(
        id = id,
        cameraId = cameraId,
        bottomLeftOpen = bottomLeftOpen,
        bottomLeftClose = bottomLeftClose,
        centerOpen = centerOpen,
        centerClose = centerClose,
        topRightOpen = topRightOpen,
        topRightClose = topRightClose,
        bottomLeftOpenOffset = bottomLeftOpenOffset,
        bottomLeftCloseOffset = bottomLeftCloseOffset,
        topRightOpenOffset = topRightOpenOffset,
        topRightCloseOffset = topRightCloseOffset,
        firmwareVersion = firmwareVersion,
        measurementUnit = measurementUnit,
        timestamp = timestamp,
        selectedShutterSpeed = selectedShutterSpeed,
        referenceShutterSpeed = referenceShutterSpeed,
        referenceSpeedMicros = referenceSpeedMicros,
        bottomLeftDuration = bottomLeftClose - bottomLeftOpen,
        centerDuration = centerClose - centerOpen,
        topRightDuration = topRightClose - topRightOpen,
        bottomLeftDeviation = (bottomLeftClose - bottomLeftOpen) - referenceSpeedMicros,
        centerDeviation = (centerClose - centerOpen) - referenceSpeedMicros,
        topRightDeviation = (topRightClose - topRightOpen) - referenceSpeedMicros,
        bottomLeftDeviationPercent = ((bottomLeftClose - bottomLeftOpen - referenceSpeedMicros).toDouble() / referenceSpeedMicros.toDouble()) * 100,
        centerDeviationPercent = ((centerClose - centerOpen - referenceSpeedMicros).toDouble() / referenceSpeedMicros.toDouble()) * 100,
        topRightDeviationPercent = ((topRightClose - topRightOpen - referenceSpeedMicros).toDouble() / referenceSpeedMicros.toDouble()) * 100
    )
} 