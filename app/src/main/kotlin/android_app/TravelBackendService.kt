package android_app

import com.travel.android_app.BuildConfig
import kotlin.math.*

class TravelBackendService(
    private val placesApi: PlacesApiService = PlacesApiService.create(),
    private val weatherApi: WeatherApiService = WeatherApiService.create(),
    private val weatherKey: String = "" // Nếu dùng OpenWeatherMap có thể đưa vào BuildConfig sau
) {

    // === CHỨC NĂNG TÌM KIẾM ĐỊA ĐIỂM DÙNG GOOGLE MAPS API ===
    suspend fun searchAndFilterPlaces(
        query: String,
        category: String? = null,
        minRating: Double = 0.0,
        userLat: Double? = null,
        userLng: Double? = null
    ): List<PlaceItem> {
        val trimmed = query.trim()
        if (trimmed.isEmpty()) return emptyList()

        return try {
            val locationParam = if (userLat != null && userLng != null) {
                "$userLat,$userLng"
            } else null

            val radiusParam = if (locationParam != null) 5000 else null

            // Gọi Google Places API (Interceptor đã tự động đính kèm MAPS_API_KEY ngầm)
            val response = placesApi.searchPlacesWithGoogle(
                query = trimmed,
                location = locationParam,
                radius = radiusParam
            )

            response.results.mapNotNull { result ->
                val name = result.name
                val lat = result.geometry.location.lat
                val lng = result.geometry.location.lng

                val distKm = if (userLat != null && userLng != null) {
                    calculateHaversine(userLat, userLng, lat, lng)
                } else {
                    0.0
                }

                val hours = "07:30 - 22:30 (Hàng ngày)"
                val dynamicImage = "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800"
                val budget = "50.000đ - 200.000đ"
                val calcRating = result.rating ?: (4.0 + (abs(result.place_id.hashCode()) % 10) / 10.0)

                PlaceItem(
                    placeId = result.place_id,
                    name = name,
                    vicinity = result.formatted_address ?: "Việt Nam",
                    rating = calcRating,
                    priceLevel = result.price_level ?: 2,
                    estimatedBudget = budget,
                    openingHours = hours,
                    description = "Địa điểm nổi bật được tìm thấy qua Google Maps.",
                    imageUrl = dynamicImage,
                    types = result.types ?: emptyList(),
                    geometry = Geometry(location = LatLng(lat, lng)),
                    distanceInKm = distKm
                )
            }.filter { it.rating >= minRating }
                .sortedBy { it.distanceInKm }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    // === CHỨC NĂNG THỜI TIẾT ===
    suspend fun getPlaceWeather(lat: Double, lng: Double): WeatherResponse? {
        return try {
            if (weatherKey.isNotBlank()) {
                weatherApi.getCurrentWeather(lat = lat, lon = lng, apiKey = weatherKey)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        return Math.round(r * c * 100.0) / 100.0
    }
}