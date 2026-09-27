package android_app

/**
 * Trạng thái giao diện của màn hình Khám phá & Tìm kiếm Địa điểm Du lịch.
 * Tuân thủ Unidirectional Data Flow (UDF) trong Clean Architecture.
 */

sealed interface PlaceScreenStatus {
    /** Đang tải dữ liệu từ Repository/API */
    data object Loading : PlaceScreenStatus

    /** Tải thành công danh sách địa điểm */
    data class Success(val places: List<PlaceItem>) : PlaceScreenStatus

    /** Không tìm thấy kết quả nào phù hợp với bộ lọc */
    data object Empty : PlaceScreenStatus

    /** Đã xảy ra lỗi trong quá trình lấy hoặc xử lý dữ liệu */
    data class Error(val message: String) : PlaceScreenStatus
}

data class PlaceUiState(
    /** Từ khóa tìm kiếm người dùng nhập */
    val searchQuery: String = "",
    
    /** Danh mục địa điểm đang chọn (VD: tourist_attraction, restaurant, lodging, cafe...) */
    val selectedCategory: String? = "tourist_attraction",
    
    /** Mức đánh giá tối thiểu (0.0 - 5.0) */
    val minRating: Double = 0.0,
    
    /** Mức giá tối đa (0 - 4 đại diện cho $) */
    val maxPriceLevel: Int = 4,
    
    /** Tiêu chí sắp xếp (DISTANCE, RATING_DESC, PRICE_ASC) */
    val sortType: PlaceRepository.SortType = PlaceRepository.SortType.RATING_DESC,
    
    /** Vị trí hiện tại của người dùng (vĩ độ, kinh độ) */
    val userLat: Double = 10.7769,
    val userLng: Double = 106.7009,
    
    /** Danh sách ID các địa điểm được yêu thích */
    val favoritePlaceIds: Set<String> = emptySet(),
    
    /** Địa điểm đang được chọn để xem chi tiết (Mục 3) */
    val selectedPlace: PlaceItem? = null,
    
    /** Trạng thái tổng thể của màn hình */
    val status: PlaceScreenStatus = PlaceScreenStatus.Loading
)
