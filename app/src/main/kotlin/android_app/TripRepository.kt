package android_app

class TripRepository(
    private val dataSource: TripDataSource = InMemoryTripDataSource(),
    private val budgetService: GeminiBudgetService
) {

    suspend fun createTrip(
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
        days: Int,
        initialStops: List<TripStop> = emptyList(),
        peopleCount: Int = 1
    ): TripPlan {
        val stopNames = initialStops.map { it.name }
        val budget = budgetService.estimateBudget(
            destination = destination,
            days = days,
            stopNames = stopNames,
            peopleCount = peopleCount
        )

        val trip = TripPlan(
            title = title,
            destination = destination,
            startDate = startDate,
            endDate = endDate,
            totalDays = days,
            stops = initialStops,
            budget = budget
        )

        dataSource.saveTrip(trip)
        return trip
    }

    suspend fun addPlaceToTrip(tripId: String, place: PlaceItem): Boolean {
        val targetTrip = dataSource.getTripById(tripId) ?: return false

        val newStop = TripStop(
            placeId = place.placeId,
            name = place.name,
            address = place.vicinity,
            lat = place.geometry?.location?.lat,
            lng = place.geometry?.location?.lng,
            imageUrl = place.imageUrl,
            stopOrder = targetTrip.stops.size + 1
        )

        val added = dataSource.addStopToTrip(tripId, newStop)
        if (added) {
            recalculateBudgetForTrip(tripId)
        }
        return added
    }

    suspend fun recalculateBudgetForTrip(tripId: String): BudgetBreakdown? {
        val trip = dataSource.getTripById(tripId) ?: return null
        val updatedBudget = budgetService.estimateBudget(
            destination = trip.destination,
            days = trip.totalDays,
            stopNames = trip.stops.map { it.name }
        )
        val updatedTrip = trip.copy(budget = updatedBudget)
        dataSource.saveTrip(updatedTrip)
        return updatedBudget
    }

    suspend fun addExpenseAndCheckBudget(
        tripId: String,
        title: String,
        amount: Long,
        category: ExpenseCategory
    ): BudgetStatus? {
        val expense = TripExpense(title = title, amount = amount, category = category)
        val isAdded = dataSource.addExpenseToTrip(tripId, expense)

        if (!isAdded) return null

        val trip = dataSource.getTripById(tripId) ?: return null

        val totalSpent = trip.expenses.sumOf { it.amount }
        val estimatedTotalStr = trip.budget?.totalEstimated ?: "0"
        val totalEstimated = parseCurrencyToLong(estimatedTotalStr)
        val remaining = totalEstimated - totalSpent

        val warningMessage = when {
            totalSpent > totalEstimated ->
                "🚨 VƯỢT NGÂN SÁCH! Bạn đã tiêu lố ${formatMoney(totalSpent - totalEstimated)}đ so với dự kiến."
            totalSpent >= totalEstimated * 0.8 ->
                "⚠️ CHÚ Ý: Đã tiêu ${formatMoney(totalSpent)}đ. Chỉ còn lại ${formatMoney(remaining)}đ (Sắp cạn ngân sách)."
            else -> null
        }

        return BudgetStatus(
            totalSpent = totalSpent,
            totalEstimated = totalEstimated,
            remainingBalance = remaining,
            warningMessage = warningMessage
        )
    }

    private fun parseCurrencyToLong(currencyStr: String): Long {
        val digitsOnly = currencyStr.replace(Regex("[^0-9]"), "")
        return digitsOnly.toLongOrNull() ?: 0L
    }

    private fun formatMoney(amount: Long): String {
        return String.format("%,d", amount).replace(',', '.')
    }

    suspend fun getAllTrips(): List<TripPlan> = dataSource.getTrips()

    suspend fun getTripDetails(tripId: String): TripPlan? = dataSource.getTripById(tripId)
}