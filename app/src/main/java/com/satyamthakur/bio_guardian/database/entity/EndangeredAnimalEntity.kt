package com.satyamthakur.bio_guardian.database.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "endangered_animals")
data class EndangeredAnimalEntity(
    @PrimaryKey val id: Int,
    val animalName: String?,
    val animalType: String?,
    val biologicalName: String?,
    val conservationStatus: String?,
    val imageUrl: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val lastFetched: Long // Timestamp of when this data was last fetched
)