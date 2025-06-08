import android.content.Context
import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Feed
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.SubcomposeAsyncImage
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.satyamthakur.bio_guardian.data.entity.AnimalDetails
import com.satyamthakur.bio_guardian.data.entity.HabitatCoordinates
import com.satyamthakur.bio_guardian.ui.screens.AnimalLocationMap
import com.satyamthakur.bio_guardian.ui.screens.feed.SpeciesPerspectivesScreen
import com.satyamthakur.bio_guardian.utils.getStorageCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

// Data class for user perspective
data class UserPerspective(
    val id: String = UUID.randomUUID().toString(),
    val perspective: String,
    val timestamp: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val species: String
)

// Data class for species perspectives collection
data class SpeciesPerspectives(
    val species: String,
    val scientificName: String,
    val perspectives: MutableList<UserPerspective> = mutableListOf(),
    var lastUpdated: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
)

sealed class AnimalDetailTile {
    data class FullWidthTile(
        val icon: ImageVector,
        val title: String,
        val value: String,
        val secondaryValue: String? = null,
        val color: Color
    ) : AnimalDetailTile()

    data class HalfWidthTile(
        val icon: ImageVector,
        val title: String,
        val value: String,
        val color: Color
    ) : AnimalDetailTile()

    companion object {
        @Composable
        fun createFullWidth(
            icon: ImageVector,
            title: String,
            value: String,
            secondaryValue: String? = null,
            color: Color = MaterialTheme.colorScheme.primary
        ): FullWidthTile = FullWidthTile(icon, title, value, secondaryValue, color)

        @Composable
        fun createHalfWidth(
            icon: ImageVector,
            title: String,
            value: String,
            color: Color = MaterialTheme.colorScheme.primary
        ): HalfWidthTile = HalfWidthTile(icon, title, value, color)
    }
}

// Simplified main screen that just calls AnimalDetailGrid
@Composable
fun AnimalDetailsScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    animalDetails: AnimalDetails,
    imageUrl: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(paddingValues)
    ) {
        AnimalDetailGrid(animalDetails = animalDetails, imageUrl = imageUrl)
    }
}

@Composable
fun HalfWidthTilesRow(
    leftTile: AnimalDetailTile.HalfWidthTile,
    rightTile: AnimalDetailTile.HalfWidthTile,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Max)
        ) {
            DetailTile(tile = leftTile)
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .height(IntrinsicSize.Max)
        ) {
            DetailTile(tile = rightTile)
        }
    }
}

