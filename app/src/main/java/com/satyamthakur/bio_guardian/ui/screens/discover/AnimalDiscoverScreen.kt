package com.satyamthakur.bio_guardian.ui.screens.discover

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.satyamthakur.bio_guardian.R
import com.satyamthakur.bio_guardian.ui.navigation.Endpoints
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.accentColor
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_tertiaryContainer
import java.net.URLEncoder

data class AnimalCategory(
    val name: String,
    val imageRes: Int
)

val animalCategories = listOf(
    AnimalCategory("mammals", R.drawable.mammals),
    AnimalCategory("birds", R.drawable.birds),
    AnimalCategory("amphibians", R.drawable.amphibians),
    AnimalCategory("reptiles", R.drawable.reptiles),
    AnimalCategory("fish", R.drawable.fish)
)

@Composable
fun AnimalDiscoverScreen(
    paddingValues: PaddingValues,
    navController: NavController,
    animalType: String = "mammals"
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(
            start = 12.dp,
            end = 12.dp,
            top = paddingValues.calculateTopPadding() + 16.dp,
            bottom = paddingValues.calculateBottomPadding() + 80.dp
        ),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_light_background)
    ) {
        // Header: "Discover"
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Discover",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = Montserrat,
                color = Color.Black
            )
        }

        // SearchBar
        item(span = { GridItemSpan(2) }) {
            SearchBar(
                navController = navController
            )
        }

        // Section Title: "Find Endangered"
        item(span = { GridItemSpan(2) }) {
            Text(
                text = "Find Endangered",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                fontFamily = Montserrat,
                color = Color.Black
            )
        }

        // Category Items
        itemsIndexed(animalCategories) { _, category ->
            AnimalCategoryItem(category = category) {
                val encodedCategory = URLEncoder.encode(category.name, "UTF-8")
                navController.navigate("${Endpoints.DISCOVER_ENDANGERED}/$encodedCategory")
            }
        }
    }
}

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = accentColor)
            ) {
                navController.navigate(Endpoints.SEARCH_SCREEN)
            }
            .semantics { contentDescription = "Open search screen" },
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(24.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Search an Animal",
                color = Color.DarkGray,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
                style = TextStyle(
                    fontFamily = Montserrat,
                    fontWeight = FontWeight.Medium,
                    fontSize = 16.sp
                ),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun AnimalCategoryItem(category: AnimalCategory, onClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = md_theme_light_tertiaryContainer
        ),
        modifier = Modifier
            .width(180.dp)
            .height(180.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = rememberRipple(color = accentColor)
            ) { onClick() }
            .semantics { contentDescription = "Category: ${category.name}" },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = md_theme_light_tertiaryContainer
                )
            ) {
                Image(
                    painter = painterResource(id = category.imageRes),
                    contentDescription = null, // Handled by parent semantics
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(100.dp)
                        .fillMaxWidth()
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = category.name.replaceFirstChar { it.uppercase() },
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
                color = Color.Black,
                maxLines = 1
            )
        }
    }
}