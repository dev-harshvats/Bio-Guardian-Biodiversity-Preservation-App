package com.satyamthakur.bio_guardian.di

import android.content.Context
import androidx.room.Room
import com.satyamthakur.bio_guardian.database.AppDatabase
import com.satyamthakur.bio_guardian.database.EndangeredAnimalDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "bio_guardian_db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideEndangeredAnimalDao(database: AppDatabase): EndangeredAnimalDao {
        return database.endangeredAnimalDao()
    }
}