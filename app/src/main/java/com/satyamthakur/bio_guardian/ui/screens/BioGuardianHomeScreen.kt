package com.satyamthakur.bio_guardian.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.satyamthakur.bio_guardian.ui.composables.EndangeredNowSection
import com.satyamthakur.bio_guardian.ui.composables.ExploreMoreSection
import com.satyamthakur.bio_guardian.ui.composables.HeadingToolbar
import com.satyamthakur.bio_guardian.ui.composables.HeroCard
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background

@Composable
fun BioGuardianAppHomeScreen(paddingValues: PaddingValues, homeScreenNavController: NavController) {
    val scrollState = rememberScrollState()
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = md_theme_light_background
    ) {

    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(paddingValues)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(10.dp))
        HeadingToolbar()
        Spacer(modifier = Modifier.height(20.dp))
        HeroCard()
        Spacer(modifier = Modifier.height(20.dp))
        EndangeredNowSection(homeScreenNavController)
        Spacer(modifier = Modifier.height(20.dp))
        ExploreMoreSection()
        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPrev() {
    val navController = rememberNavController()
    BioGuardianAppHomeScreen(paddingValues = PaddingValues(), navController)
}