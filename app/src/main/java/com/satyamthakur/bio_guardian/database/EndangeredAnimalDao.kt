package com.satyamthakur.bio_guardian.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.satyamthakur.bio_guardian.database.entity.EndangeredAnimalEntity

@Dao
interface EndangeredAnimalDao {
    @Query("SELECT * FROM endangered_animals WHERE animalType = :type")
    suspend fun getAnimalsByType(type: String): List<EndangeredAnimalEntity>

//    @Query("SELECT * FROM endangered_animals")
//    suspend fun getAnimalsByType(): List<EndangeredAnimalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAnimals(animals: List<EndangeredAnimalEntity>)

    @Query("DELETE FROM endangered_animals WHERE animalType = :type")
    suspend fun deleteAnimalsByType(type: String)
}