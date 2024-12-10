package com.spacemishka.app.shutterspeeder.data
import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.spacemishka.app.shutterspeeder.data.dao.CameraDao
import com.spacemishka.app.shutterspeeder.data.dao.MeasurementDao
import com.spacemishka.app.shutterspeeder.data.entity.Camera
import com.spacemishka.app.shutterspeeder.data.entity.Measurement

@Database(
    entities = [Camera::class, Measurement::class],
    version = 5,
    exportSchema = true
)
abstract class ShutterSpeedDatabase : RoomDatabase() {
    abstract fun cameraDao(): CameraDao
    abstract fun measurementDao(): MeasurementDao

    companion object {
        @Volatile
        private var INSTANCE: ShutterSpeedDatabase? = null

        fun getDatabase(context: Context): ShutterSpeedDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    ShutterSpeedDatabase::class.java,
                    "shutterspeed.db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
} 