package android_app

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android_app.ui.theme.TravelAppTheme

/**
 * Các thành phần Giao diện Compose (Material 3) tái sử dụng cho ứng dụng Du lịch.
 */

// Danh mục địa điểm phổ biến
data class PlaceCategory(val id: String?, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

val placeCategories = listOf(
    PlaceCategory(null, "Tất cả", Icons.Default.Public),
    PlaceCategory("tourist_attraction", "Tham quan", Icons.Default.PhotoCamera),
    PlaceCategory("lodging", "Khách sạn", Icons.Default.Hotel),
    PlaceCategory("restaurant", "Nhà hàng", Icons.Default.Restaurant),
    PlaceCategory("cafe", "Cà phê", Icons.Default.LocalCafe),
    PlaceCategory("shopping_mall", "Mua sắm", Icons.Default.ShoppingBag),
    PlaceCategory("amusement_park", "Vui chơi", Icons.Default.Attractions)
)

/**
 * Thanh cuộn chọn Danh mục Địa điểm (Category Filter Chips)
 */
@Composable
fun CategoryChipRow(
    selectedCategory: String?,
    onCategorySelected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(placeCategories) { category ->
            FilterChip(
                selected = selectedCategory == category.id,
                onClick = { onCategorySelected(category.id) },
                label = { Text(category.label) },
                leadingIcon = {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = category.label,
                        modifier = Modifier.size(18.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp)
            )
        }
    }
}

/**
 * Thanh Bộ Lọc Nâng Cao & Tùy Chọn Sắp Xếp (Sort & Filter Bar)
 */
@Composable
fun FilterAndSortBar(
    currentSortType: PlaceRepository.SortType,
    minRating: Double,
    maxPriceLevel: Int,
    onSortTypeChanged: (PlaceRepository.SortType) -> Unit,
    onFilterRatingChanged: (Double) -> Unit,
    onFilterPriceChanged: (Int) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showSortMenu by remember { mutableStateOf(false) }
    var showFilterDialog by remember { mutableStateOf(false) }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Nút chọn sắp xếp
        Box {
            AssistChip(
                onClick = { showSortMenu = true },
                label = {
                    val sortText = when (currentSortType) {
                        PlaceRepository.SortType.RATING_DESC -> "Đánh giá cao nhất"
                        PlaceRepository.SortType.DISTANCE -> "Gần đây nhất"
                        PlaceRepository.SortType.PRICE_ASC -> "Giá thấp đến cao"
                    }
                    Text("Sắp xếp: $sortText")
                },
                leadingIcon = {
                    Icon(Icons.Default.Sort, contentDescription = "Sort", modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            )

            DropdownMenu(
                expanded = showSortMenu,
                onDismissRequest = { showSortMenu = false }
            ) {
                DropdownMenuItem(
                    text = { Text("Đánh giá cao nhất") },
                    onClick = {
                        onSortTypeChanged(PlaceRepository.SortType.RATING_DESC)
                        showSortMenu = false
                    },
                    leadingIcon = { Icon(Icons.Outlined.Star, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Khoảng cách gần nhất") },
                    onClick = {
                        onSortTypeChanged(PlaceRepository.SortType.DISTANCE)
                        showSortMenu = false
                    },
                    leadingIcon = { Icon(Icons.Outlined.LocationOn, contentDescription = null) }
                )
                DropdownMenuItem(
                    text = { Text("Giá cả hợp lý nhất") },
                    onClick = {
                        onSortTypeChanged(PlaceRepository.SortType.PRICE_ASC)
                        showSortMenu = false
                    },
                    leadingIcon = { Icon(Icons.Default.AttachMoney, contentDescription = null) }
                )
            }
        }

        // Nút mở Dialog bộ lọc chi tiết
        FilterChip(
            selected = minRating > 0 || maxPriceLevel < 4,
            onClick = { showFilterDialog = true },
            label = { Text("Bộ lọc") },
            leadingIcon = { Icon(Icons.Default.FilterList, contentDescription = "Filter", modifier = Modifier.size(18.dp)) }
        )
    }

    // Dialog Bộ Lọc Chi Tiết
    if (showFilterDialog) {
        AlertDialog(
            onDismissRequest = { showFilterDialog = false },
            title = { Text("Bộ Lọc Địa Điểm", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text("Đánh giá tối thiểu: ${if (minRating > 0) "$minRating★" else "Tất cả"}")
                    Slider(
                        value = minRating.toFloat(),
                        onValueChange = { onFilterRatingChanged(it.toDouble()) },
                        valueRange = 0f..5f,
                        steps = 9
                    )

                    Text("Mức giá tối đa: ${"$".repeat(maxPriceLevel.coerceAtLeast(1))}")
                    Slider(
                        value = maxPriceLevel.toFloat(),
                        onValueChange = { onFilterPriceChanged(it.toInt()) },
                        valueRange = 1f..4f,
                        steps = 2
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showFilterDialog = false }) {
                    Text("Áp dụng")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    onClearFilters()
                    showFilterDialog = false
                }) {
                    Text("Đặt lại")
                }
            }
        )
    }
}

/**
 * Item Card hiển thị từng Địa điểm Du lịch trong Danh sách
 */
@Composable
fun PlaceCard(
    place: PlaceItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon / Ảnh đại diện
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        place.types.contains("lodging") -> Icons.Default.Hotel
                        place.types.contains("restaurant") -> Icons.Default.Restaurant
                        place.types.contains("cafe") -> Icons.Default.LocalCafe
                        else -> Icons.Default.Place
                    },
                    contentDescription = null,
                    modifier = Modifier.size(36.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Thông tin chi tiết
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = place.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                place.vicinity?.let {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.secondary
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Badge Rating
                    Surface(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = "Rating",
                                modifier = Modifier.size(12.dp),
                                tint = Color(0xFFFFB800)
                            )
                            Spacer(modifier = Modifier.width(2.dp))
                            Text(
                                text = place.rating.toString(),
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Mức giá ($$$)
                    if (place.priceLevel > 0) {
                        Text(
                            text = "$".repeat(place.priceLevel),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Khoảng cách (km)
                    if (place.distanceInKm > 0) {
                        Text(
                            text = "• ${place.distanceInKm} km",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }

            // Nút Thêm vào Yêu thích
            IconButton(onClick = onToggleFavorite) {
                Icon(
                    imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite",
                    tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}

/**
 * Trạng thái Đang Tải Dữ Liệu (Loading State)
 */
@Composable
fun LoadingStateView(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 4.dp
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Đang tìm kiếm địa điểm tốt nhất...",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Trạng thái Không Tìm Thấy Kết Quả (Empty State)
 */
@Composable
fun EmptyStateView(
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.SearchOff,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.outline
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Không tìm thấy địa điểm nào",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Hãy thử thay đổi từ khóa hoặc điều chỉnh lại bộ lọc đánh giá / giá tiền.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        OutlinedButton(onClick = onClearFilters) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Đặt lại bộ lọc")
        }
    }
}

/**
 * Trạng thái Lỗi Kết Nối / Xử Lý (Error State)
 */
@Composable
fun ErrorStateView(
    errorMessage: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Đã xảy ra lỗi",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = errorMessage,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
        ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Thử lại")
        }
    }
}

/**
 * Modal BottomSheet Xem Chi Tiết Địa Điểm (Mục 3)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlaceDetailBottomSheet(
    place: PlaceItem,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onDismissRequest: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Tên & Nút Favorite
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = place.name,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    place.vicinity?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.outline
                    )
                }
            }

            HorizontalDivider()

            // Chi tiết thông tin
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                DetailInfoItem(
                    icon = Icons.Default.Star,
                    title = "Đánh giá",
                    value = "${place.rating} / 5.0",
                    tint = Color(0xFFFFB800)
                )
                DetailInfoItem(
                    icon = Icons.Default.AttachMoney,
                    title = "Mức giá",
                    value = if (place.priceLevel > 0) "$".repeat(place.priceLevel) else "Miễn phí/Chưa rõ"
                )
                DetailInfoItem(
                    icon = Icons.Outlined.LocationOn,
                    title = "Khoảng cách",
                    value = "${place.distanceInKm} km"
                )
            }

            HorizontalDivider()

            // Các hành động
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismissRequest,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Đóng")
                }
                Button(
                    onClick = { /* Mở bản đồ / Thêm vào Lịch trình */ },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Tạo chuyến đi")
                }
            }
        }
    }
}

