package com.satyamthakur.bio_guardian.ui.composables

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.satyamthakur.bio_guardian.R
import com.satyamthakur.bio_guardian.ui.model.Animal
import com.satyamthakur.bio_guardian.ui.data.animalsListData
import com.satyamthakur.bio_guardian.ui.navigation.Endpoints
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.Roboto
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_onTertiaryContainer
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_tertiaryContainer
import retrofit2.http.Url
import java.net.URL
import java.net.URLEncoder

@Composable
fun EndangeredNowSection(navController: NavController) {
    Column() {
        EndangeredNowSectionHeading()
        Spacer(modifier = Modifier.height(14.dp))
        EndangeredAnimalsList(navController)
    }
}

@Composable
fun EndangeredAnimalsList(navController: NavController) {
    LazyRow(
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(animalsListData) { animal ->
            EndangeredAnimalItemCard(animal = animal, navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EndangeredAnimalItemCard(
    animal: Animal,
    navController: NavController
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = md_theme_light_tertiaryContainer
        ),
        modifier = Modifier,
        onClick = {
            val penguin = animal.imageLink
            val encodedPenguin = URLEncoder.encode(penguin, "UTF-8")
            val encodedAnimalName = URLEncoder.encode(animal.name, "UTF-8")
            navController.navigate("${Endpoints.ANIMAL_DESC}/$encodedPenguin/$encodedAnimalName")
        }
    ) {
        Column(
            modifier = Modifier
                .width(180.dp)
                .height(215.dp)
                .padding(16.dp),
        ) {
            Card {
                AsyncImage(
                    placeholder = painterResource(id = R.drawable.image_place),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(animal.imageLink)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.height(100.dp),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = animal.name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp
            )
            Text(
                text = animal.bioName,
                fontFamily = Roboto,
                fontSize = 12.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun EndangeredNowSectionHeading() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Endangered Now",
            fontFamily = Montserrat,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = md_theme_light_onTertiaryContainer
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EndangeredNowPreview() {
    val navController = rememberNavController()
    EndangeredNowSection(navController)
}