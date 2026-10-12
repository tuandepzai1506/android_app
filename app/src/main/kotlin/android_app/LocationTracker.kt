package android_app

import android.annotation.SuppressLint
import android.content.Context
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

class LocationTracker(context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Hàm lấy tọa độ hiện tại.
    // @SuppressLint để bỏ qua cảnh báo IDE vì ta sẽ kiểm tra quyền ở giao diện UI trước khi gọi hàm này.
    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Pair<Double, Double>? {
        return try {
            // Lấy vị trí gần nhất từ điện thoại
            val location = fusedLocationClient.lastLocation.await()
            if (location != null) {
                Pair(location.latitude, location.longitude) // Trả về (Vĩ độ, Kinh độ)
            } else {
                null // Không lấy được vị trí (có thể do tắt GPS)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}