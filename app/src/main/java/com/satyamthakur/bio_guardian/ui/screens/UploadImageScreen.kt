package com.satyamthakur.bio_guardian.ui.screens

import android.content.Context
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCameraBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
import androidx.activity.ComponentActivity
import com.satyamthakur.bio_guardian.ui.screens.auth.GoogleSignInManager
import com.satyamthakur.bio_guardian.ui.screens.auth.UserInfo
import com.satyamthakur.bio_guardian.ui.screens.auth.SidebarItem
import com.satyamthakur.bio_guardian.ui.screens.auth.UserSidebar
import com.satyamthakur.bio_guardian.ui.navigation.Endpoints
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.Roboto
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.utils.getStorageCredentials
import com.satyamthakur.bio_guardian.ui.screens.auth.AuthPreferencesManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URLEncoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.math.max

sealed class ImageSelectionOption {
    object Camera : ImageSelectionOption()
    object Gallery : ImageSelectionOption()
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

@Composable
fun GoogleSignInDialog(
    onDismiss: () -> Unit,
    onSignInClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Sign In Required") },
        text = { Text("Please sign in with Google to upload images and access your profile.") },
        confirmButton = {
            TextButton(onClick = onSignInClick) {
                Text("Sign In with Google")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun SessionExpiringDialog(
    onDismiss: () -> Unit,
    onExtendSession: () -> Unit,
    remainingTime: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Session Expiring Soon") },
        text = { Text("Your session will expire in $remainingTime. Would you like to extend your session?") },
        confirmButton = {
            TextButton(onClick = onExtendSession) {
                Text("Extend Session")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not Now")
            }
        }
    )
}

@Composable
fun UploadImageScreen(paddingValues: PaddingValues, navController: NavController) {
    val context = LocalContext.current
    val googleSignInManager = remember { GoogleSignInManager(context) }
    val authPreferencesManager = remember { AuthPreferencesManager(context) }
    val coroutineScope = rememberCoroutineScope()

    var currentUser by remember { mutableStateOf<UserInfo?>(null) }
    var showSignInDialog by remember { mutableStateOf(false) }
    var showSessionExpiringDialog by remember { mutableStateOf(false) }
    var isSigningIn by remember { mutableStateOf(false) }
    var isSidebarOpen by remember { mutableStateOf(false) }
    var isInitializing by remember { mutableStateOf(true) }

    // Check authentication status on screen load
    LaunchedEffect(Unit) {
        try {
            // First check if we have saved user data
            val savedUser = authPreferencesManager.getSavedUserAuth()

            if (savedUser != null) {
                // We have saved user data and session is valid
                currentUser = savedUser
                Log.d("Auth", "Restored user session: ${savedUser.email}")

                // Check if session is expiring soon
                if (authPreferencesManager.isSessionExpiringSoon()) {
                    val remainingTime = formatRemainingTime(authPreferencesManager.getRemainingSessionTime())
                    showSessionExpiringDialog = true
                    Log.d("Auth", "Session expiring soon, showing dialog")
                }
            } else {
                // No valid saved session, check with Google Sign-In Manager
                val currentGoogleUser = googleSignInManager.getCurrentUser()
                if (currentGoogleUser != null) {
                    // User is signed in with Google but not in our preferences
                    currentUser = currentGoogleUser
                    authPreferencesManager.saveUserAuth(currentGoogleUser)
                    Log.d("Auth", "Found Google user, saved to preferences: ${currentGoogleUser.email}")
                } else {
                    // No user signed in at all
                    Log.d("Auth", "No user signed in, showing sign-in dialog")
                }
            }
        } catch (e: Exception) {
            Log.e("Auth", "Error checking authentication status", e)
        } finally {
            isInitializing = false
            // Only show sign-in dialog if no user is found and we're not initializing
            if (currentUser == null) {
                showSignInDialog = true
            }
        }
    }

    // Traditional Google Sign-In launcher
    val traditionalSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        coroutineScope.launch {
            try {
                result.data?.let { data ->
                    val userInfo = googleSignInManager.handleSignInResult(data)
                    if (userInfo != null) {
                        currentUser = userInfo
                        authPreferencesManager.saveUserAuth(userInfo)
                        Log.d("GoogleSignIn", "Traditional sign-in successful and saved: ${userInfo.email}")
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Traditional sign-in failed", e)
            } finally {
                isSigningIn = false
            }
        }
    }

    // One Tap Sign-In launcher
    val oneTapLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        coroutineScope.launch {
            try {
                result.data?.let { data ->
                    val userInfo = googleSignInManager.handleOneTapResult(data)
                    if (userInfo != null) {
                        currentUser = userInfo
                        authPreferencesManager.saveUserAuth(userInfo)
                        isSigningIn = false
                        Log.d("GoogleSignIn", "One Tap sign-in successful and saved: ${userInfo.email}")
                    } else {
                        // Fallback to traditional sign-in
                        traditionalSignInLauncher.launch(googleSignInManager.getSignInIntent())
                    }
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "One Tap sign-in failed", e)
                isSigningIn = false
                // Fallback to traditional sign-in
                traditionalSignInLauncher.launch(googleSignInManager.getSignInIntent())
            }
        }
    }

    // Sign-in function
    fun performSignIn() {
        isSigningIn = true
        coroutineScope.launch {
            try {
                val oneTapRequest = googleSignInManager.beginSignIn()
                if (oneTapRequest != null) {
                    oneTapLauncher.launch(oneTapRequest)
                } else {
                    // Fallback to traditional sign-in
                    traditionalSignInLauncher.launch(googleSignInManager.getSignInIntent())
                }
            } catch (e: Exception) {
                Log.e("GoogleSignIn", "Sign-in initiation failed", e)
                isSigningIn = false
            }
        }
    }

    // Modified sign-out function to clear preferences and close the app
    fun performSignOut() {
        coroutineScope.launch {
            googleSignInManager.signOut()
            authPreferencesManager.clearUserAuth()
            currentUser = null
            Log.d("Auth", "User signed out and preferences cleared")
            // Close the app after sign out
            (context as? ComponentActivity)?.finishAffinity()
        }
    }

    // Extend session function
    fun extendSession() {
        authPreferencesManager.updateSignInTimestamp()
        showSessionExpiringDialog = false
        Log.d("Auth", "Session extended")
    }

    // Custom sidebar items for this screen
    val sidebarItems = listOf(
        SidebarItem("My Images", Icons.Default.PhotoCameraBack) {
            // Navigate to user's uploaded images
            Log.d("Sidebar", "Navigate to My Images")
        }
    )

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(md_theme_light_background)
                .padding(paddingValues)
        ) {
            Spacer(modifier = Modifier.height(10.dp))

            // Header with menu button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "Upload",
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                )

                if (currentUser != null) {
                    IconButton(
                        onClick = { isSidebarOpen = true }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Open sidebar"
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            when {
                isInitializing -> {
                    // Show loading indicator while checking authentication
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                currentUser != null -> {
                    SelectAnImageCardWithHeading(navController, currentUser!!)
                }
                else -> {
                    // Show sign-in required message
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.Gray)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Sign in required",
                                fontFamily = Montserrat,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Please sign in with Google to upload images",
                                fontFamily = Roboto,
                                fontSize = 14.sp,
                                color = Color.Gray
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Button(
                                onClick = { showSignInDialog = true },
                                enabled = !isSigningIn
                            ) {
                                if (isSigningIn) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Text(text = if (isSigningIn) "Signing In..." else "Sign In with Google")
                            }
                        }
                    }
                }
            }
        }

        // Sidebar
        if (isSidebarOpen) {
            UserSidebar(
                userInfo = currentUser,
                isOpen = isSidebarOpen,
                onClose = { isSidebarOpen = false },
                onSignOut = { performSignOut() }
            )
        }

        // Sign-in dialog
        if (showSignInDialog) {
            GoogleSignInDialog(
                onDismiss = { showSignInDialog = false },
                onSignInClick = {
                    showSignInDialog = false
                    performSignIn()
                }
            )
        }

        // Session expiring dialog
        if (showSessionExpiringDialog) {
            val remainingTime = formatRemainingTime(authPreferencesManager.getRemainingSessionTime())
            SessionExpiringDialog(
                onDismiss = { showSessionExpiringDialog = false },
                onExtendSession = { extendSession() },
                remainingTime = remainingTime
            )
        }
    }
}

