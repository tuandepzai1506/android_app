package android_app

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
