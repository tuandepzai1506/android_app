package android_app

import com.google.gson.annotations.SerializedName

// --- 1. User & Auth ---
data class UserProfile(
        val uid: String,
        val email: String,
        val displayName: String = "",
        val preferences: List<String> = emptyList() // thiên nhiên, ăn uống, mua sắm...
)

// --- 2 & 3. Places API Models ---
data class PlacesSearchResponse(
        @SerializedName("results") val results: List<PlaceItem>,
        @SerializedName("status") val status: String
)

data class PlaceDetailResponse(
        @SerializedName("result") val result: PlaceDetailItem?,
        @SerializedName("status") val status: String
)

data class PlaceItem(
        @SerializedName("place_id") val placeId: String,
        @SerializedName("name") val name: String,
        @SerializedName("rating") val rating: Double = 0.0,
        @SerializedName("price_level") val priceLevel: Int = 1,
        @SerializedName("types") val types: List<String> = emptyList(),
        @SerializedName("geometry") val geometry: Geometry? = null
) {
    var distanceInKm: Double = 0.0
}

data class PlaceDetailItem(
        @SerializedName("place_id") val placeId: String,
        @SerializedName("name") val name: String,
        @SerializedName("rating") val rating: Double = 0.0,
        @SerializedName("formatted_address") val formattedAddress: String? = null,
        @SerializedName("price_level") val priceLevel: Int = 1,
        @SerializedName("geometry") val geometry: Geometry? = null,
        @SerializedName("opening_hours") val openingHours: OpeningHours? = null,
        @SerializedName("photos") val photos: List<PlacePhoto>? = null,
        @SerializedName("reviews") val reviews: List<PlaceReview>? = null
) {
    var distanceInKm: Double = 0.0
    var estimatedBudget: String = ""
}

data class Geometry(@SerializedName("location") val location: LatLng)

data class LatLng(@SerializedName("lat") val lat: Double, @SerializedName("lng") val lng: Double)

data class OpeningHours(@SerializedName("weekday_text") val weekdayText: List<String>? = null)

data class PlacePhoto(@SerializedName("photo_reference") val photoReference: String)

data class PlaceReview(
        @SerializedName("author_name") val authorName: String,
        @SerializedName("rating") val rating: Double,
        @SerializedName("text") val text: String
)

// --- 6, 7, 8, 9. Trip, Budget & Itinerary ---
data class TripPlan(
        val tripId: String,
        val title: String,
        val destination: String,
        val startDate: Long,
        val endDate: Long,
        val totalBudget: Double,
        val memberCount: Int
)

data class BudgetAllocation(
        val totalBudget: Double,
        val hotelBudget: Double, // 35%
        val foodBudget: Double, // 30%
        val transportBudget: Double, // 20%
        val activityBudget: Double // 15%
)

data class ExpenseItem(
        val id: String,
        val category: String, // "food", "hotel", "transport", "other"
        val amount: Double,
        val timestamp: Long
)

data class BudgetStatus(val budget: Double, val spent: Double, val remaining: Double)

data class ItineraryStop(
        val order: Int,
        val place: PlaceItem,
        val estimatedVisitDurationMinutes: Int = 90,
        val distanceFromPreviousKm: Double = 0.0
)

// --- 11. Weather API Models ---
data class WeatherResponse(
        @SerializedName("main") val main: WeatherMain,
        @SerializedName("weather") val weather: List<WeatherDescription>,
        @SerializedName("name") val cityName: String
)

data class WeatherMain(
        @SerializedName("temp") val temp: Double,
        @SerializedName("humidity") val humidity: Int
)

data class WeatherDescription(
        @SerializedName("description") val description: String,
        @SerializedName("icon") val icon: String
)

// --- 12. Check-in ---
data class CheckInPost(
        val id: String,
        val userId: String,
        val placeName: String,
        val photoUrl: String,
        val caption: String,
        val timestamp: Long
)

// --- 13. Note / Checklist ---
data class TripNote(
        val id: String,
        val title: String,
        val content: String,
        val isCompleted: Boolean = false
)

// --- 14. Review / Rating ---
data class UserReview(
        val id: String,
        val placeId: String,
        val userId: String,
        val rating: Double,
        val comment: String,
        val createdAt: Long
)

// --- 15. Travel Journal Summary ---
data class JournalSummary(
        val tripId: String,
        val durationDays: Long,
        val totalPlacesVisited: Int,
        val totalPhotosCount: Int,
        val totalCost: Double,
        val totalDistanceKm: Double,
        val visitedPlaces: List<String>
)
