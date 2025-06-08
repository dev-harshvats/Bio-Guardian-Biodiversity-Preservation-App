package com.satyamthakur.bio_guardian.ui.screens.feed

import android.content.Context
import android.util.Log
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.cloud.storage.BlobId
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.satyamthakur.bio_guardian.data.entity.AnimalDetails
import com.satyamthakur.bio_guardian.data.entity.HabitatCoordinates
import com.satyamthakur.bio_guardian.utils.getStorageCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Data classes (these should match the ones in your main file)
data class UserPerspective(
    val id: String = UUID.randomUUID().toString(),
    val perspective: String,
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val species: String
)

data class SpeciesPerspectives(
    val species: String,
    val scientificName: String,
    val perspectives: MutableList<UserPerspective> = mutableListOf(),
    var lastUpdated: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)

@Composable
fun SpeciesPerspectivesScreen(
    animalDetails: AnimalDetails,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var perspectivesData by remember { mutableStateOf<SpeciesPerspectives?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isRefreshing by remember { mutableStateOf(false) }

    // Function to load perspectives
    val loadPerspectives = {
        coroutineScope.launch {
            try {
                isRefreshing = true
                errorMessage = null
                val data = loadPerspectivesFromGCS(context, animalDetails.species)

                // Debug logging
                Log.d("PerspectivesDebug", "Loaded data: $data")
                Log.d("PerspectivesDebug", "Data is null: ${data == null}")
                Log.d("PerspectivesDebug", "Perspectives list: ${data?.perspectives}")
                Log.d("PerspectivesDebug", "Perspectives count: ${data?.perspectives?.size ?: 0}")

                perspectivesData = data
                isLoading = false
            } catch (e: Exception) {
                Log.e("PerspectivesLoad", "Failed to load perspectives", e)
                errorMessage = "Failed to load perspectives. Please try again."
                isLoading = false
            } finally {
                isRefreshing = false
            }
        }
    }

    // Load perspectives when screen loads
    LaunchedEffect(animalDetails.species) {
        loadPerspectives()
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFADEBB3)),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "${animalDetails.species} Community",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Perspectives and discussions",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )

                    perspectivesData?.let { data ->
                        Text(
                            text = "${data.perspectives.size} perspectives shared",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Refresh Button
                IconButton(
                    onClick = { loadPerspectives() },
                    enabled = !isRefreshing
                ) {
                    if (isRefreshing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh perspectives",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content Section
        when {
            isLoading -> {
                Log.d("PerspectivesDebug", "Showing loading state")
                // Loading State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(48.dp),
                            strokeWidth = 4.dp
                        )
                        Text(
                            text = "Loading perspectives...",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            errorMessage != null -> {
                Log.d("PerspectivesDebug", "Showing error state: $errorMessage")
                // Error State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                        Text(
                            text = errorMessage!!,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.error,
                            textAlign = TextAlign.Center
                        )
                        OutlinedButton(
                            onClick = { loadPerspectives() }
                        ) {
                            Text("Try Again")
                        }
                    }
                }
            }

            perspectivesData == null || perspectivesData?.perspectives?.isEmpty() == true -> {
                Log.d("PerspectivesDebug", "Showing empty state - Data null: ${perspectivesData == null}, Empty: ${perspectivesData?.perspectives?.isEmpty()}")
                // Empty State
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forum,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = Color(0xFF4CAF50).copy(alpha = 0.6f)
                        )
                        Text(
                            text = "No perspectives yet",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Be the first to share your thoughts about ${animalDetails.species}!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Switch to the Details tab to add your perspective.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            else -> {
                Log.d("PerspectivesDebug", "Showing perspectives list with ${perspectivesData?.perspectives?.size} items")
                // Show Perspectives
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 24.dp)
                ) {
                    perspectivesData?.perspectives?.let { perspectives ->
                        // Sort by timestamp (most recent first)
                        val sortedPerspectives = perspectives.sortedByDescending {
                            try {
                                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(it.timestamp)
                            } catch (e: Exception) {
                                Date(0) // fallback for invalid dates
                            }
                        }

                        items(sortedPerspectives) { perspective ->
                            PerspectiveCard(perspective = perspective)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PerspectiveCard(
    perspective: UserPerspective,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with user info and timestamp
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .background(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                shape = CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Text(
                        text = "Community Member",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = formatTimestamp(perspective.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Perspective content
            Text(
                text = perspective.perspective,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )
        }
    }
}

// Function to load perspectives from GCS
suspend fun loadPerspectivesFromGCS(
    context: Context,
    species: String
): SpeciesPerspectives? {
    return withContext(Dispatchers.IO) {
        try {
            val storage = getStorageCredentials(context).service
            val bucketName = "bio-guardian-capstone-image"

            // Create species-specific filename (replace special characters and spaces)
            val speciesFileName = species
                .lowercase()
                .replace(" ", "_")
                .replace("[^a-z0-9_]".toRegex(), "")

            val perspectivesFilePath = "perspectives/${speciesFileName}_perspectives.json"
            val blobId = BlobId.of(bucketName, perspectivesFilePath)

            Log.d("PerspectivesLoad", "Looking for file: $perspectivesFilePath")

            // Try to get existing perspectives for this species
            val blob = storage.get(blobId)
            if (blob != null && blob.exists()) {
                val jsonContent = String(blob.getContent())
                val gson = Gson()
                val type = object : TypeToken<SpeciesPerspectives>() {}.type
                val perspectivesData = gson.fromJson<SpeciesPerspectives>(jsonContent, type)

                Log.d("PerspectivesLoad", "Loaded ${perspectivesData.perspectives.size} perspectives for $species")
                perspectivesData
            } else {
                Log.d("PerspectivesLoad", "No perspectives file found for $species")
                null
            }
        } catch (e: Exception) {
            Log.e("PerspectivesLoad", "Failed to load perspectives for $species", e)
            throw e
        }
    }
}

// Helper function to format timestamp
fun formatTimestamp(timestamp: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = inputFormat.parse(timestamp)
        date?.let { outputFormat.format(it) } ?: timestamp
    } catch (e: Exception) {
        timestamp
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun SpeciesPerspectivesScreenPreview() {
    MaterialTheme {
        val sampleData = AnimalDetails(
            species = "Giant Panda",
            scientificName = "Ailuropoda melanoleuca",
            habitat = "Mountain forests in central China",
            diet = "Herbivorous, primarily bamboo",
            lifespan = "20-30 years",
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

        SpeciesPerspectivesScreen(animalDetails = sampleData)
    }
}