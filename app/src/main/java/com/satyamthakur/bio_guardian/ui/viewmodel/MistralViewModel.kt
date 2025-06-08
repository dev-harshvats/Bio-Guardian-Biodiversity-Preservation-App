package com.satyamthakur.bio_guardian.ui.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.satyamthakur.bio_guardian.data.entity.Content
import com.satyamthakur.bio_guardian.data.entity.Message
import com.satyamthakur.bio_guardian.data.entity.MistralRequest
import com.satyamthakur.bio_guardian.data.entity.MistralResponse
import com.satyamthakur.bio_guardian.data.repository.MistralRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MistralViewModel @Inject constructor(
    private val repository: MistralRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AnimalInfoUiState>(AnimalInfoUiState.Idle)
    val uiState: StateFlow<AnimalInfoUiState> = _uiState.asStateFlow()

    fun fetchAnimalInfo(imageUrl: String, animalName: String = "null") {
        _uiState.value = AnimalInfoUiState.Loading

        val mistralRequest = buildMistralRequest(imageUrl, animalName)

        viewModelScope.launch {
            try {
                val response = repository.getAnimalInfoFromImage(mistralRequest)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = AnimalInfoUiState.Success(response.body()!!)
                } else {
                    _uiState.value = AnimalInfoUiState.Error(
                        "Error: ${response.code()} - ${response.errorBody()?.string() ?: "Unknown error"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = AnimalInfoUiState.Error("Exception: ${e.message ?: "Unknown exception"}")
                Log.e("MistralViewModel", "Error fetching animal info", e)
            }
        }
    }

    private fun buildMistralRequest(imageUrl: String, animalName: String): MistralRequest {
        return if (animalName != "null") {
            Log.d("BIOAPP", "ANIMAL NAME DETAILS FINDING")
            buildAnimalNameRequest(animalName)
        } else {
            Log.d("BIOAPP", "ANIMAL IMAGE DETAILS FINDING")
            buildImageRecognitionRequest(imageUrl)
        }
    }

    private fun buildAnimalNameRequest(animalName: String): MistralRequest {
        return MistralRequest(
            messages = listOf(
                Message(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Give me the details of the $animalName animal in the following format:\n" +
                                    "\n" +
                                    "{\n" +
                                    "  \"Species\": \"...\",\n" +
                                    "  \"Scientific Name\": \"...\",\n" +
                                    "  \"Habitat\": \"...\",\n" +
                                    "  \"Diet\": \"...\",\n" +
                                    "  \"Lifespan\": \"...\",\n" +
                                    "  \"Size & Weight\": \"...\",\n" +
                                    "  \"Reproduction\": \"...\",\n" +
                                    "  \"Behavior\": \"...\",\n" +
                                    "  \"Conservation Status\": \"...\",\n" +
                                    "  \"Special Adaptations\": \"...\",\n" +
                                    "  \"Habitat Coordinates\": {\n" +
                                    "    \"Type\": \"Polygon\",\n" +
                                    "    \"Coordinates\": [\n" +
                                    "      [latitude1, longitude1],\n" +
                                    "      [latitude2, longitude2],\n" +
                                    "      [latitude3, longitude3],\n" +
                                    "      [latitude4, longitude4],\n" +
                                    "      [latitude5, longitude5],\n" +
                                    "      [latitudeN, longitudeN]\n" +
                                    "    ]\n" +
                                    "  }\n" +
                                    "}\n" +
                                    "\n" +
                                    "Ensure the response is a valid JSON object with no extra characters, formatting, or markdown (like triple backticks).\n" +
                                    "Do NOT include any additional text or explanations.\n" +
                                    "The 'Coordinates' array should contain **as many latitude-longitude points as necessary** to form a complete polygon representing the animal's habitat.\n" +
                                    "Ensure that the JSON response is complete and syntactically correct."
                        )
                    )
                )
            )
        )
    }

    private fun buildImageRecognitionRequest(imageUrl: String): MistralRequest {
        return MistralRequest(
            messages = listOf(
                Message(
                    content = listOf(
                        Content(
                            type = "text",
                            text = "Identify the type of animal (including humans) in the image. If no animal is detected, return a JSON response with all fields set to \"not found\":\n" +
                                    "\n" +
                                    "{\n" +
                                    "  \"Species\": \"...\",\n" +
                                    "  \"Scientific Name\": \"...\",\n" +
                                    "  \"Habitat\": \"...\",\n" +
                                    "  \"Diet\": \"...\",\n" +
                                    "  \"Lifespan\": \"...\",\n" +
                                    "  \"Size & Weight\": \"...\",\n" +
                                    "  \"Reproduction\": \"...\",\n" +
                                    "  \"Behavior\": \"...\",\n" +
                                    "  \"Conservation Status\": \"...\",\n" +
                                    "  \"Special Adaptations\": \"...\",\n" +
                                    "  \"Habitat Coordinates\": {\n" +
                                    "    \"Type\": \"Polygon\",\n" +
                                    "    \"Coordinates\": [\n" +
                                    "      [latitude1, longitude1],\n" +
                                    "      [latitude2, longitude2],\n" +
                                    "      [latitude3, longitude3],\n" +
                                    "      [latitude4, longitude4],\n" +
                                    "      [latitude5, longitude5],\n" +
                                    "      [latitudeN, longitudeN]\n" +
                                    "    ]\n" +
                                    "  }\n" +
                                    "}\n" +
                                    "\n" +
                                    "Ensure the response is a valid JSON object with no extra characters, formatting, or markdown (like triple backticks).\n" +
                                    "Do NOT include any additional text or explanations.\n" +
                                    "The 'Coordinates' array should contain **as many latitude-longitude points as necessary** to form a complete polygon representing the animal's habitat.\n" +
                                    "Ensure that the JSON response is complete and syntactically correct."
                        ),
                        Content(
                            type = "image_url",
                            image_url = imageUrl
                        )
                    )
                )
            )
        )
    }
}

sealed class AnimalInfoUiState {
    object Idle : AnimalInfoUiState()
    object Loading : AnimalInfoUiState()
    data class Success(val response: MistralResponse) : AnimalInfoUiState()
    data class Error(val message: String) : AnimalInfoUiState()
}