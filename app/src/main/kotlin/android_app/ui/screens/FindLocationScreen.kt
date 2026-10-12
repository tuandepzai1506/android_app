package android_app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage


import android_app.PlaceViewModel
import android_app.PlaceUiEvent
import android_app.PlaceScreenStatus
import android_app.PlaceItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FindLocationScreen(
    viewModel: PlaceViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    
    var searchQuery by remember { mutableStateOf(uiState.searchQuery) }
    var active by remember { mutableStateOf(false) }
    
    val categories = listOf("Nhà hàng", "Khách sạn", "Cảnh đẹp", "Quán cà phê", "Bảo tàng")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tìm địa điểm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = { 
                    active = false
                    viewModel.onEvent(PlaceUiEvent.OnSearchQueryChanged(searchQuery))
                },
                active = active,
                onActiveChange = { active = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                placeholder = { Text("Tìm kiếm thành phố, địa danh...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = null)
                        }
                    }
                }
            ) {
                // Search suggestions can go here
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(5) {
                        ListItem(
                            headlineContent = { Text("Lịch sử tìm kiếm $it") },
                            leadingContent = { Icon(Icons.Default.History, contentDescription = null) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Categories Filter
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(categories) { category ->
                    FilterChip(
                        selected = false,
                        onClick = { /* TODO */ },
                        label = { Text(category) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Result List
            Text(
                text = "Gợi ý cho bạn",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            
            when (val status = uiState.status) {
                is PlaceScreenStatus.Loading -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is PlaceScreenStatus.Error -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = status.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is PlaceScreenStatus.Empty -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(text = "Không tìm thấy địa điểm nào phù hợp.")
                    }
                }
                is PlaceScreenStatus.Success -> {
                    LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(status.places) { place ->
                            LocationResultCard(place = place, onClick = {
                                viewModel.onEvent(PlaceUiEvent.OnPlaceSelected(place))
                            })
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LocationResultCard(place: PlaceItem, onClick: () -> Unit) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp),
        onClick = onClick
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = place.imageUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .width(120.dp)
                    .fillMaxHeight()
            )
            
            Column(
                modifier = Modifier
                    .padding(12.dp)
                    .fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${place.rating} (${(10..500).random()} đánh giá)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                
                Text(
                    text = if (place.distanceInKm > 0) "Cách đây ${place.distanceInKm} km" else place.vicinity ?: "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}
