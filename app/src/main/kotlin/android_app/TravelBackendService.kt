package android_app

import kotlin.math.*

class TravelBackendService(
        private val googleApi: GoogleApiService,
        private val weatherApi: WeatherApiService,
        private val googleKey: String,
        private val weatherKey: String
) {

    // === CHỨC NĂNG 2 & 3: TÌM KIẾM, LỌC, SẮP XẾP & CHI TIẾT ===
    fun searchAndFilterPlaces(
            query: String,
            category: String?,
            minRating: Double = 0.0,
            userLat: Double? = null,
            userLng: Double? = null
    ): List<PlaceItem> {
        val response = googleApi.searchPlaces(query, category, googleKey).execute()
        val places = response.body()?.results ?: return emptyList()

        if (userLat != null && userLng != null) {
            places.forEach { item ->
                item.geometry?.location?.let {
                    item.distanceInKm = calculateHaversine(userLat, userLng, it.lat, it.lng)
                }
            }
        }
        return places.filter { it.rating >= minRating }.sortedBy { it.distanceInKm }
    }

    fun getPlaceDetail(
            placeId: String,
            userLat: Double? = null,
            userLng: Double? = null
    ): PlaceDetailItem? {
        val detail =
                googleApi
                        .getPlaceDetail(placeId = placeId, apiKey = googleKey)
                        .execute()
                        .body()
                        ?.result
        if (detail != null && userLat != null && userLng != null) {
            detail.geometry?.location?.let {
                detail.distanceInKm = calculateHaversine(userLat, userLng, it.lat, it.lng)
            }
        }
        detail?.let {
            it.estimatedBudget =
                    when (it.priceLevel) {
                        1 -> "< 100k VNĐ (Bình dân)"
                        2 -> "100k - 300k VNĐ (Trung bình)"
                        3 -> "300k - 1tr VNĐ (Khá)"
                        else -> "> 1tr VNĐ (Cao cấp)"
                    }
        }
        return detail
    }

    // === CHỨC NĂNG 5: RECOMMENDATION ENGINE (ĐỀ XUẤT ĐỊA ĐIỂM) ===
    fun recommendPlaces(
            preferences: List<String>,
            maxBudgetLevel: Int,
            userLat: Double,
            userLng: Double,
            candidatePlaces: List<PlaceItem>
    ): List<PlaceItem> {
        // Thuật toán chấm điểm mức độ phù hợp: Match Preferences + Rating - Khoảng cách
        return candidatePlaces.filter { it.priceLevel <= maxBudgetLevel }.sortedByDescending { place
            ->
            val preferenceMatchScore =
                    place.types.count { pref -> preferences.contains(pref) } * 3.0
            val distance =
                    place.geometry?.location?.let {
                        calculateHaversine(userLat, userLng, it.lat, it.lng)
                    }
                            ?: 5.0
            val ratingScore = place.rating * 2.0
            preferenceMatchScore + ratingScore -
                    (distance * 0.2) // Càng gần và điểm càng cao thì ưu tiên
        }
    }

    // === CHỨC NĂNG 7: SMART ITINERARY (TỰ LẬP LỊCH TRÌNH THEO ĐƯỜNG ĐI NGẮN NHẤT) ===
    fun generateOptimizedItinerary(
            placesToVisit: MutableList<PlaceItem>,
            startLat: Double,
            startLng: Double
    ): List<ItineraryStop> {
        val itinerary = mutableListOf<ItineraryStop>()
        var currentLat = startLat
        var currentLng = startLng
        var step = 1

        val remaining = placesToVisit.toMutableList()
        while (remaining.isNotEmpty()) {
            // Tìm điểm gần nhất tiếp theo (Nearest Neighbor Algorithm)
            val nearest =
                    remaining.minByOrNull {
                        val loc = it.geometry?.location ?: LatLng(0.0, 0.0)
                        calculateHaversine(currentLat, currentLng, loc.lat, loc.lng)
                    }!!

            val dist =
                    nearest.geometry?.location?.let {
                        calculateHaversine(currentLat, currentLng, it.lat, it.lng)
                    }
                            ?: 0.0
            itinerary.add(
                    ItineraryStop(order = step++, place = nearest, distanceFromPreviousKm = dist)
            )

            currentLat = nearest.geometry?.location?.lat ?: currentLat
            currentLng = nearest.geometry?.location?.lng ?: currentLng
            remaining.remove(nearest)
        }
        return itinerary
    }

    // === CHỨC NĂNG 8: QUẢN LÝ & PHÂN BỔ NGÂN SÁCH ===
    fun calculateBudgetAllocation(totalBudget: Double): BudgetAllocation {
        return BudgetAllocation(
                totalBudget = totalBudget,
                hotelBudget = totalBudget * 0.35,
                foodBudget = totalBudget * 0.30,
                transportBudget = totalBudget * 0.20,
                activityBudget = totalBudget * 0.15
        )
    }

    // === CHỨC NĂNG 9: THEO DÕI CHI PHÍ (EXPENSE TRACKING) ===
    fun calculateExpenseStatus(totalBudget: Double, expenses: List<ExpenseItem>): BudgetStatus {
        val totalSpent = expenses.sumOf { it.amount }
        return BudgetStatus(
                budget = totalBudget,
                spent = totalSpent,
                remaining = totalBudget - totalSpent
        )
    }

    // === CHỨC NĂNG 11: DỰ BÁO THỜI TIẾT TẠI ĐỊA ĐIỂM ===
    fun getWeather(lat: Double, lng: Double): WeatherResponse? {
        val response = weatherApi.getWeatherByCoordinates(lat, lng, apiKey = weatherKey).execute()
        return response.body()
    }

    // === CHỨC NĂNG 15: TỔNG HỢP NHẬT KÝ HÀNH TRÌNH (TRAVEL JOURNAL) ===
    fun generateJournalSummary(
            trip: TripPlan,
            visitedPlaces: List<PlaceItem>,
            photos: List<String>,
            expenses: List<ExpenseItem>
    ): JournalSummary {
        val totalCost = expenses.sumOf { it.amount }
        val durationDays = max(1L, (trip.endDate - trip.startDate) / (1000 * 60 * 60 * 24))

        // Tính tổng quãng đường di chuyển giữa các điểm liên tiếp
        var totalDist = 0.0
        for (i in 0 until visitedPlaces.size - 1) {
            val loc1 = visitedPlaces[i].geometry?.location
            val loc2 = visitedPlaces[i + 1].geometry?.location
            if (loc1 != null && loc2 != null) {
                totalDist += calculateHaversine(loc1.lat, loc1.lng, loc2.lat, loc2.lng)
            }
        }

        return JournalSummary(
                tripId = trip.tripId,
                durationDays = durationDays,
                totalPlacesVisited = visitedPlaces.size,
                totalPhotosCount = photos.size,
                totalCost = totalCost,
                totalDistanceKm = Math.round(totalDist * 100.0) / 100.0,
                visitedPlaces = visitedPlaces.map { it.name }
        )
    }

    // Công thức tính khoảng cách bề mặt Trái Đất (km)
    private fun calculateHaversine(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 6371.0
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a =
                sin(dLat / 2).pow(2) +
                        cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) * sin(dLon / 2).pow(2)
        return Math.round(r * 2 * atan2(sqrt(a), sqrt(1 - a)) * 100.0) / 100.0
    }
}
