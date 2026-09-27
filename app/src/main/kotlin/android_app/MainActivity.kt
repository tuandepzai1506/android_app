package android_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import android_app.ui.theme.TravelAppTheme

/**
 * Entry Point chính cho ứng dụng di động Android Native.
 * Kế thừa ComponentActivity, cài đặt Material 3 Theme và khởi tạo ViewModel.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Khởi tạo dịch vụ API, Repository và ViewModel
        val apiService = PlacesApiService.create()
        val apiKey = "YOUR_GOOGLE_PLACES_API_KEY" // Nhập API Key của Google Places API tại đây
        val repository = PlaceRepository(apiService, apiKey)
        val viewModel = PlaceViewModel(repository)

        setContent {
            TravelAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainTravelAppScreen(viewModel = viewModel)
                }
            }
        }
    }
}
