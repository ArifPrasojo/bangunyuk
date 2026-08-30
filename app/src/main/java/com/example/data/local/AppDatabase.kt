package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [AlarmEntity::class, PhotoSpotEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun photoSpotDao(): PhotoSpotDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bangun_yuk_database"
                )
                .addCallback(object : Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        CoroutineScope(Dispatchers.IO).launch {
                            // Seed default photo spots and a sample alarm
                            val appDb = getInstance(context)
                            seedInitialData(appDb)
                        }
                    }
                })
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedInitialData(db: AppDatabase) {
            val spotDao = db.photoSpotDao()
            val alarmDao = db.alarmDao()

            spotDao.insertOrUpdate(
                PhotoSpotEntity(
                    spotKey = "TOILET",
                    spotName = "Toilet / Kloset",
                    spotDescription = "Foto area toilet kamar mandi untuk memastikan kamu sudah bangun dan ke kamar mandi.",
                    imageUri = null,
                    iconName = "Wc"
                )
            )
            spotDao.insertOrUpdate(
                PhotoSpotEntity(
                    spotKey = "SINK",
                    spotName = "Wastafel / Cermin",
                    spotDescription = "Foto wastafel cuci muka atau sikat gigi.",
                    imageUri = null,
                    iconName = "Wash"
                )
            )
            spotDao.insertOrUpdate(
                PhotoSpotEntity(
                    spotKey = "KITCHEN",
                    spotName = "Dapur / Kulkas",
                    spotDescription = "Foto kulkas atau dispenser air minum pagi.",
                    imageUri = null,
                    iconName = "Kitchen"
                )
            )
            spotDao.insertOrUpdate(
                PhotoSpotEntity(
                    spotKey = "DOOR",
                    spotName = "Pintu Depan / Ruang Tamu",
                    spotDescription = "Foto pintu luar atau ruang depan untuk mulai aktivitas.",
                    imageUri = null,
                    iconName = "DoorFront"
                )
            )

            // Default alarm for 05:30 AM
            alarmDao.insertAlarm(
                AlarmEntity(
                    hour = 5,
                    minute = 30,
                    isEnabled = true,
                    label = "Bangun Subuh / Pagi",
                    daysOfWeek = "1,2,3,4,5,6,7",
                    missionType = "PHOTO",
                    photoTargetPlace = "TOILET",
                    photoTargetLabel = "Toilet / Kamar Mandi"
                )
            )
        }
    }
}