@Composable
fun PerspectiveInputSection(
    animalDetails: AnimalDetails,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var perspectiveText by remember { mutableStateOf("") }
    var isUploading by remember { mutableStateOf(false) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header with icon
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Create,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column {
                    Text(
                        text = "Share Your Perspective",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tell us what you think about ${animalDetails.species}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Perspective input
            OutlinedTextField(
                value = perspectiveText,
                onValueChange = { perspectiveText = it },
                label = { Text("Your thoughts about ${animalDetails.species}") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                maxLines = 5,
                shape = RoundedCornerShape(12.dp),
                enabled = !isUploading,
                placeholder = {
                    Text(
                        text = "Share your observations, experiences, or thoughts about this species...",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            )

            // Status messages
            successMessage?.let { message ->
                Text(
                    text = message,
                    color = Color(0xFF4CAF50),
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            errorMessage?.let { message ->
                Text(
                    text = message,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Upload progress
            if (isUploading) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(
                        text = "Saving your perspective...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Submit button
            Button(
                onClick = {
                    if (perspectiveText.isBlank() || perspectiveText.length < 10) {
                        errorMessage = "Please enter at least 10 characters for your perspective"
                        return@Button
                    }

                    errorMessage = null
                    successMessage = null
                    isUploading = true

                    val userPerspective = UserPerspective(
                        perspective = perspectiveText.trim(),
                        species = animalDetails.species
                    )

                    coroutineScope.launch {
                        try {
                            savePerspectiveToGCS(context, userPerspective, animalDetails)

                            // Reset form
                            perspectiveText = ""
                            isUploading = false
                            successMessage = "Thank you! Your perspective has been saved successfully."

                            // Clear success message after 5 seconds
                            kotlinx.coroutines.delay(5000)
                            successMessage = null

                        } catch (e: Exception) {
                            Log.e("PerspectiveSave", "Failed to save perspective", e)
                            isUploading = false
                            errorMessage = "Failed to save your perspective. Please try again."

                            // Clear error message after 5 seconds
                            kotlinx.coroutines.delay(5000)
                            errorMessage = null
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isUploading && perspectiveText.isNotBlank(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (isUploading) "Saving..." else "Create Post",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// Function to save perspective to GCS with species-based organization
suspend fun savePerspectiveToGCS(
    context: Context,
    userPerspective: UserPerspective,
    animalDetails: AnimalDetails
) {
    withContext(Dispatchers.IO) {
        try {
            val storage = getStorageCredentials(context).service
            val bucketName = "bio-guardian-capstone-image"

            // Create species-specific filename (replace special characters and spaces)
            val speciesFileName = animalDetails.species
                .lowercase()
                .replace(" ", "_")
                .replace("[^a-z0-9_]".toRegex(), "")

            val perspectivesFilePath = "perspectives/${speciesFileName}_perspectives.json"
            val blobId = BlobId.of(bucketName, perspectivesFilePath)

            // Try to get existing perspectives for this species
            val existingPerspectives = try {
                val blob = storage.get(blobId)
                if (blob != null && blob.exists()) {
                    val jsonContent = String(blob.getContent())
                    val gson = Gson()
                    val type = object : TypeToken<SpeciesPerspectives>() {}.type
                    gson.fromJson<SpeciesPerspectives>(jsonContent, type)
                } else {
                    null
                }
            } catch (e: Exception) {
                Log.w("PerspectiveSave", "No existing perspectives found or error reading: ${e.message}")
                null
            }

            // Create or update the species perspectives object
            var speciesPerspectives = existingPerspectives ?: SpeciesPerspectives(
                species = animalDetails.species,
                scientificName = animalDetails.scientificName
            )

            // Add the new perspective
            speciesPerspectives.perspectives.add(userPerspective)
            speciesPerspectives.lastUpdated = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

            // Convert to JSON
            val gson = Gson()
            val jsonContent = gson.toJson(speciesPerspectives)

            // Save to GCS
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/json")
                .setContentDisposition("inline")
                .build()

            storage.create(blobInfo, jsonContent.toByteArray(Charsets.UTF_8))

            Log.d("PerspectiveSave", "Perspective saved successfully for species: ${animalDetails.species}")
            Log.d("PerspectiveSave", "File path: $perspectivesFilePath")
            Log.d("PerspectiveSave", "Total perspectives for this species: ${speciesPerspectives.perspectives.size}")

        } catch (e: Exception) {
            Log.e("PerspectiveSave", "Failed to save perspective", e)
            throw e
        }
    }
}

// Modified AnimalDetailGrid with integrated tabs
@Composable
fun AnimalDetailGrid(animalDetails: AnimalDetails, imageUrl: String) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }

    val tabs = listOf(
        "Details" to Icons.Default.Info,
        "Feed" to Icons.Default.Feed
    )

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Tab Row
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 4.dp
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                tabs.forEachIndexed { index, (title, icon) ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = {
                            selectedTabIndex = index
                            Log.d("TabSwitch", "Switched to tab: $title at index $index")
                        },
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .heightIn(min = 48.dp) // Minimum touch target size
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = "$title tab",
                                modifier = Modifier.size(20.dp),
                                tint = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = title,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (selectedTabIndex == index)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Tab Content
        Box(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
        ) {
            when (selectedTabIndex) {
                0 -> AnimalDetailContent(animalDetails = animalDetails, imageUrl = imageUrl)
                1 -> SpeciesPerspectivesScreen(animalDetails = animalDetails)
            }
        }
    }
}

// Separated the detail content into its own composable
@Composable
fun AnimalDetailContent(animalDetails: AnimalDetails, imageUrl: String) {
    LazyColumn(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        // Image Section
        item {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                tonalElevation = 1.dp,
                shadowElevation = 2.dp
            ) {
                SubcomposeAsyncImage(
                    model = imageUrl,
                    contentDescription = "Image of ${animalDetails.species}",
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    contentScale = ContentScale.Fit,
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(48.dp),
                                color = MaterialTheme.colorScheme.secondary,
                                strokeWidth = 4.dp
                            )
                        }
                    }
                )
            }
        }

        // Species & Scientific Name
        item {
            HalfWidthTilesRow(
                leftTile = AnimalDetailTile.createHalfWidth(
                    icon = Icons.Default.Pets,
                    title = "Species",
                    value = animalDetails.species,
                    color = MaterialTheme.colorScheme.primary
                ),
                rightTile = AnimalDetailTile.createHalfWidth(
                    icon = Icons.Default.Science,
                    title = "Scientific Name",
                    value = animalDetails.scientificName,
                    color = MaterialTheme.colorScheme.secondary
                )
            )
        }

        // Habitat
        item {
            DetailTile(
                tile = AnimalDetailTile.createFullWidth(
                    icon = Icons.Default.Park,
                    title = "Habitat",
                    value = animalDetails.habitat,
                    color = MaterialTheme.colorScheme.tertiary
                )
            )
        }

        // Diet & Lifespan
        item {
            HalfWidthTilesRow(
                leftTile = AnimalDetailTile.createHalfWidth(
                    icon = Icons.Default.Restaurant,
                    title = "Diet",
                    value = animalDetails.diet,
                    color = MaterialTheme.colorScheme.secondary
                ),
                rightTile = AnimalDetailTile.createHalfWidth(
                    icon = Icons.Default.Cake,
                    title = "Lifespan",
                    value = animalDetails.lifespan,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        }

        // Conservation Status
        item {
            DetailTile(
                tile = AnimalDetailTile.createFullWidth(
                    icon = Icons.Default.HealthAndSafety,
                    title = "Conservation",
                    value = animalDetails.conservationStatus,
                    color = if (animalDetails.conservationStatus == "Vulnerable")
                        Color(0xFFF4D03F) else Color(0xFF58D68D)
                )
            )
        }

        // Adaptations
        item {
            DetailTile(
                tile = AnimalDetailTile.createFullWidth(
                    icon = Icons.Default.Construction,
                    title = "Adaptations",
                    value = animalDetails.specialAdaptations
                )
            )
        }

        // Map View of Habitat location
        item {
            AnimalLocationMap(
                coordinates = animalDetails.habitatCoordinates.coordinates,
                animalName = animalDetails.species
            )
        }

        // Perspective Input Section
        item {
            PerspectiveInputSection(animalDetails = animalDetails)
        }
    }
}

@Composable
fun DetailTile(tile: AnimalDetailTile) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .heightIn(min = 120.dp), // Minimum height for consistency
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .background(color = when(tile) {
                        is AnimalDetailTile.FullWidthTile -> tile.color
                        is AnimalDetailTile.HalfWidthTile -> tile.color
                    }.copy(alpha = 0.2f), shape = CircleShape)
            ) {
                Icon(
                    imageVector = when(tile) {
                        is AnimalDetailTile.FullWidthTile -> tile.icon
                        is AnimalDetailTile.HalfWidthTile -> tile.icon
                    },
                    contentDescription = null,
                    tint = when(tile) {
                        is AnimalDetailTile.FullWidthTile -> tile.color
                        is AnimalDetailTile.HalfWidthTile -> tile.color
                    },
                    modifier = Modifier.size(20.dp)
                )
            }

            Text(
                text = when(tile) {
                    is AnimalDetailTile.FullWidthTile -> tile.title
                    is AnimalDetailTile.HalfWidthTile -> tile.title
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Text(
                text = when(tile) {
                    is AnimalDetailTile.FullWidthTile -> tile.value
                    is AnimalDetailTile.HalfWidthTile -> tile.value
                },
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (tile is AnimalDetailTile.FullWidthTile) {
                tile.secondaryValue?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true, widthDp = 360, heightDp = 800)
@Composable
fun AnimalDetailTabScreenPreview() {
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

        AnimalDetailsScreen(
            paddingValues = PaddingValues(),
            navController = rememberNavController(),
            animalDetails = sampleData,
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a3/Aptenodytes_forsteri_-Snow_Hill_Island%2C_Antarctica_-adults_and_juvenile-8.jpg/800px-Aptenodytes_forsteri_-Snow_Hill_Island%2C_Antarctica_-adults_and_juvenile-8.jpg"
        )
    }
}
