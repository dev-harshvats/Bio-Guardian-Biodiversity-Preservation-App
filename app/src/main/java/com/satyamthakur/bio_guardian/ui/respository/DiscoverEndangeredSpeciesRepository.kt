package com.satyamthakur.bio_guardian.ui.respository

import android.util.Log
import androidx.compose.ui.text.capitalize
import com.satyamthakur.bio_guardian.data.api.DisoverEndangeredApi
import com.satyamthakur.bio_guardian.database.EndangeredAnimalDao
import com.satyamthakur.bio_guardian.database.entity.EndangeredAnimalEntity
import java.util.Locale
import javax.inject.Inject

class DiscoverEndangeredSpeciesRepository @Inject constructor(
    private val api: DisoverEndangeredApi,
    private val dao: EndangeredAnimalDao
) {
    private val TAG = "EndangeredRepo"

    suspend fun getEndangeredSpecies(animalType: String): Result<List<EndangeredAnimalEntity>> {
        // Check local database first
        val localData = dao.getAnimalsByType(animalType.replaceFirstChar { if (it.isLowerCase()) it.titlecase(
            Locale.getDefault()) else it.toString() })
        Log.d(TAG, localData.toString())

        val currentTime = System.currentTimeMillis()
        val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L // One week in milliseconds

        // If local data exists and is fresh (less than one week old), return it
        if (localData.isNotEmpty() && localData.all { (currentTime - it.lastFetched) < oneWeekInMillis }) {
            Log.d(TAG, "Using fresh cached data for $animalType (${localData.size} items)")
            return Result.success(localData)
        }

        // Data is either missing or stale, fetch from API
        return try {
            Log.d(TAG, "Fetching data from API for $animalType")
            val response = api.getEndangeredAnimals(animalType)

            if (response.isSuccessful && response.body() != null) {
                val apiData = response.body()!!
                Log.d(TAG, "API fetch successful, got ${apiData.size} animals")

                // Map API response to entities
                val entities = apiData.map { animal ->
                    EndangeredAnimalEntity(
                        id = animal.id ?: 0,
                        animalName = animal.animalName,
                        animalType = animal.animalType,
                        biologicalName = animal.biologicalName,
                        conservationStatus = animal.conservationStatus,
                        imageUrl = animal.imageUrl,
                        createdAt = animal.createdAt,
                        updatedAt = animal.updatedAt,
                        lastFetched = currentTime
                    )
                }

                // Save to local database
                dao.deleteAnimalsByType(animalType)
                dao.insertAnimals(entities)
                Result.success(entities)
            } else {
                // API failed, return local data if available
                Log.w(TAG, "API error: HTTP ${response.code()}")
                if (localData.isNotEmpty()) {
                    Log.d(TAG, "Falling back to stale data (${localData.size} items)")
                    Result.success(localData) // Return stale data as fallback
                } else {
                    Log.e(TAG, "No cached data available to fall back to")
                    Result.failure(Exception("API call failed with code ${response.code()}"))
                }
            }
        } catch (e: Exception) {
            // Network or other error (likely offline)
            Log.w(TAG, "Exception during network call: ${e.message}")

            if (localData.isNotEmpty()) {
                Log.d(TAG, "Network error, falling back to cache (${localData.size} items)")
                Result.success(localData) // Return stale data as fallback
            } else {
                Log.e(TAG, "Network error and no cached data available")
                Result.failure(Exception("Failed to fetch data: ${e.message}", e))
            }
        }
    }
}