package android_app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewItineraryScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activities = listOf(
        "08:00" to "Ăn sáng tại khách sạn",
        "09:30" to "Tham quan Dinh Bảo Đại",
        "12:00" to "Ăn trưa đặc sản",
        "14:00" to "Check-in Quảng trường Lâm Viên",
        "18:00" to "Dạo chợ đêm"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết lịch trình") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Du lịch Đà Lạt mộng mơ",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "12/10/2026 - 15/10/2026",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }

            // Timeline
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp)
            ) {
                item {
                    Text(
                        text = "Ngày 1 - 12/10",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }
                
                items(activities.size) { index ->
                    val isLast = index == activities.lastIndex
                    TimelineItem(
                        time = activities[index].first,
                        name = activities[index].second,
                        isLast = isLast
                    )
                }
            }
        }
    }
}

@Composable
fun TimelineItem(
    time: String,
    name: String,
    isLast: Boolean
) {
    val lineColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    val circleColor = MaterialTheme.colorScheme.primary
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
    ) {
        // Timeline graphics
        Box(
            modifier = Modifier
                .width(40.dp)
                .fillMaxHeight(),
            contentAlignment = Alignment.TopCenter
        ) {
            if (!isLast) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawLine(
                        color = lineColor,
                        start = Offset(size.width / 2, 24.dp.toPx()),
                        end = Offset(size.width / 2, size.height),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }
            
            Surface(
                shape = MaterialTheme.shapes.small,
                color = circleColor,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .size(16.dp)
            ) {}
        }
        
        // Content
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp, top = 12.dp, bottom = 24.dp)
        ) {
            Text(
                text = time,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
