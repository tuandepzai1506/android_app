package android_app

import com.google.gson.annotations.SerializedName
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// ==========================================
// 1. OPENWEATHERMAP SERVICE & MODELS
// ==========================================
interface WeatherApiService {

    @GET("data/2.5/weather")
    suspend fun getCurrentWeather(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric",
        @Query("lang") lang: String = "vi"
    ): WeatherResponse

    companion object {
        private const val BASE_URL = "https://api.openweathermap.org/"

        fun create(): WeatherApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WeatherApiService::class.java)
        }
    }
}

// ==========================================
// 2. WIKIPEDIA SERVICE & MODELS (LẤY ẢNH ĐỊA DANH)
// ==========================================
data class WikiResponse(
    @SerializedName("query") val query: WikiQuery? = null
)

data class WikiQuery(
    @SerializedName("pages") val pages: Map<String, WikiPage>? = null
)

data class WikiPage(
    @SerializedName("pageid") val pageId: Long? = null,
    @SerializedName("title") val title: String? = null,
    @SerializedName("thumbnail") val thumbnail: WikiThumbnail? = null
)

data class WikiThumbnail(
    @SerializedName("source") val source: String? = null,
    @SerializedName("width") val width: Int = 0,
    @SerializedName("height") val height: Int = 0
)
data class GeminiRequest(
    val contents: List<GeminiContent>
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>? = null
)

data class GeminiCandidate(
    val content: GeminiContent? = null
)

interface GeminiApiService {

    // ĐỔI TỪ 1.5 SANG 2.5 Ở DÒNG NÀY:
    @retrofit2.http.POST("v1beta/models/gemini-flash-latest:generateContent")
    suspend fun generateContent(
        @retrofit2.http.Query("key") apiKey: String,
        @retrofit2.http.Body request: GeminiRequest
    ): GeminiResponse

    companion object {
        private const val BASE_URL = "https://generativelanguage.googleapis.com/"

        fun create(): GeminiApiService {
            return retrofit2.Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(retrofit2.converter.gson.GsonConverterFactory.create())
                .build()
                .create(GeminiApiService::class.java)
        }
    }
}

interface WikipediaApiService {

    @GET("w/api.php")
    suspend fun fetchPlaceImage(
        @Query("action") action: String = "query",
        @Query("prop") prop: String = "pageimages",
        @Query("format") format: String = "json",
        @Query("pithumbsize") thumbSize: Int = 600,
        @Query("titles") title: String
    ): WikiResponse

    companion object {
        private const val BASE_URL = "https://vi.wikipedia.org/"

        fun create(): WikipediaApiService {
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(WikipediaApiService::class.java)
        }
    }
}