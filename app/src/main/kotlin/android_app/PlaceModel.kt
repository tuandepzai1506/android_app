package android_app

/**
 * Model ánh xạ cho UI hiển thị danh sách địa điểm khám phá.
 */
data class PlaceUiModel(
    val id: String,
    val name: String,
    val rating: Double,
    val vicinity: String?,
    val distanceKm: Double,
    val priceLevel: Int
)