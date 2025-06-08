package com.satyamthakur.bio_guardian.ui.screens.community

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCameraBack
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import android.Manifest
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.cloud.storage.Storage
import com.satyamthakur.bio_guardian.ui.navigation.Endpoints
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.Roboto
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.utils.getStorageCredentials
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max

sealed class ImageSelectionOption {
    data object Camera : ImageSelectionOption()
    data object Gallery : ImageSelectionOption()
}

data class BlogData(
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val author: String = "",
    val category: String = "",
    val imageUrl: String = "",
    val publishedAt: String = ""
)

@Composable
fun CommunityScreen(paddingValues: PaddingValues, navController: NavController) {
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Create Post", "View Posts")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_light_background)
            .padding(paddingValues)
    ) {
        // Tab Row
        TabRow(
            selectedTabIndex = selectedTabIndex,
            modifier = Modifier.fillMaxWidth(),
            containerColor = Color.White,
            contentColor = Color.Black,
            indicator = { tabPositions ->
                TabRowDefaults.Indicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = Color(0xFF5CE65C)
                )
            }
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTabIndex == index,
                    onClick = { selectedTabIndex = index },
                    text = {
                        Text(
                            text = title,
                            fontFamily = Montserrat,
                            fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Medium,
                            color = if (selectedTabIndex == index) Color(0xFF5CE65C) else Color.Gray
                        )
                    }
                )
            }
        }

        // Tab Content
        when (selectedTabIndex) {
            0 -> CreatePostTab(navController)
            1 -> ViewPostsTab(navController)
        }
    }
}

