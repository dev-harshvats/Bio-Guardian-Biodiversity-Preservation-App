package com.satyamthakur.bio_guardian.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.satyamthakur.bio_guardian.ui.composables.AnimalImageView
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.Roboto
import com.satyamthakur.bio_guardian.ui.theme.accentColor
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_onTertiaryContainer
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_tertiaryContainer
import com.satyamthakur.bio_guardian.ui.theme.onAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimalDescriptionScreen(navController: NavController) {

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())


    Scaffold(
        modifier = Modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Animal Info",
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

@Composable
fun AnimalDescriptionScreenBody(paddingValues: PaddingValues) {
    val scrollState = rememberScrollState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_light_background)
            .padding(paddingValues)
            .verticalScroll(scrollState)
    ) {
        AnimalImageView(image = "https://static.scientificamerican.com/blogs/cache/file/CF131687-8C9A-4FAD-982B239120D74750_source.jpg?w=590&h=800&901C7AF1-049F-4C9D-B6F1EDF4E945141A")
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Song Sparrow",
            fontFamily = Montserrat,
            fontSize = 28.sp,
            fontWeight = FontWeight.SemiBold,
            color = md_theme_light_onTertiaryContainer,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(
            text = "Melospiza melodia",
            fontFamily = Roboto,
            fontSize = 16.sp,
            color = md_theme_light_onTertiaryContainer,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Facts",
            fontFamily = Montserrat,
            fontSize = 24.sp,
            fontWeight = FontWeight.SemiBold,
            color = md_theme_light_onTertiaryContainer,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
        )
        Spacer(modifier = Modifier.height(10.dp))
        AnimalFactsCard()

        Spacer(modifier = Modifier.height(30.dp))
        val context = LocalContext.current
        val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://nationalzoo.si.edu/animals/song-sparrow")) }
        Button(
            onClick = {
                context.startActivity(intent)
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = accentColor
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .align(Alignment.End),
        ) {
            Text(
                text = "KNOW MORE",
                color = Color.White,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

    }
}

@Composable
fun AnimalFactsCard() {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = md_theme_light_tertiaryContainer
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            AnimalFactItem(fact = "Song sparrows' feathers change depending on location!")
            Spacer(modifier = Modifier.height(10.dp))
            AnimalFactItem(fact = "They sing up to 20 melodies, sweet or fierce!")
            Spacer(modifier = Modifier.height(10.dp))
            AnimalFactItem(fact = "They hide their nests with fur and even snake skin!")
        }
    }
}

@Composable
fun AnimalFactItem(fact: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(onAccent)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = fact,
            fontFamily = Roboto,
            fontSize = 16.sp,
            lineHeight = 24.sp,
            modifier = Modifier.offset(y = -7.dp)
        )
    }
}


@Preview(showBackground = true)
@Composable
fun AnimalDescPrev() {
    val navController = rememberNavController()
    AnimalDescriptionScreen(navController = navController)
}


