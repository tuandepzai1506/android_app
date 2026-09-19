
package android_app

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

// Google Places & Directions Interface
interface GoogleApiService {
    @GET("maps/api/place/textsearch/json")
    fun searchPlaces(
        @Query("query") query: String,
        @Query("type") type: String?,
        @Query("key") apiKey: String
    ): Call<PlacesSearchResponse>

    @GET("maps/api/place/details/json")
    fun getPlaceDetail(
        @Query("place_id") placeId: String,
        @Query("fields") fields: String = "name,rating,formatted_address,price_level,geometry,opening_hours,photos,reviews",
        @Query("key") apiKey: String
    ): Call<PlaceDetailResponse>
}

// OpenWeatherMap API Interface
interface WeatherApiService {
    @GET("data/2.5/weather")
    fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("units") units: String = "metric",
        @Query("appid") apiKey: String
    ): Call<WeatherResponse>
}

object RetrofitClient {
    fun createGoogleApi(): GoogleApiService {
        return Retrofit.Builder()
            .baseUrl("https://maps.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(GoogleApiService::class.java)
    }

    fun createWeatherApi(): WeatherApiService {
        return Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)
    }
}