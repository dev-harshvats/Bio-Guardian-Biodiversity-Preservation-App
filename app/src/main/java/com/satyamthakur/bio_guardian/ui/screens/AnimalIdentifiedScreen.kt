package com.satyamthakur.bio_guardian.ui.screens

import MistralScreen
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_onTertiaryContainer

@Composable
fun AnimalIdentifiedScreen(
    navController: NavController,
    imageUrl: String = "https://cdn.download.ams.birds.cornell.edu/api/v2/asset/612763581/900",
    animalName: String?
) {

        if (animalName != "") {
            MistralScreen(imageUrl = imageUrl, animalName!!, navController)
        } else {
            MistralScreen(imageUrl = imageUrl, "", navController)
        }

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDetails(navController: NavController) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Song Sparrow",
                        fontFamily = Montserrat,
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = md_theme_light_background,
                    titleContentColor = md_theme_light_onTertiaryContainer,
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Localized description"
                        )
                    }
                },
                scrollBehavior = scrollBehavior
            )
        },
    ) { paddingValues ->
        AnimalDescriptionScreenBody(paddingValues = paddingValues)
    }
}

@Preview
@Composable
fun AnimalDetailsPrev() {
    AnimalDetails(rememberNavController())
}
