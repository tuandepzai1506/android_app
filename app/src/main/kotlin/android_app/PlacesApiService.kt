package android_app

import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

interface PlacesApiService {
    // Gọi Text Search của Google Places API
    @GET("maps/api/place/textsearch/json")
    fun searchPlaces(
        @Query("query") query: String,
        @Query("type") type: String? = null,       // restaurant, lodging, tourist_attraction, cafe...
        @Query("key") apiKey: String
    ): Call<PlacesSearchResponse>

    companion object {
        private const val BASE_URL = "https://maps.googleapis.com/"

        fun create(): PlacesApiService {
            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
            return retrofit.create(PlacesApiService::class.java)
        }
    }
}