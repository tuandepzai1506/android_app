package android_app

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android_app.ui.theme.TravelAppTheme

/**
 * Màn hình Khám phá & Tìm kiếm Địa điểm Du lịch.
 * Tách biệt hoàn toàn Stateless Composable UI và State/ViewModel theo Clean Architecture (UDF).
 */

@Composable
fun PlaceDiscoveryRoute(
    viewModel: PlaceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    PlaceDiscoveryScreen(
        uiState = uiState,
        onEvent = viewModel::onEvent,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDiscoveryScreen(
    uiState: PlaceUiState,
    onEvent: (PlaceUiEvent) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Khám Phá Địa Điểm",
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleLarge
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. Ô Nhập Từ Khóa Tìm Kiếm (Search Field)
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { onEvent(PlaceUiEvent.OnSearchQueryChanged(it)) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text("Tìm địa điểm, khách sạn, nhà hàng, cafe...") },
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = "Search Icon")
                },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { onEvent(PlaceUiEvent.OnSearchQueryChanged("")) }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear search")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                )
            )

            // 2. Thanh Danh Mục Địa Điểm (Category Chips)
            CategoryChipRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = { category ->
                    onEvent(PlaceUiEvent.OnCategorySelected(category))
                }
            )

            // 3. Thanh Sắp Xếp & Bộ Lọc Nâng Cao (Sort & Filter)
            FilterAndSortBar(
                currentSortType = uiState.sortType,
                minRating = uiState.minRating,
                maxPriceLevel = uiState.maxPriceLevel,
                onSortTypeChanged = { sortType ->
                    onEvent(PlaceUiEvent.OnSortTypeChanged(sortType))
                },
                onFilterRatingChanged = { rating ->
                    onEvent(PlaceUiEvent.OnFilterRatingChanged(rating))
                },
                onFilterPriceChanged = { price ->
                    onEvent(PlaceUiEvent.OnFilterPriceChanged(price))
                },
                onClearFilters = {
                    onEvent(PlaceUiEvent.OnClearFilters)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Nội dung chính xử lý 4 Trạng thái (Loading, Success, Empty, Error)
            Crossfade(
                targetState = uiState.status,
                label = "ScreenStatusTransition",
                modifier = Modifier.weight(1f)
            ) { status ->
                when (status) {
                    is PlaceScreenStatus.Loading -> {
                        LoadingStateView()
                    }

                    is PlaceScreenStatus.Empty -> {
                        EmptyStateView(
                            onClearFilters = { onEvent(PlaceUiEvent.OnClearFilters) }
                        )
                    }

                    is PlaceScreenStatus.Error -> {
                        ErrorStateView(
                            errorMessage = status.message,
                            onRetry = { onEvent(PlaceUiEvent.OnRetry) }
                        )
                    }

                    is PlaceScreenStatus.Success -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = status.places,
                                key = { it.placeId }
                            ) { place ->
                                val isFav = uiState.favoritePlaceIds.contains(place.placeId)
                                PlaceCard(
                                    place = place,
                                    isFavorite = isFav,
                                    onToggleFavorite = {
                                        onEvent(PlaceUiEvent.OnToggleFavorite(place.placeId))
                                    },
                                    onClick = {
                                        onEvent(PlaceUiEvent.OnPlaceSelected(place))
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. BottomSheet Chi Tiết khi chọn một Địa điểm
        uiState.selectedPlace?.let { selectedPlace ->
            val isFav = uiState.favoritePlaceIds.contains(selectedPlace.placeId)
            PlaceDetailBottomSheet(
                place = selectedPlace,
                isFavorite = isFav,
                onToggleFavorite = {
                    onEvent(PlaceUiEvent.OnToggleFavorite(selectedPlace.placeId))
                },
                onDismissRequest = {
                    onEvent(PlaceUiEvent.OnPlaceSelected(null))
                }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// SCREEN PREVIEWS (LIGHT & DARK MODE)
// ---------------------------------------------------------------------------

private val mockPlaces = listOf(
    PlaceItem(
        placeId = "1",
        name = "Bưu điện Trung tâm Sài Gòn",
        rating = 4.7,
        priceLevel = 1,
        vicinity = "2 Công xã Paris, Bến Nghé, Quận 1",
        types = listOf("tourist_attraction")
    ).apply { distanceInKm = 0.8 },
    PlaceItem(
        placeId = "2",
        name = "Nhà hát Thành phố Hồ Chí Minh",
        rating = 4.6,
        priceLevel = 2,
        vicinity = "7 Công Trường Lam Sơn, Bến Nghé, Quận 1",
        types = listOf("tourist_attraction")
    ).apply { distanceInKm = 1.1 },
    PlaceItem(
        placeId = "3",
        name = "Dinh Độc Lập",
        rating = 4.8,
        priceLevel = 2,
        vicinity = "135 Nam Kỳ Khởi Nghĩa, Bến Thành, Quận 1",
        types = listOf("tourist_attraction")
    ).apply { distanceInKm = 1.5 }
)

@Preview(name = "Discovery Screen - Success (Light Mode)", showBackground = true)
@Composable
fun PlaceDiscoveryScreenSuccessLightPreview() {
    TravelAppTheme(darkTheme = false) {
        PlaceDiscoveryScreen(
            uiState = PlaceUiState(
                searchQuery = "Sài Gòn",
                status = PlaceScreenStatus.Success(mockPlaces),
                favoritePlaceIds = setOf("1")
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Discovery Screen - Success (Dark Mode)", showBackground = true)
@Composable
fun PlaceDiscoveryScreenSuccessDarkPreview() {
    TravelAppTheme(darkTheme = true) {
        PlaceDiscoveryScreen(
            uiState = PlaceUiState(
                searchQuery = "Sài Gòn",
                status = PlaceScreenStatus.Success(mockPlaces),
                favoritePlaceIds = setOf("1")
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Discovery Screen - Loading State", showBackground = true)
@Composable
fun PlaceDiscoveryScreenLoadingPreview() {
    TravelAppTheme {
        PlaceDiscoveryScreen(
            uiState = PlaceUiState(
                status = PlaceScreenStatus.Loading
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Discovery Screen - Empty State", showBackground = true)
@Composable
fun PlaceDiscoveryScreenEmptyPreview() {
    TravelAppTheme {
        PlaceDiscoveryScreen(
            uiState = PlaceUiState(
                searchQuery = "Địa điểm không tồn tại",
                status = PlaceScreenStatus.Empty
            ),
            onEvent = {}
        )
    }
}

@Preview(name = "Discovery Screen - Error State", showBackground = true)
@Composable
fun PlaceDiscoveryScreenErrorPreview() {
    TravelAppTheme {
        PlaceDiscoveryScreen(
            uiState = PlaceUiState(
                status = PlaceScreenStatus.Error("Không thể kết nối đến máy chủ API Google Places.")
            ),
            onEvent = {}
        )
    }
}
