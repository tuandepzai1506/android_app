package android_app

import com.google.gson.annotations.SerializedName

// --- GEOAPIFY RESPONSE MODELS ---
data class GeoapifyResponse(
        @SerializedName("features") val features: List<GeoapifyFeature> = emptyList()
)

data class GeoapifyFeature(
        @SerializedName("properties") val properties: GeoapifyProperties? = null
)

data class GeoapifyProperties(
        @SerializedName("place_id") val placeId: String? = null,
        @SerializedName("name") val name: String? = null,
        @SerializedName("formatted") val formatted: String? = null,
        @SerializedName("address_line1") val addressLine1: String? = null,
        @SerializedName("address_line2") val addressLine2: String? = null,
        @SerializedName("city") val city: String? = null,
        @SerializedName("country") val country: String? = null,
        @SerializedName("categories") val categories: List<String> = emptyList(),
        @SerializedName("opening_hours") val openingHours: String? = null,
        @SerializedName("website") val website: String? = null,
        @SerializedName("contact") val contact: GeoapifyContact? = null,
        @SerializedName("distance") val distanceMeters: Double? = null,
        @SerializedName("lat") val lat: Double = 0.0,
        @SerializedName("lon") val lon: Double = 0.0
)

data class GeoapifyContact(
        @SerializedName("phone") val phone: String? = null
)

// --- WEATHER MODELS (OpenWeatherMap) ---
data class WeatherResponse(
        @SerializedName("weather") val weather: List<WeatherDescription> = emptyList(),
        @SerializedName("main") val main: MainWeatherData? = null,
        @SerializedName("name") val cityName: String? = null
)

data class WeatherDescription(
        @SerializedName("description") val description: String,
        @SerializedName("icon") val icon: String
)

data class MainWeatherData(
        @SerializedName("temp") val temp: Double,
        @SerializedName("humidity") val humidity: Int
)

// --- MODEL HIỂN THỊ TRÊN UI ---
data class PlaceItem(
        val placeId: String,
        val name: String,
        val vicinity: String?,
        val rating: Double = 4.5,
        val priceLevel: Int = 2,
        val estimatedBudget: String = "35.000đ - 70.000đ",
        val openingHours: String = "07:30 - 22:30",
        val description: String = "Địa điểm phổ biến, phục vụ chu đáo.",
        val imageUrl: String = "",
        val types: List<String> = emptyList(),
        val geometry: Geometry? = null,
        var distanceInKm: Double = 0.0
)

data class Geometry(val location: LatLng? = null)
data class LatLng(val lat: Double, val lng: Double)