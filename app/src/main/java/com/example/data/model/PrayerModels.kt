package com.example.data.model

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class PrayerType(val displayNameAr: String, val displayNameEn: String) {
    FAJR("الفجر", "Fajr"),
    SUNRISE("الشروق", "Sunrise"),
    DHUHR("الظهر", "Dhuhr"),
    ASR("العصر", "Asr"),
    MAGHRIB("المغرب", "Maghrib"),
    ISHA("العشاء", "Isha")
}

enum class CalculationMethod(
    val titleAr: String,
    val titleEn: String,
    val fajrAngle: Double,
    val ishaAngle: Double,
    val ishaIntervalMinutes: Int? = null
) {
    EGYPTIAN("الهيئة العامة المصرية للمساحة", "Egyptian General Authority", 19.5, 17.5),
    UMM_AL_QURA("أم القرى - مكة المكرمة", "Umm Al-Qura, Makkah", 18.5, 0.0, 90),
    MUSLIM_WORLD_LEAGUE("رابطة العالم الإسلامي", "Muslim World League", 18.0, 17.0),
    ISNA("أمريكا الشمالية (ISNA)", "Islamic Society of North America", 15.0, 15.0),
    KARACHI("جامعة العلوم الإسلامية بكراتشي", "Univ. of Islamic Sciences, Karachi", 18.0, 18.0),
    DIYANET("رئاسة الشؤون الدينية التركية", "Diyanet, Turkey", 18.0, 17.0),
    TEHRAN("معهد الجيوفيزياء بجامعة طهران", "Institute of Geophysics, Tehran", 17.7, 14.0),
    GULF("دول الخليج العربي", "Gulf Region", 19.5, 0.0, 90)
}

enum class JuristicSchool(val titleAr: String, val titleEn: String, val shadowFactor: Double) {
    SHAFI("جمهور الفقهاء (شافعي، مالكي، حنبلي)", "Standard (Shafi, Maliki, Hanbali)", 1.0),
    HANAFI("المذهب الحنفي", "Hanafi School", 2.0)
}

data class SinglePrayerTime(
    val type: PrayerType,
    val time: LocalDateTime,
    val isCurrent: Boolean = false,
    val isNext: Boolean = false
) {
    fun formattedTime(locale: Locale = Locale.getDefault()): String {
        val pattern = "hh:mm a"
        return time.format(DateTimeFormatter.ofPattern(pattern, locale))
    }
}

data class DayPrayerSchedule(
    val gregorianDate: String,
    val hijriDate: String,
    val cityName: String,
    val countryName: String,
    val prayers: List<SinglePrayerTime>,
    val nextPrayer: SinglePrayerTime,
    val timeRemainingMillis: Long
)
