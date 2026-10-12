package android_app

import com.google.gson.annotations.SerializedName

// 1. Dữ liệu đầu vào từ người dùng (Input)
data class RecommendationRequest(
    val preferences: String,       // Sở thích (vd: "yên tĩnh, view đẹp, lãng mạn")
    val budget: String,            // Ngân sách (vd: "Sinh viên", "Sang trọng", "200k")
    val timeAvailable: String,     // Thời gian (vd: "Buổi tối", "2 tiếng")
    val userLat: Double,           // Vị trí người dùng (Vĩ độ)
    val userLng: Double,           // Vị trí người dùng (Kinh độ)
    val maxDistanceKm: Double,     // Khoảng cách tối đa (vd: 5.0 km)
    val minRating: Double          // Rating tối thiểu (vd: 4.0)
)

// 2. Chiến lược tìm kiếm do Gemini trả về (Output của AI)
data class AiSearchStrategy(
    @SerializedName("keyword") val keyword: String = "",
    @SerializedName("category") val category: String = "tourism,catering",
    @SerializedName("reasoning") val reasoning: String = "" // AI giải thích lý do đề xuất
)

// 3. Kết quả trả về cho UI
data class RecommendedPlace(
    val place: PlaceItem,
    val matchReason: String // Gắn lý do AI khuyên đến đây
)