package android_app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TripUiState(
    val trips: List<TripPlan> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val createTripSuccess: Boolean = false
)

class TripViewModel(
    private val repository: TripRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TripUiState())
    val uiState: StateFlow<TripUiState> = _uiState.asStateFlow()

    init {
        loadAllTrips()
    }

    fun loadAllTrips() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val allTrips = repository.getAllTrips()
                _uiState.update { it.copy(trips = allTrips, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    fun createTrip(
        title: String,
        destination: String,
        startDate: String,
        endDate: String,
        days: Int
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, createTripSuccess = false) }
            try {
                // Sẽ gọi qua Gemini AI ở lớp Repository để tính Budget
                repository.createTrip(
                    title = title,
                    destination = destination,
                    startDate = startDate,
                    endDate = endDate,
                    days = days
                )
                
                // Sau khi tạo xong, tải lại danh sách
                val updatedTrips = repository.getAllTrips()
                _uiState.update { 
                    it.copy(
                        trips = updatedTrips, 
                        isLoading = false,
                        createTripSuccess = true
                    ) 
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }
    
    fun resetCreateState() {
        _uiState.update { it.copy(createTripSuccess = false, error = null) }
    }
}
