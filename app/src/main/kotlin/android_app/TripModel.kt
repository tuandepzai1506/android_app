package android_app

import java.util.UUID

// 1. Trạm dừng / điểm đến
data class TripStop(
    val stopId: String = UUID.randomUUID().toString(),
    val placeId: String,
    val name: String,
    val address: String? = null,
    val lat: Double? = null,
    val lng: Double? = null,
    val imageUrl: String? = null,
    val stopOrder: Int = 1
)

// 2. Ngân sách do Gemini tính
data class BudgetBreakdown(
    val transportCost: String = "0đ",
    val accommodationCost: String = "0đ",
    val foodCost: String = "0đ",
    val entertainmentCost: String = "0đ",
    val totalEstimated: String = "0đ",
    val summaryAdvice: String = ""
)

// 3. Phân loại chi tiêu
enum class ExpenseCategory {
    TRANSPORT,        // Di chuyển
    ACCOMMODATION,    // Lưu trú
    FOOD,             // Ăn uống
    ENTERTAINMENT,    // Vui chơi
    OTHER             // Khác
}

// 4. Một khoản chi tiêu thực tế
data class TripExpense(
    val expenseId: String = UUID.randomUUID().toString(),
    val title: String,
    val amount: Long,
    val category: ExpenseCategory,
    val timestamp: Long = System.currentTimeMillis()
)

// 5. Kết quả trả về sau khi thêm chi tiêu (Cảnh báo)
data class BudgetStatus(
    val totalSpent: Long,
    val totalEstimated: Long,
    val remainingBalance: Long,
    val warningMessage: String?
)

// 6. Đối tượng chuyến đi hoàn chỉnh
data class TripPlan(
    val tripId: String = UUID.randomUUID().toString(),
    val title: String,
    val destination: String,
    val startDate: String,
    val endDate: String,
    val totalDays: Int = 1,
    val stops: List<TripStop> = emptyList(),
    val budget: BudgetBreakdown? = null,
    val expenses: List<TripExpense> = emptyList(), // Danh sách chi tiêu thực tế
    val createdAt: Long = System.currentTimeMillis()
)