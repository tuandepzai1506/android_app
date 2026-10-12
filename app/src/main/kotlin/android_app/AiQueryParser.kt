package android_app

import com.google.gson.Gson
import com.google.gson.annotations.SerializedName

data class ParsedPlaceQuery(
    @SerializedName("cleanName") val cleanName: String? = null,
    @SerializedName("category") val category: String = "catering,tourism",
    @SerializedName("locationHint") val locationHint: String? = null
)

class AiQueryParser(
    private val geminiApiKey: String,
    private val geminiApi: GeminiApiService = GeminiApiService.create()
) {
    private val gson = Gson()

    suspend fun parseUserPrompt(userInput: String): ParsedPlaceQuery {
        if (userInput.isBlank()) return ParsedPlaceQuery()

        val promptText = """
            Phân tích câu tìm kiếm: "$userInput"
            Trả về DUY NHẤT một chuỗi JSON hợp lệ không có markdown:
            {"cleanName": "Tên thương hiệu/quán chính (bỏ từ: tìm, quán, tiệm, coffee, cà phê, gần đây), hoặc null nếu chỉ tìm loại hình chung", "category": "catering.cafe hoặc catering.restaurant hoặc accommodation.hotel hoặc tourism", "locationHint": "khu vực/địa danh nếu có, còn lại null"}
        """.trimIndent()

        return try {
            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        parts = listOf(GeminiPart(text = promptText))
                    )
                )
            )

            val response = geminiApi.generateContent(
                apiKey = geminiApiKey,
                request = requestBody
            )

            val rawText = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                .orEmpty()

            val cleanJson = rawText
                .replace("```json", "")
                .replace("```", "")
                .trim()

            gson.fromJson(cleanJson, ParsedPlaceQuery::class.java) ?: fallbackParse(userInput)
        } catch (e: Exception) {
            println(">>> [LỖI KẾT NỐI GEMINI]: ${e.message}")
            fallbackParse(userInput)
        }
    }

    private fun fallbackParse(input: String): ParsedPlaceQuery {
        val lower = input.lowercase()
        val cat = when {
            lower.contains("cafe") || lower.contains("cà phê") || lower.contains("coffee") -> "catering.cafe"
            lower.contains("ăn") || lower.contains("nhà hàng") || lower.contains("quán") -> "catering.restaurant"
            else -> "catering,tourism"
        }
        val clean = input.replace(Regex("(?i)(tìm|quán|cà phê|cafe|coffee|gần đây)"), "").trim()
        return ParsedPlaceQuery(cleanName = clean.ifBlank { null }, category = cat)
    }
}