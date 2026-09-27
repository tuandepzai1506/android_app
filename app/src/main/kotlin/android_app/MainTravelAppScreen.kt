package android_app

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import android_app.ui.theme.TravelAppTheme

/**
 * Danh sách các Tab điều hướng của Ứng dụng Du lịch
 */
enum class TravelTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    EXPLORE("Khám phá", Icons.Filled.Explore, Icons.Outlined.Explore),
    FAVORITES("Yêu thích", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    TRIPS("Chuyến đi", Icons.Filled.Luggage, Icons.Outlined.Luggage),
    PROFILE("Tài khoản", Icons.Filled.Person, Icons.Outlined.Person)
}

/**
 * Shell chính cho ứng dụng du lịch chứa Bottom Navigation Bar.
 */
@Composable
fun MainTravelAppScreen(
    viewModel: PlaceViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(TravelTab.EXPLORE) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                tonalElevation = 8.dp
            ) {
                TravelTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        label = { Text(tab.title) },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title
                            )
                        }
                    )
                }
            }
        },
        modifier = modifier
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedTab) {
                TravelTab.EXPLORE -> {
                    PlaceDiscoveryRoute(viewModel = viewModel)
                }
                TravelTab.FAVORITES -> {
                    FavoritesTabScreen(viewModel = viewModel)
                }
                TravelTab.TRIPS -> {
                    TripsTabScreen()
                }
                TravelTab.PROFILE -> {
                    ProfileTabScreen()
                }
            }
        }
    }
}

/**
 * Tab Danh sách Địa điểm Yêu thích
 */
@Composable
fun FavoritesTabScreen(
    viewModel: PlaceViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Địa Điểm Đã Lưu (${uiState.favoritePlaceIds.size})",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (uiState.favoritePlaceIds.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.outline
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Chưa có địa điểm yêu thích nào",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Nhấn biểu tượng trái tim để lưu lại địa điểm bạn muốn ghé thăm.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        } else {
            val favPlaces = when (val s = uiState.status) {
                is PlaceScreenStatus.Success -> s.places.filter { uiState.favoritePlaceIds.contains(it.placeId) }
                else -> emptyList()
            }

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = favPlaces,
                    key = { it.placeId }
                ) { place ->
                    PlaceCard(
                        place = place,
                        isFavorite = true,
                        onToggleFavorite = { viewModel.onEvent(PlaceUiEvent.OnToggleFavorite(place.placeId)) },
                        onClick = { viewModel.onEvent(PlaceUiEvent.OnPlaceSelected(place)) }
                    )
                }
            }
        }
    }
}

/**
 * Tab Quản lý Chuyến đi
 */
@Composable
fun TripsTabScreen(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = Icons.Default.Luggage,
                contentDescription = null,
                modifier = Modifier.size(72.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Quản Lý Chuyến Đi",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Lên kế hoạch cho các hành trình sắp tới và theo dõi lịch trình của bạn.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { /* Tạo chuyến đi mới */ }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Tạo chuyến đi mới")
            }
        }
    }
}

/**
 * Tab Tài Khoản
 */
@Composable
fun ProfileTabScreen(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Surface(
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(96.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Người Dùng Du Lịch",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "user@travel.com",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        OutlinedCard(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Cài đặt ứng dụng", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Ngôn ngữ: Tiếng Việt", style = MaterialTheme.typography.bodySmall)
                Text(text = "Đơn vị tiền tệ: VND (₫)", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// PREVIEWS
// ---------------------------------------------------------------------------

@Preview(name = "Main Screen - Light Theme", showBackground = true)
@Composable
fun MainTravelAppScreenLightPreview() {
    val sampleRepo = PlaceRepository(PlacesApiService.create(), "")
    val sampleVM = PlaceViewModel(sampleRepo)
    TravelAppTheme(darkTheme = false) {
        MainTravelAppScreen(viewModel = sampleVM)
    }
}

@Preview(name = "Main Screen - Dark Theme", showBackground = true)
@Composable
fun MainTravelAppScreenDarkPreview() {
    val sampleRepo = PlaceRepository(PlacesApiService.create(), "")
    val sampleVM = PlaceViewModel(sampleRepo)
    TravelAppTheme(darkTheme = true) {
        MainTravelAppScreen(viewModel = sampleVM)
    }
}
