package com.satyamthakur.bio_guardian.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.satyamthakur.bio_guardian.database.entity.EndangeredAnimalEntity

@Database(entities = [EndangeredAnimalEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun endangeredAnimalDao(): EndangeredAnimalDao
}