package android_app

<<<<<<< HEAD
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpServer
import java.io.OutputStream
import java.net.InetSocketAddress

fun main() {
    val server = HttpServer.create(InetSocketAddress(8080), 0)

    server.createContext("/api/status") { exchange ->
        val jsonResponse = """{"status": "ok", "message": "Backend dang chay tot!"}"""
        sendResponse(exchange, jsonResponse)
    }

    server.createContext("/api/users") { exchange ->
        val jsonResponse = """
            [
                {"id": 1, "name": "Nguyen Van A", "email": "a@gmail.com"},
                {"id": 2, "name": "Tran Thi B", "email": "b@gmail.com"}
            ]
        """.trimIndent()
        sendResponse(exchange, jsonResponse)
    }

    server.executor = null
    server.start()
    println(">>> Backend Server dang chay tai: http://localhost:8080")

    // Giữ cho Server tiếp tục chạy, không tự thoát
    Thread.currentThread().join()
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
=======
import android.app.Application

/**
 * Lớp Application đại diện cho toàn bộ ứng dụng Android Native Du Lịch.
 */
class TravelApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Khởi tạo các cấu hình toàn cục nếu cần
    }
}
>>>>>>> 734c911 (feat: Implement Travel App UI with Jetpack Compose & Material 3)
