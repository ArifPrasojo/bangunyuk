package com.example.data.repository

import com.example.data.local.AlarmDao
import com.example.data.local.AlarmEntity
import com.example.data.local.PhotoSpotDao
import com.example.data.local.PhotoSpotEntity
import kotlinx.coroutines.flow.Flow

class AlarmRepository(
    private val alarmDao: AlarmDao,
    private val photoSpotDao: PhotoSpotDao
) {
    val allAlarms: Flow<List<AlarmEntity>> = alarmDao.getAllAlarms()
    val allPhotoSpots: Flow<List<PhotoSpotEntity>> = photoSpotDao.getAllPhotoSpots()

    suspend fun getAlarmById(id: Long): AlarmEntity? = alarmDao.getAlarmById(id)

    suspend fun insertAlarm(alarm: AlarmEntity): Long = alarmDao.insertAlarm(alarm)

    suspend fun updateAlarm(alarm: AlarmEntity) = alarmDao.updateAlarm(alarm)

    suspend fun deleteAlarm(alarm: AlarmEntity) = alarmDao.deleteAlarm(alarm)

    suspend fun deleteAlarmById(id: Long) = alarmDao.deleteAlarmById(id)

    suspend fun getEnabledAlarms(): List<AlarmEntity> = alarmDao.getEnabledAlarms()

    suspend fun savePhotoSpot(spot: PhotoSpotEntity): Long = photoSpotDao.insertOrUpdate(spot)

    suspend fun deletePhotoSpot(spot: PhotoSpotEntity) = photoSpotDao.deleteById(spot.id)

    suspend fun deletePhotoSpotById(id: Long) = photoSpotDao.deleteById(id)

    suspend fun getPhotoSpotByKey(key: String): PhotoSpotEntity? = photoSpotDao.getPhotoSpotByKey(key)
}
