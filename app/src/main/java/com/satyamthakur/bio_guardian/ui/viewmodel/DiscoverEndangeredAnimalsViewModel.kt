package com.satyamthakur.bio_guardian.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyamthakur.bio_guardian.database.entity.EndangeredAnimalEntity
import com.satyamthakur.bio_guardian.ui.respository.DiscoverEndangeredSpeciesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel responsible for fetching and managing endangered animal data.
 * It communicates with [DiscoverEndangeredSpeciesRepository] to retrieve data
 * and exposes UI state via [animalsState] for Jetpack Compose integration.
 */
@HiltViewModel
class DiscoverEndangeredAnimalsViewModel @Inject constructor(
    private val repository: DiscoverEndangeredSpeciesRepository
) : ViewModel() {

    private val _animalsState = MutableStateFlow<AnimalsUiState>(AnimalsUiState.Idle)
    val animalsState: StateFlow<AnimalsUiState> = _animalsState.asStateFlow()

    /**
     * Fetches endangered animals based on the specified [animalType].
     * Updates [animalsState] with the result: Loading, Success, or Error.
     *
     * @param animalType The type of animals to fetch (e.g., "mammals", "birds").
     */
    fun fetchEndangeredAnimals(animalType: String) {
        viewModelScope.launch {
            _animalsState.value = AnimalsUiState.Loading

            try {
                val result = repository.getEndangeredSpecies(animalType)

                result.fold(
                    onSuccess = { animals ->
                        _animalsState.value = if (animals.isNotEmpty()) {
                            AnimalsUiState.Success(animals)
                        } else {
                            AnimalsUiState.Error("No animals found for type: $animalType")
                        }
                    },
                    onFailure = { exception ->
                        val errorMessage = "Failed to fetch animals: ${exception.message ?: "Unknown error"}"
                        Log.e(TAG, errorMessage, exception)
                        _animalsState.value = AnimalsUiState.Error(errorMessage)
                    }
                )
            } catch (e: Exception) {
                val errorMessage = "Unexpected error: ${e.message ?: "Unknown error"}"
                Log.e(TAG, errorMessage, e)
                _animalsState.value = AnimalsUiState.Error(errorMessage)
            }
        }
    }

    /**
     * Sealed class representing the UI state for endangered animals data.
     */
    sealed class AnimalsUiState {
        /** Initial state before any data is fetched. */
        object Idle : AnimalsUiState()

        /** State when data is being fetched. */
        object Loading : AnimalsUiState()

        /** State when data is successfully fetched. */
        data class Success(val data: List<EndangeredAnimalEntity>) : AnimalsUiState()

        /** State when an error occurs during data fetching. */
        data class Error(val message: String) : AnimalsUiState()
    }

    companion object {
        private const val TAG = "DiscoverEndangeredAnimalsViewModel"
    }
}