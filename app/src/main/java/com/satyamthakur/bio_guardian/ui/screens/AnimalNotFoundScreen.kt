package com.satyamthakur.bio_guardian.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.Roboto

@Composable
fun AnimalNotFoundScreen(navController: NavController) {
    // Center the content both vertically and horizontally
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Display a warning icon
        Icon(
            imageVector = Icons.Filled.Warning,
            contentDescription = "Warning Icon",
            modifier = Modifier
                .size(64.dp)
                .padding(bottom = 16.dp),
            tint = Color(0xFFFF9800) // Warning orange color
        )

        // Main heading
        Text(
            text = "Animal Not Found",
            fontSize = 24.sp,
            fontFamily = Montserrat,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        // Explanation text
        Text(
            text = "We couldn't identify any animals in the uploaded image. " +
                    "This might happen if:",
            fontSize = 16.sp,
            fontFamily = Roboto,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp),
            color = Color.Gray
        )

        // Create a card with possible reasons
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                BulletPoint("The image is unclear or blurry")
                BulletPoint("The animal is too far away")
                BulletPoint("The image doesn't contain any animals")
                BulletPoint("The lighting is too dark or too bright")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Try again button
        Button(
            onClick = { /* Handle try again */
                navController.popBackStack()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        ) {
            Text(
                text = "Try Another Photo",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun BulletPoint(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Custom bullet point
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(Color.Gray, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 14.sp,
            fontFamily = Roboto,
            color = Color.DarkGray
        )
    }
}