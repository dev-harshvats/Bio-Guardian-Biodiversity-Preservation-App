package com.satyamthakur.bio_guardian.ui.composables

import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.satyamthakur.bio_guardian.R

@Composable
fun AnimalImageView(image: String) {
    AsyncImage(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp),
        placeholder = painterResource(id = R.drawable.image_place),
        model = image,
        contentScale = ContentScale.Crop,
        contentDescription = null
    )

}