@Composable
private fun DetailInfoItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    value: String,
    tint: Color = MaterialTheme.colorScheme.primary
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(imageVector = icon, contentDescription = title, tint = tint, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline)
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
    }
}

// ---------------------------------------------------------------------------
// PREVIEWS FOR COMPONENTS
// ---------------------------------------------------------------------------

@Preview(name = "Place Card - Light Mode", showBackground = true)
@Composable
fun PlaceCardLightPreview() {
    TravelAppTheme(darkTheme = false) {
        PlaceCard(
            place = PlaceItem(
                placeId = "1",
                name = "Chợ Bến Thành",
                rating = 4.5,
                priceLevel = 2,
                vicinity = "Quận 1, Thành phố Hồ Chí Minh",
                types = listOf("tourist_attraction")
            ).apply { distanceInKm = 1.2 },
            isFavorite = true,
            onToggleFavorite = {},
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Place Card - Dark Mode", showBackground = true)
@Composable
fun PlaceCardDarkPreview() {
    TravelAppTheme(darkTheme = true) {
        PlaceCard(
            place = PlaceItem(
                placeId = "2",
                name = "Khách sạn Rex Saigon",
                rating = 4.8,
                priceLevel = 4,
                vicinity = "141 Nguyễn Huệ, Quận 1",
                types = listOf("lodging")
            ).apply { distanceInKm = 0.5 },
            isFavorite = false,
            onToggleFavorite = {},
            onClick = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Preview(name = "Empty State - Light Mode", showBackground = true)
@Composable
fun EmptyStateViewPreview() {
    TravelAppTheme {
        EmptyStateView(onClearFilters = {})
    }
}

@Preview(name = "Error State - Light Mode", showBackground = true)
@Composable
fun ErrorStateViewPreview() {
    TravelAppTheme {
        ErrorStateView(
            errorMessage = "Không thể kết nối đến máy chủ. Vui lòng kiểm tra lại kết nối Internet.",
            onRetry = {}
        )
    }
}
