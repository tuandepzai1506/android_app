package android_app

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RecommendationEngine(
    private val geminiApiKey: String,
    private val geminiApi: GeminiApiService = GeminiApiService.create(),
    private val placeRepository: PlaceRepository
) {
    private val gson = Gson()

    suspend fun getRecommendations(request: RecommendationRequest): List<RecommendedPlace> = withContext(Dispatchers.IO) {
        // 1. Gọi Gemini AI để chuyển đổi Sở thích -> Chiến lược tìm kiếm
        val strategy = analyzeUserPreferences(request.preferences, request.budget, request.timeAvailable)
        println(">>> [AI STRATEGY] Cần tìm: '${strategy.keyword}', Danh mục: '${strategy.category}'. Lý do: ${strategy.reasoning}")

        // 2. Gọi Geoapify (thông qua PlaceRepository) để tìm địa điểm thực tế
        // Vì PlaceRepository.fetchPlaces sử dụng AI nội bộ, ta truyền keyword thẳng vào
        val rawPlaces = placeRepository.fetchPlaces(
            keyword = strategy.keyword,
            userLat = request.userLat,
            userLng = request.userLng
        )

        // 3. Lọc bằng code Kotlin (Rating & Khoảng cách)
        val filteredPlaces = rawPlaces.filter { place ->
            val dist = place.distanceInKm ?: 0.0
            dist <= request.maxDistanceKm && place.rating >= request.minRating
        }

        // 4. Map sang định dạng RecommendedPlace kèm lý do của AI
        filteredPlaces.map { place ->
            RecommendedPlace(
                place = place,
                matchReason = strategy.reasoning
            )
        }
    }

    private suspend fun analyzeUserPreferences(
        preferences: String,
        budget: String,
        time: String
    ): AiSearchStrategy {
        val prompt = """
            Bạn là một AI gợi ý địa điểm du lịch thông minh.
            Người dùng đang muốn tìm địa điểm với các tiêu chí:
            - Sở thích/Mood: "$preferences"
            - Ngân sách: "$budget"
            - Thời gian có sẵn: "$time"

            Hãy phân tích và trả về DUY NHẤT một chuỗi JSON hợp lệ (không markdown) với cấu trúc:
            {
               "keyword": "1-2 từ khóa cốt lõi nhất để search (vd: 'cà phê', 'công viên', 'nhà hàng', 'chùa', 'bar', 'bảo tàng')",
               "category": "Chọn 1 trong các mục Geoapify sau: catering.cafe, catering.restaurant, catering.bar, tourism.sights, entertainment, commercial.shopping, accommodation",
               "reasoning": "1 câu ngắn gọn (dưới 15 chữ) giải thích vì sao loại địa điểm này hợp với họ (vd: 'Không gian yên tĩnh, giá sinh viên hợp lý')"
            }
        """.trimIndent()

        return try {
            val requestBody = GeminiRequest(
                contents = listOf(GeminiContent(parts = listOf(GeminiPart(text = prompt))))
            )
            val response = geminiApi.generateContent(apiKey = geminiApiKey, request = requestBody)
            val rawText = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""

            val cleanJson = rawText.replace("```json", "").replace("```", "").trim()
            gson.fromJson(cleanJson, AiSearchStrategy::class.java) ?: fallbackStrategy(preferences)
        } catch (e: Exception) {
            fallbackStrategy(preferences)
        }
    }

    private fun fallbackStrategy(pref: String): AiSearchStrategy {
        return AiSearchStrategy(
            keyword = "cafe",
            category = "catering.cafe",
            reasoning = "Phù hợp để thư giãn nhẹ nhàng."
        )
    }
}