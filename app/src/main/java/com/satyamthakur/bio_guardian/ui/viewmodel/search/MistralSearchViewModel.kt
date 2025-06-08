package com.satyamthakur.bio_guardian.ui.viewmodel.search

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
class MistralSearchViewModel @Inject constructor(
    private val repository: MistralRepository
): ViewModel() {

    private val _uiState = MutableStateFlow<SearchUiState>(SearchUiState.Idle)
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    fun searchAnimals(query: String) {
        if (query.isBlank()) {
            _uiState.value = SearchUiState.Error("Search query cannot be empty")
            return
        }

        _uiState.value = SearchUiState.Loading

        val mistralRequest = buildSearchRequest(query)

        viewModelScope.launch {
            try {
                val response = repository.getAnimalInfoFromImage(mistralRequest)
                if (response.isSuccessful && response.body() != null) {
                    _uiState.value = SearchUiState.Success(response.body()!!)
                } else {
                    _uiState.value = SearchUiState.Error(
                        "Error: ${response.code()} - ${response.errorBody()?.string() ?: "Unknown error"}"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = SearchUiState.Error("Exception: ${e.message ?: "Unknown exception"}")
                Log.e("SearchViewModel", "Error searching animals", e)
            }
        }
    }

    private fun buildSearchRequest(query: String): MistralRequest {
        return MistralRequest(
            model = "mistral-large-latest",
            messages = listOf(
                Message(
                    content = listOf(
                        Content(
                            type = "text",
                            text = """
                                You are a search engine for a very large animals database. 
                                I have given you a query to search for the animal "$query" in the database.
                                Respond with up to 10 relevant results based on the query using fuzzy search or advanced search algorithms in JSON format.
                                Each result should follow this JSON schema:
                                {
                                    "id": <number>,
                                    "name": "<string>",
                                    "scientific_name": "<string>",
                                    "habitat": "<string>",
                                    "diet": "<string>",
                                    "status": "<string>"
                                }
                                !!!CRITICAL If no relevant animal results are found, return a single JSON object with all fields set to "not found":
                                {
                                    "id": 0,
                                    "name": "not found",
                                    "scientific_name": "not found",
                                    "habitat": "not found",
                                    "diet": "not found",
                                    "status": "not found"
                                }
                                Ensure the response is a valid JSON array (for multiple results) or a single JSON object (for no results) with no extra characters, formatting, or markdown.
                                Do NOT include any additional text or explanations.
                            """.trimIndent()
                        )
                    )
                )
            )
        )
    }
}

sealed class SearchUiState {
    object Idle : SearchUiState()
    object Loading : SearchUiState()
    data class Success(val response: MistralResponse) : SearchUiState()
    data class Error(val message: String) : SearchUiState()
}