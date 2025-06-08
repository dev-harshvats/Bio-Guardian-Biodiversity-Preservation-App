package com.satyamthakur.bio_guardian.ui.screens.discover

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.capitalize
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import com.satyamthakur.bio_guardian.data.entity.DiscoverEndangeredAnimalsResponse
import com.satyamthakur.bio_guardian.database.entity.EndangeredAnimalEntity
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.customTypography
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.ui.viewmodel.DiscoverEndangeredAnimalsViewModel

/**
 * Screen displaying a list of endangered animals based on the provided [animalType].
 * @param animalType The type of animals to fetch (e.g., "mammals", "birds").
 * @param viewModel The ViewModel providing animal data.
 */
@Composable
fun DiscoverEndangeredAnimalsScreen(
    animalType: String = "amphibians",
    viewModel: DiscoverEndangeredAnimalsViewModel = hiltViewModel()
) {
    MaterialTheme(typography = customTypography) {
        val state by viewModel.animalsState.collectAsState()

        LaunchedEffect(animalType) {
            viewModel.fetchEndangeredAnimals(animalType)
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
//                .padding(top = 24.dp, bottom = 24.dp), // Modern spacing
            contentAlignment = Alignment.Center
        ) {
            when (state) {
                is DiscoverEndangeredAnimalsViewModel.AnimalsUiState.Idle -> {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Discover Endangered $animalType".replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchEndangeredAnimals(animalType) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Explore Now",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                is DiscoverEndangeredAnimalsViewModel.AnimalsUiState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.size(56.dp),
                        strokeWidth = 4.dp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                is DiscoverEndangeredAnimalsViewModel.AnimalsUiState.Success -> {
                    val animals = (state as DiscoverEndangeredAnimalsViewModel.AnimalsUiState.Success).data
                    if (animals.isEmpty()) {
                        Text(
                            text = "No $animalType found.",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp),
                            contentPadding = PaddingValues(vertical = 12.dp)
                        ) {
                            item {
                                Text(
                                    text = "Endangered ${animalType.capitalize()}",
                                    fontFamily = Montserrat,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(animals) { item ->
                                AnimalCard(item)
                            }
                        }
                    }
                }

                is DiscoverEndangeredAnimalsViewModel.AnimalsUiState.Error -> {
                    val message = (state as DiscoverEndangeredAnimalsViewModel.AnimalsUiState.Error).message
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Oops! Something went wrong.",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { viewModel.fetchEndangeredAnimals(animalType) },
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Try Again",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AnimalCard(
    animal: EndangeredAnimalEntity,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
//        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = md_theme_light_background)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                            )
                        )
                    )
            ) {
                AsyncImage(
                    model = animal.imageUrl,
                    contentDescription = animal.animalName ?: "Endangered animal image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.width(20.dp))

            Column {
                Text(
                    text = animal.animalName ?: "Unknown Animal",
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = animal.biologicalName ?: "Unknown Scientific Name",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Status: ${animal.conservationStatus ?: "Unknown"}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.semantics { contentDescription = "Conservation status: ${animal.conservationStatus ?: "Unknown"}" }
                )
            }
        }
    }
}