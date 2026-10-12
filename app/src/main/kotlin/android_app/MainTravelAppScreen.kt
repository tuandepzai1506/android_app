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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import android_app.ui.theme.TravelAppTheme
import com.travel.android_app.BuildConfig

/**
- Danh sách các Tab điều hướng của Ứng dụng Du lịch
 */
enum class TravelTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    EXPLORE("Khám phá", Icons.Filled.Explore, Icons.Outlined.Explore),
    FAVORITES("Yêu thích", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    TRIPS("Chuyến đi", Icons.Filled.Luggage, Icons.Outlined.Luggage),
    MENU("Menu", Icons.Filled.Menu, Icons.Outlined.Menu)
}

enum class NavigationRoute {
    HOME,
    LOGIN,
    REGISTER,
    FIND_LOCATION,
    CREATE_TRIP,
    CREATE_ITINERARY,
    VIEW_ITINERARY
}

/**
 * Shell chính cho ứng dụng du lịch chứa Bottom Navigation Bar.
 */
@Composable
fun MainTravelAppScreen(
    placeViewModel: PlaceViewModel,
    tripViewModel: TripViewModel,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(TravelTab.EXPLORE) }
    var currentRoute by remember { mutableStateOf(NavigationRoute.LOGIN) } // Start at login for demo

    // If we are not on the home route (tabs), show the full screen sub-routes
    if (currentRoute != NavigationRoute.HOME) {
        when (currentRoute) {
            NavigationRoute.LOGIN -> {
                android_app.ui.screens.LoginScreen(
                    onNavigateToHome = { currentRoute = NavigationRoute.HOME },
                    onNavigateToRegister = { currentRoute = NavigationRoute.REGISTER }
                )
            }
            NavigationRoute.REGISTER -> {
                android_app.ui.screens.RegisterScreen(
                    onNavigateToLogin = { currentRoute = NavigationRoute.LOGIN }
                )
            }
            NavigationRoute.FIND_LOCATION -> {
                android_app.ui.screens.FindLocationScreen(
                    viewModel = placeViewModel,
                    onBack = { currentRoute = NavigationRoute.HOME }
                )
            }
            NavigationRoute.CREATE_TRIP -> {
                android_app.ui.screens.CreateTripScreen(
                    viewModel = tripViewModel,
                    onBack = { currentRoute = NavigationRoute.HOME }
                )
            }
            NavigationRoute.CREATE_ITINERARY -> {
                android_app.ui.screens.CreateItineraryScreen(onBack = { currentRoute = NavigationRoute.HOME })
            }
            NavigationRoute.VIEW_ITINERARY -> {
                android_app.ui.screens.ViewItineraryScreen(
                    viewModel = tripViewModel,
                    onBack = { currentRoute = NavigationRoute.HOME }
                )
            }
            else -> {}
        }
        return // Return early to not show Scaffold with BottomNav
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.height(72.dp)
            ) {
                TravelTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clickable { selectedTab = tab },
                        contentAlignment = Alignment.Center
                    ) {
                        // Top Border for Active Tab
                        if (isSelected) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .fillMaxWidth()
                                    .height(3.dp)
                                    .padding(horizontal = 16.dp)
                                    .background(MaterialTheme.colorScheme.error) // Red border like sample
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tab.title,
                                tint = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
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
                    PlaceDiscoveryRoute(viewModel = placeViewModel)
                }
                TravelTab.FAVORITES -> {
                    FavoritesTabScreen(viewModel = placeViewModel)
                }
                TravelTab.TRIPS -> {
                    TripsTabScreen()
                }
                TravelTab.MENU -> {
                    android_app.ui.screens.MenuScreen(
                        onNavigateToFindLocation = { currentRoute = NavigationRoute.FIND_LOCATION },
                        onNavigateToCreateTrip = { currentRoute = NavigationRoute.CREATE_TRIP },
                        onNavigateToCreateItinerary = { currentRoute = NavigationRoute.CREATE_ITINERARY },
                        onNavigateToViewItinerary = { currentRoute = NavigationRoute.VIEW_ITINERARY },
                        onLogout = {
                            selectedTab = TravelTab.EXPLORE // Reset tab
                            currentRoute = NavigationRoute.LOGIN // Navigate to login
                        }
                    )
                }
            }
        }
    }
}

/**
- Tab Danh sách Địa điểm Yêu thích
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
- Tab Quản lý Chuyến đi
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

// ---------------------------------------------------------------------------
// PREVIEWS - CẬP NHẬT CHO GEOAPIFY & GEMINI PARSER
// ---------------------------------------------------------------------------

@Preview(name = "Main Screen - Light Theme", showBackground = true)
@Composable
fun MainTravelAppScreenLightPreview() {
    val aiParser = AiQueryParser(geminiApiKey = BuildConfig.GEMINI_API_KEY)
    val budgetService = GeminiBudgetService(geminiApiKey = BuildConfig.GEMINI_API_KEY)
    val sampleRepo = PlaceRepository(aiParser = aiParser)
    val tripRepo = TripRepository(budgetService = budgetService)
    val placeVM = PlaceViewModel(sampleRepo)
    val tripVM = TripViewModel(tripRepo)
    TravelAppTheme(darkTheme = false) {
        MainTravelAppScreen(placeViewModel = placeVM, tripViewModel = tripVM)
    }
}

@Preview(name = "Main Screen - Dark Theme", showBackground = true)
@Composable
fun MainTravelAppScreenDarkPreview() {
    val aiParser = AiQueryParser(geminiApiKey = BuildConfig.GEMINI_API_KEY)
    val budgetService = GeminiBudgetService(geminiApiKey = BuildConfig.GEMINI_API_KEY)
    val sampleRepo = PlaceRepository(aiParser = aiParser)
    val tripRepo = TripRepository(budgetService = budgetService)
    val placeVM = PlaceViewModel(sampleRepo)
    val tripVM = TripViewModel(tripRepo)
    TravelAppTheme(darkTheme = true) {
        MainTravelAppScreen(placeViewModel = placeVM, tripViewModel = tripVM)
    }
}