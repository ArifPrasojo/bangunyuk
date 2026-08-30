package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val isEnabled: Boolean = true,
    val label: String = "Bangun Pagi",
    val daysOfWeek: String = "1,2,3,4,5", // 1=Mon, 2=Tue, 3=Wed, 4=Thu, 5=Fri, 6=Sat, 7=Sun, or empty for once
    val soundName: String = "Energetic Sunrise",
    val volume: Int = 100,
    val vibrate: Boolean = true,
    val snoozeMinutes: Int = 5,
    val missionType: String = "PHOTO", // PHOTO, MATH, SHAKE, STEPS, MEMORY, TYPING
    val missionDifficulty: String = "MEDIUM", // EASY, MEDIUM, HARD
    val photoTargetPlace: String = "TOILET", // TOILET, SINK, KITCHEN, CUSTOM
    val photoReferenceUri: String? = null,
    val photoTargetLabel: String = "Toilet / Kamar Mandi",
    val shakeTarget: Int = 30,
    val stepsTarget: Int = 25,
    val mathProblemCount: Int = 3,
    val snoozedUntil: Long = 0L,
    val createdAt: Long = System.currentTimeMillis()
) {
    fun isCurrentlySnoozed(): Boolean = snoozedUntil > System.currentTimeMillis()

    fun getMissionTypeList(): List<com.example.mission.MissionType> {
        val types = missionType.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .map { com.example.mission.MissionType.fromString(it) }
        return if (types.isEmpty()) listOf(com.example.mission.MissionType.PHOTO) else types
    }

    fun getPhotoTargetPlaceList(): List<String> {
        val places = photoTargetPlace.split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
        return if (places.isEmpty()) listOf("TOILET") else places
    }
}

@Entity(tableName = "photo_spots")
data class PhotoSpotEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val spotKey: String, // TOILET, SINK, KITCHEN, DOOR, DESK
    val spotName: String,
    val spotDescription: String,
    val imageUri: String?,
    val iconName: String,
    val updatedAt: Long = System.currentTimeMillis()
)
