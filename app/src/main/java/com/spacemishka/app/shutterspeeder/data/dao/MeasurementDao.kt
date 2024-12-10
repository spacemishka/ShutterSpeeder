package com.spacemishka.app.shutterspeeder.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.spacemishka.app.shutterspeeder.data.entity.Measurement
import kotlinx.coroutines.flow.Flow

@Dao
interface MeasurementDao {
    @Query("SELECT * FROM measurement WHERE cameraId = :cameraId ORDER BY timestamp DESC")
    fun getMeasurementsForCamera(cameraId: Long): Flow<List<Measurement>>
    
    @Insert
    suspend fun insert(measurement: Measurement): Long
    
    @Delete
    suspend fun deleteMeasurement(measurement: Measurement)
    
    @Query("SELECT * FROM measurement WHERE id = :measurementId")
    suspend fun getMeasurementById(measurementId: Long): Measurement?
} 