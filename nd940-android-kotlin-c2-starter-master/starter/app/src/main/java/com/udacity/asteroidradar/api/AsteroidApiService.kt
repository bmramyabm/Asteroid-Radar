package com.udacity.asteroidradar.api

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.udacity.asteroidradar.domain.Constants
import com.udacity.asteroidradar.domain.Constants.BASE_URL
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query


enum class AsteroidApiFilter(val value: String) {
    SHOW_WEEK("week"), SHOW_TODAY("today"), SHOW_SAVE(
        "all"
    )
}


private val retrofit = Retrofit.Builder()
    .addConverterFactory(ScalarsConverterFactory.create())
    .baseUrl(BASE_URL)
    .build()

/**
 * Build the Moshi object that Retrofit will be using, making sure to add the Kotlin adapter for
 * full Kotlin compatibility.
 */
private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()


private val retrofit_moshi = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(Constants.BASE_URL)
    .build()

/**
 * A retrofit service to fetch the asteroid and picture of the day
 */
interface AsteroidApiService {
    @GET("neo/rest/v1/feed")
    suspend fun getAsteroids(
        @Query("start_date") startDate: String?,
        @Query("end_date") endDate: String?,
        @Query("api_key") apiKey: String
    ): String

    @GET("planetary/apod")
    suspend fun getPictureOfDay(
        @Query("api_key")
        apiKey: String
    ): NetworkPictureOfDay
}

object AsteroidApi {
    val retrofitMoshiService: AsteroidApiService by lazy {
        retrofit_moshi.create(AsteroidApiService::class.java)
    }
}