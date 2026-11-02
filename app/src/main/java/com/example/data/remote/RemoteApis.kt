package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

// Aladhan API DTOs
data class AladhanResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: AladhanData?
)

data class AladhanData(
    @Json(name = "timings") val timings: Map<String, String>,
    @Json(name = "date") val date: AladhanDate
)

data class AladhanDate(
    @Json(name = "readable") val readable: String,
    @Json(name = "hijri") val hijri: AladhanHijri?
)

data class AladhanHijri(
    @Json(name = "date") val date: String,
    @Json(name = "format") val format: String,
    @Json(name = "day") val day: String,
    @Json(name = "year") val year: String,
    @Json(name = "month") val month: AladhanHijriMonth?
)

data class AladhanHijriMonth(
    @Json(name = "number") val number: Int,
    @Json(name = "en") val en: String,
    @Json(name = "ar") val ar: String
)

interface AladhanApiService {
    @GET("v1/timings")
    suspend fun getTimings(
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") methodId: Int
    ): AladhanResponse
}

// AlQuran Cloud API DTOs
data class QuranSurahResponse(
    @Json(name = "code") val code: Int,
    @Json(name = "status") val status: String,
    @Json(name = "data") val data: List<QuranEditionData>?
)

data class QuranEditionData(
    @Json(name = "number") val number: Int,
    @Json(name = "name") val name: String,
    @Json(name = "englishName") val englishName: String,
    @Json(name = "ayahs") val ayahs: List<QuranRemoteAyah>
)

data class QuranRemoteAyah(
    @Json(name = "number") val number: Int,
    @Json(name = "text") val text: String,
    @Json(name = "numberInSurah") val numberInSurah: Int,
    @Json(name = "juz") val juz: Int,
    @Json(name = "audio") val audio: String?
)

interface AlQuranCloudApiService {
    @GET("v1/surah/{number}/editions/quran-uthmani,en.sahih,ar.alafasy")
    suspend fun getSurahEditions(
        @Path("number") surahNumber: Int
    ): QuranSurahResponse
}

object ApiClient {
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .addInterceptor(HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        })
        .build()

    val aladhanApi: AladhanApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.aladhan.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AladhanApiService::class.java)
    }

    val quranApi: AlQuranCloudApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.alquran.cloud/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AlQuranCloudApiService::class.java)
    }
}
