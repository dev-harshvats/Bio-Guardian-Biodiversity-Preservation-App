package com.satyamthakur.bio_guardian.ui.composables

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.satyamthakur.bio_guardian.R
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.md_theme_dark_onPrimaryContainer
import com.satyamthakur.bio_guardian.ui.theme.md_theme_dark_onTertiaryContainer
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_onTertiary
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_onTertiaryContainer
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_tertiaryContainer

@Composable
fun HeroCard() {

    val context = LocalContext.current
    val intent = remember { Intent(Intent.ACTION_VIEW, Uri.parse("https://www.iucn.org/")) }

    Card(
        modifier = Modifier.padding(horizontal = 16.dp),
        colors = CardDefaults.cardColors(
            containerColor = md_theme_light_tertiaryContainer
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding()
        ) {
            Text(
                text = "Learn and Contribute \n" +
                        "to Conservation",
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = md_theme_light_onTertiaryContainer,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp)
            )
            Row {
                Image(
                    modifier = Modifier
                        .height(150.dp)
                        .padding(top = 10.dp),
                    painter = painterResource(id = R.drawable.leaf),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.weight(1f))
                OutlinedButton(
                    border = BorderStroke(2.dp, md_theme_light_onTertiaryContainer),
                    modifier = Modifier
                        .align(Alignment.Bottom)
                        .padding(end = 20.dp, bottom = 16.dp),
                    onClick = {
                        context.startActivity(intent)
                    }
                ) {
                    Icon(
                        imageVector = Icons.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        modifier = Modifier.size(ButtonDefaults.IconSize),
                        tint = md_theme_light_onTertiaryContainer
                    )
                    Spacer(modifier = Modifier.size(ButtonDefaults.IconSpacing))
                    Text(
                        text = "Get Started".uppercase(),
                        fontFamily = Montserrat,
                        fontSize = 12.sp,
                        color = md_theme_light_onTertiaryContainer
                    )
                }
            }
        }
    }
}

@Preview()
@Composable
fun PrevHero() {
    HeroCard()
}