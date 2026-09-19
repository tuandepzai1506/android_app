package android_app

import kotlin.math.*

class PlaceRepository(private val apiService: PlacesApiService, private val apiKey: String) {

    // 1. Hàm gọi API lấy danh sách gốc theo từ khóa & danh mục
    fun fetchPlaces(keyword: String, categoryType: String?): List<PlaceItem> {
        val call = apiService.searchPlaces(query = keyword, type = categoryType, apiKey = apiKey)
        val response = call.execute() // Thực thi gọi mạng đồng bộ để test
        return response.body()?.results ?: emptyList()
    }

    // 2. Logic Lọc (Filter) theo Đánh giá (Rating) & Mức giá (Cost/PriceLevel)
    fun filterPlaces(
        list: List<PlaceItem>,
        minRating: Double = 0.0,
        maxPriceLevel: Int = 4
    ): List<PlaceItem> {
        return list.filter { place ->
            place.rating >= minRating && place.priceLevel <= maxPriceLevel
        }
    }

    // 3. Logic Tính khoảng cách (Distance) từ vị trí người dùng đến địa điểm (công thức Haversine)
    fun calculateDistances(list: List<PlaceItem>, userLat: Double, userLng: Double) {
        for (item in list) {
            val loc = item.geometry?.location
            if (loc != null) {
                item.distanceInKm = calculateHaversine(userLat, userLng, loc.lat, loc.lng)
            }
        }
    }

    // 4. Logic Sắp xếp (Sort): theo khoảng cách, đánh giá hoặc giá tiền
    enum class SortType { DISTANCE, RATING_DESC, PRICE_ASC }

    fun sortPlaces(list: List<PlaceItem>, sortBy: SortType): List<PlaceItem> {
        return when (sortBy) {
            SortType.DISTANCE -> list.sortedBy { it.distanceInKm }
            SortType.RATING_DESC -> list.sortedByDescending { it.rating }
            SortType.PRICE_ASC -> list.sortedBy { it.priceLevel }
        }
    }

    private fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0 // Bán kính Trái Đất (km)
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2) + cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        return Math.round(r * c * 100.0) / 100.0 // Làm tròn 2 chữ số thập phân
    }
}