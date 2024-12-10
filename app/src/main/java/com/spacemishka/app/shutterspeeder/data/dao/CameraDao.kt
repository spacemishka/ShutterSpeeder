package com.spacemishka.app.shutterspeeder.data.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import kotlinx.coroutines.flow.Flow

@Dao
interface CameraDao {
    @Query("SELECT * FROM camera ORDER BY createdAt DESC")
    fun getAllCameras(): Flow<List<Camera>>
    
    @Query("SELECT * FROM camera WHERE id = :cameraId")
    suspend fun getCameraById(cameraId: Long): Camera?
    
    @Insert
    suspend fun insertCamera(camera: Camera): Long
    
    @Update
    suspend fun updateCamera(camera: Camera)
    
    @Delete
    suspend fun deleteCamera(camera: Camera)
    
    @Query("SELECT * FROM camera WHERE manufacturer = :manufacturer AND model = :model AND serialNumber = :serialNumber")
    suspend fun findCamera(manufacturer: String, model: String, serialNumber: String): Camera?
} 