@Composable
fun CreatePostTab(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    var blogData by remember { mutableStateOf(BlogData()) }
    var isUploading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // For temporary camera image storage
    val tempImageUri = remember { mutableStateOf<Uri?>(null) }

    // Create a temporary file for camera image
    fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val imageFileName = "JPEG_${timeStamp}_"
        return File.createTempFile(
            imageFileName,
            ".jpg",
            context.cacheDir
        )
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { imageUri = it }
    }

    // Camera launcher
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            tempImageUri.value?.let {
                imageUri = it
            }
        }
    }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                val photoFile = createImageFile()
                photoFile?.let { file ->
                    tempImageUri.value = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.bio_guardian_fileprovider",
                        file
                    )
                    tempImageUri.value?.let { uri ->
                        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
                            putExtra(MediaStore.EXTRA_OUTPUT, uri)
                            putExtra("android.intent.extras.CAMERA_FACING", android.hardware.Camera.CameraInfo.CAMERA_FACING_BACK)
                            putExtra("android.intent.extras.LENS_FACING_FRONT", 0)
                            putExtra("android.intent.extra.USE_FRONT_CAMERA", false)
                        }
                        cameraLauncher.launch(uri)
                    }
                }
            } catch (e: Exception) {
                Log.e("Camera", "Error launching camera", e)
                errorMessage = "Could not launch camera"
            }
        } else {
            errorMessage = "Camera permission is required to take photos"
        }
    }

    // Handle option selection
    fun handleOptionSelection(option: ImageSelectionOption) {
        when (option) {
            ImageSelectionOption.Camera -> {
                permissionLauncher.launch(Manifest.permission.CAMERA)
            }
            ImageSelectionOption.Gallery -> {
                galleryLauncher.launch("image/*")
            }
        }
        showImagePickerDialog = false
    }

    // Main scrollable content
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_light_background),
        contentPadding = PaddingValues(
            top = 16.dp,
            start = 16.dp,
            end = 16.dp,
            bottom = 24.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Title Section
            Text(
                text = "Create Post",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        item {
            // Image Selection Section
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Add an image (Optional)",
                    fontFamily = Roboto,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium
                )

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = Color.White
                    ),
                    border = BorderStroke(2.dp, Color.Gray),
                    onClick = { showImagePickerDialog = true }
                ) {
                    if (imageUri == null) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.PhotoCameraBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = Color.Gray
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Tap to add image",
                                    color = Color.Gray,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    } else {
                        Image(
                            painter = rememberAsyncImagePainter(model = imageUri),
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }
            }
        }

        item {
            // Blog Title Input
            OutlinedTextField(
                value = blogData.title,
                onValueChange = { blogData = blogData.copy(title = it) },
                label = { Text("Post Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            // Author Name Input
            OutlinedTextField(
                value = blogData.author,
                onValueChange = { blogData = blogData.copy(author = it) },
                label = { Text("Author Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            // Category Input
            OutlinedTextField(
                value = blogData.category,
                onValueChange = { blogData = blogData.copy(category = it) },
                label = { Text("Category (e.g., Wildlife, Conservation, Research)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp)
            )
        }

        item {
            // Blog Content Input
            OutlinedTextField(
                value = blogData.content,
                onValueChange = { blogData = blogData.copy(content = it) },
                label = { Text("Post Content") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                maxLines = 10,
                shape = RoundedCornerShape(8.dp)
            )
        }

        // Error and Success Messages
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                errorMessage?.let { error ->
                    Text(
                        text = error,
                        color = Color.Red,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                successMessage?.let { success ->
                    Text(
                        text = success,
                        color = Color.Green,
                        fontSize = 12.sp,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Upload Progress
        item {
            if (isUploading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp)
                        )
                        Text(
                            text = "Publishing your post...",
                            fontSize = 12.sp,
                            color = Color.Gray
                        )
                    }
                }
            }
        }

        // Upload Button
        item {
            Button(
                onClick = {
                    if (validateBlogData(blogData)) {
                        errorMessage = null
                        successMessage = null
                        isUploading = true
                        val startTime = System.currentTimeMillis()
                        Log.d("PostUploadTiming", "Post upload started at: $startTime")

                        coroutineScope.launch {
                            try {
                                val postId = UUID.randomUUID().toString()
                                var imageUrl = ""

                                // Upload image first if selected
                                if (imageUri != null) {
                                    val bucketName = "bio-guardian-capstone-image".trim()
                                    val imageFileName = "post_${postId}_${UUID.randomUUID()}"

                                    uploadImageToGCS(context, imageUri!!, imageFileName, bucketName)
                                    imageUrl = "https://storage.googleapis.com/bio-guardian-capstone-image/images/$imageFileName.jpg"
                                    Log.d("BIOAPP", "Image uploaded successfully: $imageUrl")
                                }

                                // Upload blog with image URL
                                val updatedBlogData = blogData.copy(imageUrl = imageUrl)
                                uploadBlogToGCS(context, updatedBlogData, postId)

                                // Reset form after successful upload
                                blogData = BlogData()
                                imageUri = null
                                isUploading = false
                                successMessage = "Post published successfully!"

                                val endTime = System.currentTimeMillis()
                                val latency = endTime - startTime
                                Log.d("PostUploadTiming", "Post upload completed at: $endTime, Latency: ${latency}ms")
                                Log.d("BIOAPP", "Post uploaded successfully with ID: $postId")

                                // Clear success message after 3 seconds
                                kotlinx.coroutines.delay(3000)
                                successMessage = null

                            } catch (e: Exception) {
                                Log.e("BIOAPP", "Post upload error", e)
                                errorMessage = "Failed to upload post. Please try again."
                                isUploading = false

                                val endTime = System.currentTimeMillis()
                                val latency = endTime - startTime
                                Log.d("PostUploadTiming", "Post upload failed at: $endTime, Latency: ${latency}ms")
                            }
                        }
                    } else {
                        val validationError = getValidationError(blogData)
                        errorMessage = validationError ?: "Please fill in all required fields"
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = !isUploading && blogData.title.isNotBlank() &&
                        blogData.content.isNotBlank() && blogData.author.isNotBlank(),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = if (isUploading) "Publishing..." else "Publish Post",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }

    // Show dialog when needed
    if (showImagePickerDialog) {
        ImagePickerDialog(
            onDismiss = { showImagePickerDialog = false },
            onOptionSelected = { option -> handleOptionSelection(option) }
        )
    }
}

@Composable
fun ViewPostsTab(navController: NavController) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val blogPosts = remember { mutableStateListOf<BlogData>() }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Function to load blog posts
    fun loadBlogPosts() {
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            try {
                val posts = fetchBlogPostsFromGCS(context)
                blogPosts.clear()
                blogPosts.addAll(posts)
                isLoading = false
            } catch (e: Exception) {
                Log.e("BIOAPP", "Failed to load blog posts", e)
                errorMessage = "Failed to load posts. Please try again."
                isLoading = false
            }
        }
    }

    // Load posts when the tab is first opened
    LaunchedEffect(Unit) {
        loadBlogPosts()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_light_background)
            .padding(16.dp)
    ) {
        // Header with refresh button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Community Posts",
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            )

            IconButton(
                onClick = { loadBlogPosts() },
                enabled = !isLoading
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Refresh posts",
                    tint = Color(0xFF5CE65C)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(40.dp),
                            color = Color(0xFF5CE65C)
                        )
                        Text(
                            text = "Loading posts...",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            errorMessage != null -> {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { loadBlogPosts() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF5CE65C)
                        )
                    ) {
                        Text("Retry")
                    }
                }
            }

            blogPosts.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "No posts available",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.Gray
                        )
                        Text(
                            text = "Be the first to create a post!",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(blogPosts) { blogPost ->
                        BlogPostCard(blogPost = blogPost)
                    }
                }
            }
        }
    }
}

@Composable
fun BlogPostCard(blogPost: BlogData) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable {
                // Handle post click - you can navigate to detail screen here
                Log.d("BIOAPP", "Clicked on post: ${blogPost.title}")
            },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Author and Date Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Author Avatar
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF5CE65C)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = blogPost.author.ifBlank { "Anonymous" },
                        fontFamily = Roboto,
                        fontWeight = FontWeight.Medium,
                        fontSize = 14.sp,
                        color = Color.Black
                    )
                    Text(
                        text = blogPost.publishedAt.ifBlank { "Just now" },
                        fontFamily = Roboto,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                }

                // Category Badge
                if (blogPost.category.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .background(
                                Color(0xFF5CE65C).copy(alpha = 0.1f),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = blogPost.category,
                            fontSize = 10.sp,
                            color = Color(0xFF5CE65C),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Post Title
            Text(
                text = blogPost.title,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = Color.Black,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Post Content Preview
            Text(
                text = blogPost.content,
                fontFamily = Roboto,
                fontSize = 14.sp,
                color = Color.Gray,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )

            // Post Image (if available)
            if (blogPost.imageUrl.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(model = blogPost.imageUrl),
                        contentDescription = "Post image",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
fun ImagePickerDialog(
    onDismiss: () -> Unit,
    onOptionSelected: (ImageSelectionOption) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Choose an option") },
        text = { Text("How would you like to select an image?") },
        confirmButton = {
            TextButton(onClick = { onOptionSelected(ImageSelectionOption.Camera) }) {
                Text("Camera")
            }
        },
        dismissButton = {
            TextButton(onClick = { onOptionSelected(ImageSelectionOption.Gallery) }) {
                Text("Gallery")
            }
        }
    )
}

// Function to fetch blog posts from Google Cloud Storage
suspend fun fetchBlogPostsFromGCS(context: Context): List<BlogData> {
    return withContext(Dispatchers.IO) {
        try {
            val storage = getStorageCredentials(context).service
            val bucketName = "bio-guardian-capstone-image".trim()

            // List all blobs in the blogs folder
            val blobs = storage.list(
                bucketName,
                Storage.BlobListOption.prefix("blogs/"),
                Storage.BlobListOption.currentDirectory()
            )

            val blogPosts = mutableListOf<BlogData>()

            for (blob in blobs.iterateAll()) {
                if (blob.name.endsWith(".json")) {
                    try {
                        val content = String(blob.getContent(), Charsets.UTF_8)
                        val jsonObject = JSONObject(content)

                        val blogPost = BlogData(
                            id = jsonObject.optString("id", ""),
                            title = jsonObject.optString("title", ""),
                            content = jsonObject.optString("content", ""),
                            author = jsonObject.optString("author", ""),
                            category = jsonObject.optString("category", ""),
                            imageUrl = jsonObject.optString("imageUrl", ""),
                            publishedAt = jsonObject.optString("publishedAt", "")
                        )

                        blogPosts.add(blogPost)
                    } catch (e: Exception) {
                        Log.e("BIOAPP", "Error parsing blog post: ${blob.name}", e)
                    }
                }
            }

            // Sort by published date (newest first)
            blogPosts.sortedByDescending { it.publishedAt }
        } catch (e: Exception) {
            Log.e("BIOAPP", "Error fetching blog posts", e)
            throw e
        }
    }
}

// Validation function for blog data
private fun validateBlogData(blogData: BlogData): Boolean {
    return blogData.title.isNotBlank() &&
            blogData.content.isNotBlank() &&
            blogData.author.isNotBlank() &&
            blogData.title.length <= 100 &&
            blogData.content.length >= 10
}

// Detailed validation function with specific error messages
private fun getValidationError(blogData: BlogData): String? {
    return when {
        blogData.title.isBlank() -> "Post title is required"
        blogData.title.length > 100 -> "Post title must be 100 characters or less"
        blogData.author.isBlank() -> "Author name is required"
        blogData.content.isBlank() -> "Post content is required"
        blogData.content.length < 10 -> "Post content must be at least 10 characters long"
        else -> null
    }
}

// Function to upload blog to Google Cloud Storage as JSON
suspend fun uploadBlogToGCS(
    context: Context,
    blogData: BlogData,
    blogId: String
) {
    withContext(Dispatchers.IO) {
        try {
            val storage = getStorageCredentials(context).service
            val bucketName = "bio-guardian-capstone-image".trim()

            // Create blog JSON content with image URL
            val currentTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
            val blogJson = """
                {
                    "id": "$blogId",
                    "title": "${blogData.title.replace("\"", "\\\"")}",
                    "content": "${blogData.content.replace("\"", "\\\"").replace("\n", "\\n")}",
                    "author": "${blogData.author.replace("\"", "\\\"")}",
                    "category": "${blogData.category.replace("\"", "\\\"")}",
                    "imageUrl": "${blogData.imageUrl}",
                    "publishedAt": "$currentTime",
                    "type": "blog"
                }
            """.trimIndent()

            val blobId = BlobId.of(bucketName, "blogs/$blogId.json")
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("application/json")
                .setContentDisposition("inline")
                .build()

            // Upload blog as JSON
            storage.create(blobInfo, blogJson.toByteArray(Charsets.UTF_8))
            Log.d("GCS Blog Upload", "Blog uploaded successfully with ID: $blogId")
        } catch (e: Exception) {
            Log.e("GCS Blog Upload", "Blog upload failed", e)
            throw e
        }
    }
}

// Function to compress images
private suspend fun compressImage(context: Context, imageUri: Uri): ByteArray {
    return withContext(Dispatchers.IO) {
        // Get input stream
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalStateException("Could not open input stream")

        // Get bitmap dimensions first
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeStream(inputStream, null, options)
        inputStream.close()

        // Calculate appropriate sample size
        val maxDimension = 1024
        val sampleSize = calculateSampleSize(options.outWidth, options.outHeight, maxDimension)

        // Decode with sampling
        val secondInputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IllegalStateException("Could not open input stream")

        val decodingOptions = BitmapFactory.Options().apply {
            inSampleSize = sampleSize
            inJustDecodeBounds = false
        }

        val bitmap = BitmapFactory.decodeStream(secondInputStream, null, decodingOptions)
        secondInputStream.close()

        if (bitmap == null) {
            throw IllegalStateException("Could not decode bitmap")
        }

        // Compress to JPEG with quality reduction
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)

        // Clean up
        bitmap.recycle()

        return@withContext outputStream.toByteArray()
    }
}

// Helper function to calculate sample size
private fun calculateSampleSize(width: Int, height: Int, targetSize: Int): Int {
    var sampleSize = 1

    if (max(width, height) > targetSize) {
        val halfWidth = width / 2
        val halfHeight = height / 2

        while (halfWidth / sampleSize >= targetSize || halfHeight / sampleSize >= targetSize) {
            sampleSize *= 2
        }
    }

    return sampleSize
}

// Modified upload function to use compressed image
suspend fun uploadImageToGCS(
    context: Context,
    imageUri: Uri,
    imageName: String,
    bucketName: String
) {
    withContext(Dispatchers.IO) {
        try {
            // Compress the image
            val compressedImageData = compressImage(context, imageUri)
            Log.d("GCS Upload", "Original size: ${context.contentResolver.openInputStream(imageUri)?.readBytes()?.size ?: 0} bytes")
            Log.d("GCS Upload", "Compressed size: ${compressedImageData.size} bytes")

            val storage = getStorageCredentials(context).service

            val blobId = BlobId.of(bucketName.trim(), "images/$imageName.jpg")
            val blobInfo = BlobInfo.newBuilder(blobId)
                .setContentType("image/jpeg")
                .setContentDisposition("inline")
                .build()

            // Upload compressed image bytes directly
            storage.create(blobInfo, compressedImageData)
            Log.d("GCS Upload", "Compressed image uploaded successfully")
        } catch (e: Exception) {
            Log.e("GCS Upload", "Upload failed", e)
            throw e // Re-throw to handle in the calling function
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommunityScreenPreview() {
    CommunityScreen(paddingValues = PaddingValues(), rememberNavController())
}