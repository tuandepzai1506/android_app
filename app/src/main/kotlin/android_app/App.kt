package android_app

import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import kotlin.math.*

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    // 0. API kiểm tra trạng thái Server
    server.createContext("/api/status") { exchange ->
        val jsonResponse = """{"status": "ok", "message": "Backend dang chay tot!"}"""
        sendResponse(exchange, jsonResponse)
    }

    // 1. API Khám phá & Tìm kiếm địa điểm (Chức năng 2)
    // URL: http://localhost:8080/api/places/search?type=cafe
    server.createContext("/api/places/search") { exchange ->
        val queryParams = parseQueryParams(exchange.requestURI.query)
        val category = queryParams["type"] ?: "all"

        val jsonResponse =
                """
            [
                {
                    "place_id": "pl_001",
                    "name": "Cafe Giảng",
                    "category": "$category",
                    "rating": 4.6,
                    "price_level": 1,
                    "distance_km": 1.2
                },
                {
                    "place_id": "pl_002",
                    "name": "Khách sạn Metropole",
                    "category": "$category",
                    "rating": 4.8,
                    "price_level": 4,
                    "distance_km": 2.5
                }
            ]
        """.trimIndent()
        sendResponse(exchange, jsonResponse)
    }

    // 2. API Chi tiết địa điểm (Chức năng 3)
    // URL: http://localhost:8080/api/places/detail
    server.createContext("/api/places/detail") { exchange ->
        val jsonResponse =
                """
            {
                "place_id": "pl_001",
                "name": "Cafe Giảng",
                "rating": 4.6,
                "address": "39 Nguyễn Hữu Huân, Hoàn Kiếm, Hà Nội",
                "distance_km": 1.2,
                "estimated_budget": "35.000 - 60.000 VNĐ",
                "opening_hours": "07:00 - 22:00",
                "reviews_count": 1280,
                "photo_url": "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb"
            }
        """.trimIndent()
        sendResponse(exchange, jsonResponse)
    }

    // 3. API Tự động phân bổ ngân sách (Chức năng 8)
    // URL: http://localhost:8080/api/budget/allocate?total=10000000
    server.createContext("/api/budget/allocate") { exchange ->
        val queryParams = parseQueryParams(exchange.requestURI.query)
        val total = queryParams["total"]?.toDoubleOrNull() ?: 10_000_000.0

        val hotel = total * 0.35
        val food = total * 0.30
        val transport = total * 0.20
        val activity = total * 0.15

        val jsonResponse =
                """
            {
                "total_budget": $total,
                "allocation": {
                    "hotel_budget": $hotel,
                    "food_budget": $food,
                    "transport_budget": $transport,
                    "activity_budget": $activity
                }
            }
        """.trimIndent()
        sendResponse(exchange, jsonResponse)
    }

    // 4. API Sinh lịch trình gợi ý tối ưu (Chức năng 7)
    // URL: http://localhost:8080/api/itinerary/plan
    server.createContext("/api/itinerary/plan") { exchange ->
        val jsonResponse =
                """
            {
                "trip_title": "Lịch trình Hà Nội 1 ngày",
                "stops": [
                    { "order": 1, "place": "Hồ Hoàn Kiếm", "visit_duration": "60 phut", "distance_km": 0.0 },
                    { "order": 2, "place": "Nhà thờ Lớn", "visit_duration": "45 phut", "distance_km": 0.8 },
                    { "order": 3, "place": "Văn Miếu Quốc Tử Giám", "visit_duration": "90 phut", "distance_km": 2.1 }
                ]
            }
        """.trimIndent()
        sendResponse(exchange, jsonResponse)
    }

    // 5. API Tổng hợp nhật ký hành trình (Chức năng 15)
    // URL: http://localhost:8080/api/journal/summary
    server.createContext("/api/journal/summary") { exchange ->
        val jsonResponse =
                """
            {
                "trip_id": "trip_hn_01",
                "trip_name": "Khám phá Phố Cổ",
                "duration": "2 ngày 1 đêm",
                "total_places_visited": 5,
                "total_photos_count": 24,
                "total_spent_vnd": 3450000,
                "total_distance_km": 15.6,
                "visited_places": [
                    "Hồ Hoàn Kiếm",
                    "Chợ Đồng Xuân",
                    "Hoàng Thành Thăng Long",
                    "Cafe Giảng",
                    "Cầu Long Biên"
                ]
            }
        """.trimIndent()
        sendResponse(exchange, jsonResponse)
    }

    server.executor = null
    server.start()
    println(">>> Backend Server dang chay tai: http://localhost:8080")

    // Giữ cho Server tiếp tục chạy, không tự thoát
    Thread.currentThread().join()
}

// Hàm hỗ trợ tách tham số Query string (ví dụ: ?type=cafe&total=5000)
private fun parseQueryParams(query: String?): Map<String, String> {
    if (query.isNullOrEmpty()) return emptyMap()
    val params = mutableMapOf<String, String>()
    query.split("&").forEach { pair ->
        val parts = pair.split("=")
        if (parts.size == 2) {
            val key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8.name())
            val value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8.name())
            params[key] = value
        }
    }
    return params
}

private fun sendResponse(exchange: HttpExchange, responseText: String) {
    exchange.responseHeaders.add("Content-Type", "application/json; charset=UTF-8")
    exchange.responseHeaders.add("Access-Control-Allow-Origin", "*")

    val bytes = responseText.toByteArray(Charsets.UTF_8)
    exchange.sendResponseHeaders(200, bytes.size.toLong())
    val os: OutputStream = exchange.responseBody
    os.write(bytes)
    os.close()
}
