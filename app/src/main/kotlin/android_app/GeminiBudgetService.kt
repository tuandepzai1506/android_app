package android_app

import com.google.gson.Gson

class GeminiBudgetService(
    private val geminiApiKey: String,
    private val geminiApi: GeminiApiService = GeminiApiService.create()
) {
    private val gson = Gson()

    suspend fun estimateBudget(
        destination: String,
        days: Int,
        stopNames: List<String>,
        peopleCount: Int = 1
    ): BudgetBreakdown {
        val stopsDescription = if (stopNames.isNotEmpty()) {
            stopNames.joinToString(", ")
        } else {
            "Khám phá tự do quanh khu vực"
        }

        val promptText = """
            Bạn là trợ lý tư vấn tài chính du lịch tại Việt Nam.
            Hãy ước tính chi tiết ngân sách thực tế bằng tiền VNĐ cho chuyến đi:
            - Điểm đến chính: $destination
            - Thời gian: $days ngày
            - Số lượng người: $peopleCount người
            - Các trạm/địa điểm dự kiến ghé: $stopsDescription

            Trả về DUY NHẤT một chuỗi JSON hợp lệ không có markdown theo định dạng:
            {
               "transportCost": "ước tính di chuyển (ví dụ: 600.000đ)",
               "accommodationCost": "ước tính lưu trú/khách sạn (ví dụ: 1.500.000đ)",
               "foodCost": "ước tính ăn uống (ví dụ: 1.200.000đ)",
               "entertainmentCost": "vé tham quan, giải trí (ví dụ: 400.000đ)",
               "totalEstimated": "tổng chi phí ước tính (ví dụ: 3.700.000đ)",
               "summaryAdvice": "1-2 câu lời khuyên ngắn gọn để tiết kiệm ngân sách"
            }
        """.trimIndent()

        return try {
            val requestBody = GeminiRequest(
                contents = listOf(
                    GeminiContent(parts = listOf(GeminiPart(text = promptText)))
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

            gson.fromJson(cleanJson, BudgetBreakdown::class.java) ?: fallbackBudget(days)
        } catch (e: Exception) {
            println(">>> [LỖI GEMINI BUDGET]: ${e.message}")
            fallbackBudget(days)
        }
    }

    private fun fallbackBudget(days: Int): BudgetBreakdown {
        val total = days * 1_000_000
        return BudgetBreakdown(
            transportCost = "500.000đ",
            accommodationCost = "${days * 500_000}đ",
            foodCost = "${days * 350_000}đ",
            entertainmentCost = "200.000đ",
            totalEstimated = "${String.format("%,d", total)}đ",
            summaryAdvice = "Ước tính tiêu chuẩn trung bình khoảng 1.000.000đ/ngày/người."
        )
    }
}