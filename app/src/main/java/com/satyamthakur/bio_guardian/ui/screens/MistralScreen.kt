import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.satyamthakur.bio_guardian.data.entity.AnimalDetails
import com.satyamthakur.bio_guardian.data.entity.HabitatCoordinates
import com.satyamthakur.bio_guardian.ui.screens.AnimalNotFoundScreen
import com.satyamthakur.bio_guardian.ui.viewmodel.AnimalInfoUiState
import com.satyamthakur.bio_guardian.ui.viewmodel.MistralViewModel

fun parseAnimalDetails(rawResponse: String): AnimalDetails? {
    return try {
        // Clean up the response
        val cleanedJson = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .trimIndent()
            .trim()

        // Parse with Gson
        Gson().fromJson(cleanedJson, AnimalDetails::class.java)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

@SuppressLint("SuspiciousIndentation")
@Composable
fun MistralScreen(imageUrl: String, animalName: String, navController: NavController) {
    val viewModel: MistralViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsState()

    // Fetch animal info when imageUrl changes or screen appears
    LaunchedEffect(imageUrl, animalName) {
        viewModel.fetchAnimalInfo(imageUrl, animalName)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        when (uiState) {
            is AnimalInfoUiState.Loading -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator()
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Fetching Details...")
                }
            }

            is AnimalInfoUiState.Success -> {
                val response = (uiState as AnimalInfoUiState.Success).response
                Log.d("BIOAPP RESPONSE", "${response.choices?.firstOrNull()?.message?.content ?: "No response"}")

                val rawResponse = response.choices?.firstOrNull()?.message?.content ?: ""
                val animalDetails = parseAnimalDetails(rawResponse)

                Log.d("BIOAPP", animalDetails.toString())

                if (animalDetails != null && animalDetails.species == "not found") {
                    Log.d("BIOAPP", "ANIMAL NOT FOUND")
                    AnimalNotFoundScreen(navController)
                } else {
                    val sampleData = AnimalDetails(
                        species = "Giant Panda",
                        scientificName = "Ailuropoda melanoleuca",
                        habitat = "Mountain forests in central China",
                        diet = "Herbivorous, primarily bamboo",
                        lifespan = "20-30 years in wild",
                        sizeWeight = "1.5m long, 80-140 kg",
                        reproduction = "3-5 month gestation",
                        behavior = "Solitary animals",
                        conservationStatus = "Vulnerable",
                        specialAdaptations = "Pseudo-thumbs for bamboo",
                        habitatCoordinates = HabitatCoordinates(
                            coordinates = listOf(
                                listOf(30.2345, 100.6785),
                                listOf(30.4567, 101.1234),
                                listOf(30.6789, 100.9876),
                                listOf(30.3456, 101.4567),
                                listOf(30.5678, 100.7890),
                                listOf(30.7890, 101.2345)
                            )
                        )
                    )

                    AnimalDetailGrid(
                        animalDetails = animalDetails ?: sampleData,
                        imageUrl = imageUrl
                    )
                }
            }

            is AnimalInfoUiState.Error -> {
                val errorMessage = (uiState as AnimalInfoUiState.Error).message
                Text("Error: $errorMessage", color = MaterialTheme.colorScheme.error)
            }

            is AnimalInfoUiState.Idle -> {
                // Optional: You can show a placeholder or initial state here
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Ready to analyze animal image")
                }
            }
        }
    }
}
