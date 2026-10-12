package android_app

import com.travel.android_app.BuildConfig
import kotlin.math.*

class PlaceRepository(
    private val apiService: PlacesApiService = PlacesApiService.create(),
    private val aiParser: AiQueryParser = AiQueryParser(geminiApiKey = BuildConfig.GEMINI_API_KEY)
) {

    enum class SortType { DISTANCE, RATING_DESC, PRICE_ASC }

    suspend fun fetchPlaces(
        keyword: String,
        categoryType: String? = null,
        userLat: Double? = null,
        userLng: Double? = null
    ): List<PlaceItem> {
        val trimmed = keyword.trim()
        if (trimmed.isEmpty()) return emptyList()

        return try {
            // 1. Phân tích tự nhiên bằng Gemini AI
            val aiResult = aiParser.parseUserPrompt(trimmed)
            println(">>> [AI Phân tích] Brand: '${aiResult.cleanName}', Category: '${aiResult.category}', LocationHint: '${aiResult.locationHint}'")

            // 2. Chuẩn bị tham số tìm kiếm cho Google Places API
            val searchQuery = listOfNotNull(aiResult.cleanName, aiResult.category)
                .joinToString(" ")
                .ifBlank { trimmed }

            val locationParam = if (userLat != null && userLng != null) {
                "$userLat,$userLng"
            } else null

            val radiusParam = if (locationParam != null) 5000 else null

            // 3. Gọi Google Places API (Không cần truyền API Key vì Interceptor đã tự động đính kèm)
            val response = apiService.searchPlacesWithGoogle(
                query = searchQuery,
                location = locationParam,
                radius = radiusParam
            )
            println(">>> [Google Maps API] Trạng thái: ${response.status}, Số kết quả: ${response.results.size}")
            if (response.status != "OK" && response.status != "ZERO_RESULTS") {
                 println(">>> [Google Maps API Lỗi] Có thể API Key chưa kích hoạt Places API hoặc bị giới hạn.")
            }

            // 4. Map dữ liệu Google sang PlaceItem của ứng dụng
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
                val isCafe = aiResult.category.contains("cafe", ignoreCase = true)
                val budget = if (isCafe) "30.000đ - 65.000đ" else "60.000đ - 250.000đ"
                val calcRating = result.rating ?: (4.0 + (abs(result.place_id.hashCode()) % 10) / 10.0)

                // Lấy ảnh fallback an toàn
                val fallbackImageUrl = getFallbackImage(aiResult.category, result.place_id.hashCode())

                PlaceItem(
                    placeId = result.place_id,
                    name = name,
                    vicinity = result.formatted_address ?: "Việt Nam",
                    rating = calcRating,
                    priceLevel = result.price_level ?: 2,
                    estimatedBudget = budget,
                    openingHours = hours,
                    description = "Địa điểm nổi bật trên Google Maps.",
                    imageUrl = fallbackImageUrl,
                    types = result.types ?: emptyList(),
                    geometry = Geometry(location = LatLng(lat, lng)),
                    distanceInKm = distKm
                )
            }.sortedBy { it.distanceInKm }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    private fun getFallbackImage(category: String, seed: Int): String {
        val fallbackImages = if (category.contains("cafe", ignoreCase = true)) {
            listOf(
                "https://images.unsplash.com/photo-1554118811-1e0d58224f24?w=800",
                "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=800"
            )
        } else {
            listOf(
                "https://images.unsplash.com/photo-1517248135467-4c7edcad34c4?w=800",
                "https://images.unsplash.com/photo-1552566626-52f8b828add9?w=800"
            )
        }
        return fallbackImages[abs(seed) % fallbackImages.size]
    }

    fun calculateDistances(list: List<PlaceItem>, userLat: Double?, userLng: Double?) {
        if (userLat == null || userLng == null) return
        for (item in list) {
            val loc = item.geometry?.location
            if (loc != null) {
                item.distanceInKm = calculateHaversine(userLat, userLng, loc.lat, loc.lng)
            }
        }
    }

    fun filterPlaces(list: List<PlaceItem>, minRating: Double = 0.0, maxPriceLevel: Int = 4): List<PlaceItem> {
        return list.filter { it.rating >= minRating && it.priceLevel <= maxPriceLevel }
    }

    fun sortPlaces(list: List<PlaceItem>, sortBy: SortType): List<PlaceItem> {
        return when (sortBy) {
            SortType.DISTANCE -> list.sortedBy { it.distanceInKm }
            SortType.RATING_DESC -> list.sortedByDescending { it.rating }
            SortType.PRICE_ASC -> list.sortedBy { it.priceLevel }
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