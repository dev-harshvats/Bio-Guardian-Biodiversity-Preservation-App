package com.satyamthakur.bio_guardian.ui.screens.discover.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusManager
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.satyamthakur.bio_guardian.data.entity.MistralResponse
import com.satyamthakur.bio_guardian.ui.theme.Montserrat
import com.satyamthakur.bio_guardian.ui.theme.Roboto
import com.satyamthakur.bio_guardian.ui.theme.accentColor
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_background
import com.satyamthakur.bio_guardian.ui.theme.md_theme_light_tertiaryContainer
import com.satyamthakur.bio_guardian.ui.theme.onAccent
import com.satyamthakur.bio_guardian.ui.viewmodel.search.MistralSearchViewModel
import com.satyamthakur.bio_guardian.ui.viewmodel.search.SearchUiState

@Composable
fun AnimalSearchScreen(
    navController: NavController,
    initialQuery: String = "",
    viewModel: MistralSearchViewModel = hiltViewModel()
) {
    var searchQuery by remember { mutableStateOf(initialQuery) }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    val focusRequester = remember { FocusRequester() }

    // Request focus and show keyboard on screen entry
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    // Handle initial query for search
    LaunchedEffect(initialQuery) {
        if (initialQuery.isNotBlank()) {
            viewModel.searchAnimals(initialQuery)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(md_theme_light_background)
            .systemBarsPadding()
            .pointerInput(Unit) {
                detectTapGestures {
                    keyboardController?.hide()
                    focusManager.clearFocus()
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = accentColor
                    )
                }
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    onSearch = {
                        viewModel.searchAnimals(searchQuery)
                        keyboardController?.hide()
                        focusManager.clearFocus()
                    },
                    focusRequester = focusRequester
                )
            }

            when (val state = viewModel.uiState.collectAsState().value) {
                is SearchUiState.Idle -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Enter an animal name to search",
                        fontFamily = Montserrat,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.DarkGray
                    )
                }
                is SearchUiState.Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = accentColor
                        )
                    }
                }
                is SearchUiState.Success -> {
                    val results = parseSearchResults(state.response)
                    if (results.isEmpty() || results.firstOrNull()?.name == "not found") {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No results found",
                            fontFamily = Montserrat,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        SearchResultsList(results)
                    }
                }
                is SearchUiState.Error -> {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = state.message,
                        fontFamily = Montserrat,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp)
            .shadow(4.dp, RoundedCornerShape(24.dp))
            .clip(RoundedCornerShape(24.dp))
            .focusRequester(focusRequester),
        placeholder = {
            Text(
                text = "Search an Animal",
                color = Color.DarkGray,
                fontFamily = Montserrat,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search Icon",
                tint = accentColor,
                modifier = Modifier.size(24.dp)
            )
        },
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(
            onSearch = {
                onSearch()
                keyboardController?.hide()
                focusManager.clearFocus()
            }
        ),
        singleLine = true,
        shape = RoundedCornerShape(24.dp),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            containerColor = Color(0xFFF5F5F5),
            unfocusedBorderColor = Color.Transparent,
            focusedBorderColor = accentColor,
            cursorColor = onAccent,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black,
            focusedLabelColor = accentColor,
            unfocusedLabelColor = Color.DarkGray
        ),
        textStyle = TextStyle(
            fontFamily = Montserrat,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp
        )
    )
}

@Composable
fun SearchResultsList(results: List<AnimalSearchResult>) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(bottom = 80.dp)
    ) {
        items(results) { result ->
            AnimalSearchResultItem(result)
        }
    }
}

@Composable
fun AnimalSearchResultItem(result: AnimalSearchResult) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Navigate to details screen or handle click */ },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = md_theme_light_tertiaryContainer
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = result.name,
                fontFamily = Montserrat,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = accentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Scientific Name: ${result.scientific_name}",
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Habitat: ${result.habitat}",
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Diet: ${result.diet}",
                fontFamily = Roboto,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = Color.DarkGray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Status: ${result.status}",
                fontFamily = Roboto,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

data class AnimalSearchResult(
    val id: Int,
    val name: String,
    val scientific_name: String,
    val habitat: String,
    val diet: String,
    val status: String
)

fun parseSearchResults(response: MistralResponse): List<AnimalSearchResult> {
    val rawResponse = response.choices.firstOrNull()?.message?.content ?: return emptyList()
    return try {
        val cleanedJson = rawResponse
            .replace("```json", "")
            .replace("```", "")
            .trimIndent()
            .trim()

        val gson = Gson()
        if (cleanedJson.startsWith("[")) {
            val type = object : TypeToken<List<AnimalSearchResult>>() {}.type
            gson.fromJson(cleanedJson, type)
        } else {
            val singleResult = gson.fromJson(cleanedJson, AnimalSearchResult::class.java)
            listOf(singleResult)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}