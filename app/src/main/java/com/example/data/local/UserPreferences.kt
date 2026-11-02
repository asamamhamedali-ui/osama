package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.CalculationMethod
import com.example.data.model.JuristicSchool
import com.example.data.model.PrayerType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("adhan_rahmat_prefs", Context.MODE_PRIVATE)

    private val _methodFlow = MutableStateFlow(getCalculationMethod())
    val methodFlow: StateFlow<CalculationMethod> = _methodFlow.asStateFlow()

    private val _schoolFlow = MutableStateFlow(getJuristicSchool())
    val schoolFlow: StateFlow<JuristicSchool> = _schoolFlow.asStateFlow()

    private val _muezzinFlow = MutableStateFlow(getMuezzinSound())
    val muezzinFlow: StateFlow<String> = _muezzinFlow.asStateFlow()

    fun getLatitude(): Double = prefs.getFloat(KEY_LATITUDE, 21.4225f).toDouble()
    fun getLongitude(): Double = prefs.getFloat(KEY_LONGITUDE, 39.8262f).toDouble()
    fun getCityName(): String = prefs.getString(KEY_CITY, "مكة المكرمة") ?: "مكة المكرمة"
    fun getCountryName(): String = prefs.getString(KEY_COUNTRY, "المملكة العربية السعودية") ?: "السعودية"

    fun setLocation(lat: Double, lng: Double, city: String, country: String) {
        prefs.edit()
            .putFloat(KEY_LATITUDE, lat.toFloat())
            .putFloat(KEY_LONGITUDE, lng.toFloat())
            .putString(KEY_CITY, city)
            .putString(KEY_COUNTRY, country)
            .apply()
    }

    fun getCalculationMethod(): CalculationMethod {
        val name = prefs.getString(KEY_METHOD, CalculationMethod.EGYPTIAN.name)
        return try {
            CalculationMethod.valueOf(name ?: CalculationMethod.EGYPTIAN.name)
        } catch (e: Exception) {
            CalculationMethod.EGYPTIAN
        }
    }

    fun setCalculationMethod(method: CalculationMethod) {
        prefs.edit().putString(KEY_METHOD, method.name).apply()
        _methodFlow.value = method
    }

    fun getJuristicSchool(): JuristicSchool {
        val name = prefs.getString(KEY_SCHOOL, JuristicSchool.SHAFI.name)
        return try {
            JuristicSchool.valueOf(name ?: JuristicSchool.SHAFI.name)
        } catch (e: Exception) {
            JuristicSchool.SHAFI
        }
    }

    fun setJuristicSchool(school: JuristicSchool) {
        prefs.edit().putString(KEY_SCHOOL, school.name).apply()
        _schoolFlow.value = school
    }

    fun getMuezzinSound(): String {
        return prefs.getString(KEY_MUEZZIN, "makkah") ?: "makkah"
    }

    fun setMuezzinSound(sound: String) {
        prefs.edit().putString(KEY_MUEZZIN, sound).apply()
        _muezzinFlow.value = sound
    }

    fun isPrayerNotificationEnabled(prayer: PrayerType): Boolean {
        return prefs.getBoolean("notif_${prayer.name}", true)
    }

    fun setPrayerNotificationEnabled(prayer: PrayerType, enabled: Boolean) {
        prefs.edit().putBoolean("notif_${prayer.name}", enabled).apply()
    }

    fun getPrayerAdjustment(prayer: PrayerType): Int {
        return prefs.getInt("adj_${prayer.name}", 0)
    }

    fun setPrayerAdjustment(prayer: PrayerType, minutes: Int) {
        prefs.edit().putInt("adj_${prayer.name}", minutes).apply()
    }

    fun getHijriOffset(): Long = prefs.getLong(KEY_HIJRI_OFFSET, 0L)
    fun setHijriOffset(offset: Long) = prefs.edit().putLong(KEY_HIJRI_OFFSET, offset).apply()

    fun getQuranFontSize(): Float = prefs.getFloat(KEY_QURAN_FONT_SIZE, 22f)
    fun setQuranFontSize(size: Float) = prefs.edit().putFloat(KEY_QURAN_FONT_SIZE, size).apply()

    fun getShowTranslation(): Boolean = prefs.getBoolean(KEY_SHOW_TRANSLATION, true)
    fun setShowTranslation(show: Boolean) = prefs.edit().putBoolean(KEY_SHOW_TRANSLATION, show).apply()

    companion object {
        private const val KEY_LATITUDE = "pref_lat"
        private const val KEY_LONGITUDE = "pref_lng"
        private const val KEY_CITY = "pref_city"
        private const val KEY_COUNTRY = "pref_country"
        private const val KEY_METHOD = "pref_calculation_method"
        private const val KEY_SCHOOL = "pref_juristic_school"
        private const val KEY_MUEZZIN = "pref_muezzin_sound"
        private const val KEY_HIJRI_OFFSET = "pref_hijri_offset"
        private const val KEY_QURAN_FONT_SIZE = "pref_quran_font_size"
        private const val KEY_SHOW_TRANSLATION = "pref_show_translation"
    }
}
