package android_app

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel điều phối logic nghiệp vụ tìm kiếm, lọc, sắp xếp địa điểm và cập nhật UI State.
 * Kết nối trực tiếp với PlaceRepository có sẵn trong dự án.
 */
class PlaceViewModel(
    private val repository: PlaceRepository,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main + Job())
) {
    private val _uiState = MutableStateFlow(PlaceUiState())
    val uiState: StateFlow<PlaceUiState> = _uiState.asStateFlow()

    private var loadDataJob: Job? = null

    init {
        // Tải dữ liệu mặc định khi khởi tạo
        loadPlaces()
    }

    /**
     * Hàm nhận tất cả sự kiện tương tác người dùng từ Composable UI.
     */
    fun onEvent(event: PlaceUiEvent) {
        when (event) {
            is PlaceUiEvent.OnSearchQueryChanged -> {
                _uiState.update { it.copy(searchQuery = event.query) }
                loadPlaces()
            }
            is PlaceUiEvent.OnCategorySelected -> {
                _uiState.update { it.copy(selectedCategory = event.category) }
                loadPlaces()
            }
            is PlaceUiEvent.OnFilterRatingChanged -> {
                _uiState.update { it.copy(minRating = event.minRating) }
                applyLocalFiltersAndSort()
            }
            is PlaceUiEvent.OnFilterPriceChanged -> {
                _uiState.update { it.copy(maxPriceLevel = event.maxPriceLevel) }
                applyLocalFiltersAndSort()
            }
            is PlaceUiEvent.OnSortTypeChanged -> {
                _uiState.update { it.copy(sortType = event.sortType) }
                applyLocalFiltersAndSort()
            }
            is PlaceUiEvent.OnToggleFavorite -> {
                toggleFavorite(event.placeId)
            }
            is PlaceUiEvent.OnPlaceSelected -> {
                _uiState.update { it.copy(selectedPlace = event.place) }
            }
            PlaceUiEvent.OnClearFilters -> {
                _uiState.update { 
                    it.copy(
                        minRating = 0.0, 
                        maxPriceLevel = 4, 
                        sortType = PlaceRepository.SortType.RATING_DESC
                    ) 
                }
                applyLocalFiltersAndSort()
            }
            PlaceUiEvent.OnRetry -> {
                loadPlaces()
            }
        }
    }

    // Biến lưu kết quả gốc từ API để phục vụ lọc/sắp xếp cục bộ
    private var rawPlacesList: List<PlaceItem> = emptyList()

    /**
     * Gọi API qua Repository để lấy dữ liệu danh sách địa điểm theo từ khóa & category
     */
    fun loadPlaces() {
        loadDataJob?.cancel()
        loadDataJob = scope.launch {
            _uiState.update { it.copy(status = PlaceScreenStatus.Loading) }

            try {
                val currentState = _uiState.value
                val keyword = currentState.searchQuery.ifBlank { "Địa điểm du lịch nổi tiếng" }

                // Thực thi tác vụ mạng trên Dispatchers.IO
                val results = withContext(Dispatchers.IO) {
                    repository.fetchPlaces(keyword, currentState.selectedCategory)
                }

                // Tính toán khoảng cách
                repository.calculateDistances(results, currentState.userLat, currentState.userLng)
                rawPlacesList = results

                // Áp dụng Lọc & Sắp xếp
                applyLocalFiltersAndSort()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        status = PlaceScreenStatus.Error(
                            e.localizedMessage ?: "Không thể kết nối đến máy chủ. Vui lòng thử lại!"
                        )
                    ) 
                }
            }
        }
    }

    /**
     * Áp dụng logic Lọc & Sắp xếp trên danh sách địa điểm đã tải về
     */
    private fun applyLocalFiltersAndSort() {
        val currentState = _uiState.value

        // 1. Lọc theo rating và price level
        val filtered = repository.filterPlaces(
            rawPlacesList, 
            minRating = currentState.minRating, 
            maxPriceLevel = currentState.maxPriceLevel
        )

        // 2. Sắp xếp theo tiêu chuẩn đã chọn
        val sorted = repository.sortPlaces(filtered, currentState.sortType)

        // 3. Cập nhật UI Status tương ứng
        _uiState.update {
            it.copy(
                status = if (sorted.isEmpty()) PlaceScreenStatus.Empty else PlaceScreenStatus.Success(sorted)
            )
        }
    }

    /**
     * Thêm hoặc xóa địa điểm khỏi danh sách Yêu thích
     */
    private fun toggleFavorite(placeId: String) {
        _uiState.update { state ->
            val updatedFavorites = state.favoritePlaceIds.toMutableSet()
            if (updatedFavorites.contains(placeId)) {
                updatedFavorites.remove(placeId)
            } else {
                updatedFavorites.add(placeId)
            }
            state.copy(favoritePlaceIds = updatedFavorites)
        }
    }
}
