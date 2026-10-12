package android_app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import android_app.ui.theme.TravelAppTheme
import com.travel.android_app.BuildConfig

class MainActivity : ComponentActivity() {

    // Khởi tạo trình Launcher để hiện popup xin quyền
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.entries.all { it.value }
        if (granted) {
            // Người dùng đã đồng ý cấp quyền, có thể làm mới lại vị trí nếu cần
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. KIỂM TRA VÀ YÊU CẦU QUYỀN VỊ TRÍ KHI KHỞI ĐỘNG
        checkAndRequestLocationPermission()

        // 2. KHỞI TẠO CÁC SERVICE LÕI
        val aiParser = AiQueryParser(geminiApiKey = BuildConfig.GEMINI_API_KEY)
        val budgetService = GeminiBudgetService(geminiApiKey = BuildConfig.GEMINI_API_KEY)
        val tripRepository = TripRepository(budgetService = budgetService)

        // 3. KHỞI TẠO REPOSITORY (Dùng Google Maps API)
        val placeRepository = PlaceRepository(aiParser = aiParser)

        // 4. KHỞI TẠO VIEWMODEL
        val viewModel = PlaceViewModel(repository = placeRepository)

        // 5. GẮN GIAO DIỆN
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

    private fun checkAndRequestLocationPermission() {
        val fineLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocationGranted = ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted || !coarseLocationGranted) {
            // Nếu chưa được cấp quyền, bật popup xin phép người dùng
            requestPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }
}