package android_app

interface TripDataSource {
    suspend fun saveTrip(trip: TripPlan): Boolean
    suspend fun getTrips(): List<TripPlan>
    suspend fun getTripById(tripId: String): TripPlan?
    suspend fun addStopToTrip(tripId: String, stop: TripStop): Boolean
    suspend fun addExpenseToTrip(tripId: String, expense: TripExpense): Boolean
    suspend fun deleteTrip(tripId: String): Boolean
}

class InMemoryTripDataSource : TripDataSource {
    private val trips = mutableListOf<TripPlan>()

    override suspend fun saveTrip(trip: TripPlan): Boolean {
        trips.removeAll { it.tripId == trip.tripId }
        trips.add(trip)
        return true
    }

    override suspend fun getTrips(): List<TripPlan> = trips.toList()

    override suspend fun getTripById(tripId: String): TripPlan? {
        return trips.find { it.tripId == tripId }
    }

    override suspend fun addStopToTrip(tripId: String, stop: TripStop): Boolean {
        val index = trips.indexOfFirst { it.tripId == tripId }
        if (index != -1) {
            val currentTrip = trips[index]
            val updatedStops = currentTrip.stops + stop.copy(stopOrder = currentTrip.stops.size + 1)
            trips[index] = currentTrip.copy(stops = updatedStops)
            return true
        }
        return false
    }

    override suspend fun addExpenseToTrip(tripId: String, expense: TripExpense): Boolean {
        val index = trips.indexOfFirst { it.tripId == tripId }
        if (index != -1) {
            val currentTrip = trips[index]
            val updatedExpenses = currentTrip.expenses + expense
            trips[index] = currentTrip.copy(expenses = updatedExpenses)
            return true
        }
        return false
    }

    override suspend fun deleteTrip(tripId: String): Boolean {
        return trips.removeAll { it.tripId == tripId }
    }
}