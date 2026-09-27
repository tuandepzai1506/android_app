package android_app

/**
 * Các sự kiện tương tác người dùng (User Intents) từ Giao diện (UI) truyền về ViewModel.
 */
sealed interface PlaceUiEvent {
    /** Người dùng thay đổi từ khóa tìm kiếm */
    data class OnSearchQueryChanged(val query: String) : PlaceUiEvent

    /** Người dùng bấm chọn danh mục địa điểm (Khách sạn, Nhà hàng, Điểm tham quan...) */
    data class OnCategorySelected(val category: String?) : PlaceUiEvent

    /** Người dùng thay đổi bộ lọc đánh giá sao tối thiểu */
    data class OnFilterRatingChanged(val minRating: Double) : PlaceUiEvent

    /** Người dùng thay đổi bộ lọc mức giá tối đa */
    data class OnFilterPriceChanged(val maxPriceLevel: Int) : PlaceUiEvent

    /** Người dùng chọn kiểu sắp xếp (Khoảng cách, Đánh giá, Giá cả) */
    data class OnSortTypeChanged(val sortType: PlaceRepository.SortType) : PlaceUiEvent

    /** Bấm nút yêu thích địa điểm (Mục 4 trong lịch trình) */
    data class OnToggleFavorite(val placeId: String) : PlaceUiEvent

    /** Bấm chọn địa điểm để xem chi tiết (Mục 3 trong lịch trình) */
    data class OnPlaceSelected(val place: PlaceItem?) : PlaceUiEvent

    /** Khôi phục lại tất cả bộ lọc về mặc định */
    data object OnClearFilters : PlaceUiEvent

    /** Tải lại dữ liệu khi bị lỗi */
    data object OnRetry : PlaceUiEvent
}