// Helper function to format remaining time
private fun formatRemainingTime(timeInMillis: Long): String {
    val hours = timeInMillis / (1000 * 60 * 60)
    val minutes = (timeInMillis % (1000 * 60 * 60)) / (1000 * 60)

    return when {
        hours > 0 -> "${hours}h ${minutes}m"
        minutes > 0 -> "${minutes}m"
        else -> "less than 1 minute"
    }
}

// Updated function signature to include user info
@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SelectAnImageCardWithHeading(navController: NavController, userInfo: UserInfo) {
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val coroutineScope = rememberCoroutineScope()

    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var isImageUploading by remember { mutableStateOf(false) }
    var showImagePickerDialog by remember { mutableStateOf(false) }

    // For temporary camera image storage
    val tempImageUri = remember {
        mutableStateOf<Uri?>(null)
    }

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
            }
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

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Text(
            text = "Select an image",
            fontFamily = Roboto,
            fontSize = 16.sp
        )
        Spacer(modifier = Modifier.height(10.dp))

        Card(
            modifier = Modifier
                .width(screenWidth)
                .height(screenWidth - 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            border = BorderStroke(2.dp, Color.Black),
            onClick = { showImagePickerDialog = true }
        ) {
            if (imageUri == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.PhotoCameraBack,
                        contentDescription = null,
                        modifier = Modifier.size(32.dp)
                    )
                }
            } else {
                Image(
                    painter = rememberAsyncImagePainter(model = imageUri),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Show dialog when needed
        if (showImagePickerDialog) {
            ImagePickerDialog(
                onDismiss = { showImagePickerDialog = false },
                onOptionSelected = { option -> handleOptionSelection(option) }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (isImageUploading) {
            CircularProgressIndicator(
                modifier = Modifier
                    .width(40.dp)
                    .align(Alignment.CenterHorizontally)
            )
        }

        if (imageUri != null) {
            Button(
                onClick = {
                    isImageUploading = true
                    val startTime = System.currentTimeMillis()
                    Log.d("UploadTiming", "Upload started at: $startTime")
                    Log.d("UserInfo", "Uploading image for user: ${userInfo.email}")

                    coroutineScope.launch {
                        try {
                            val bucketName = "bio-guardian-capstone-image".trim()
                            val fileName = "${userInfo.uid}_${UUID.randomUUID()}"
                            val imageUrlIfSuccessfullyUploaded = "https://storage.googleapis.com/bio-guardian-capstone-image/images/$fileName.jpg"

                            uploadImageToGCS(context, imageUri!!, fileName, bucketName)
                            Log.d("BIOAPP", imageUrlIfSuccessfullyUploaded)

                            // Reset after successful upload
                            imageUri = null
                            isImageUploading = false

                            val endTime = System.currentTimeMillis()
                            val latency = endTime - startTime
                            Log.d("UploadTiming", "Upload completed at: $endTime, Latency: ${latency}ms")

                            // Navigate to next screen
                            val imageUrl = URLEncoder.encode(imageUrlIfSuccessfullyUploaded, "UTF-8")
                            navController.navigate("${Endpoints.ANIMAL_DESC}/${imageUrl}/${"null"}")
                        } catch (e: Exception) {
                            Log.e("BIOAPP", "Upload process error", e)
                            isImageUploading = false

                            val endTime = System.currentTimeMillis()
                            val latency = endTime - startTime
                            Log.d("UploadTiming", "Upload failed at: $endTime, Latency: ${latency}ms")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = "Upload Now")
            }
        }
    }
}

// New function to compress images
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
fun UploadImagePrev() {
    UploadImageScreen(paddingValues = PaddingValues(), rememberNavController())
}