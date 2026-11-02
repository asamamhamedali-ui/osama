package com.example.data.calculation

import com.example.data.model.CalculationMethod
import com.example.data.model.DayPrayerSchedule
import com.example.data.model.JuristicSchool
import com.example.data.model.PrayerType
import com.example.data.model.SinglePrayerTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.chrono.HijrahChronology
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.*

object AstronomicalPrayerCalculator {

    fun calculate(
        latitude: Double,
        longitude: Double,
        date: LocalDate = LocalDate.now(),
        zoneId: ZoneId = ZoneId.systemDefault(),
        method: CalculationMethod = CalculationMethod.EGYPTIAN,
        school: JuristicSchool = JuristicSchool.SHAFI,
        fajrAdjustment: Int = 0,
        dhuhrAdjustment: Int = 0,
        asrAdjustment: Int = 0,
        maghribAdjustment: Int = 0,
        ishaAdjustment: Int = 0,
        hijriDayOffset: Long = 0,
        cityName: String = "موقعي الحالي",
        countryName: String = ""
    ): DayPrayerSchedule {
        val timeZoneOffset = zoneId.rules.getOffset(date.atStartOfDay()).totalSeconds / 3600.0

        val d = julianDays(date.year, date.monthValue, date.dayOfMonth) - 2451545.0
        val g = fixAngle(357.529 + 0.98560028 * d)
        val q = fixAngle(280.459 + 0.98564736 * d)
        val l = fixAngle(q + 1.915 * sin(degToRad(g)) + 0.020 * sin(degToRad(2 * g)))
        val e = 23.439 - 0.00000036 * d
        val ra = radToDeg(atan2(cos(degToRad(e)) * sin(degToRad(l)), cos(degToRad(l)))) / 15.0
        val eqt = q / 15.0 - fixHour(ra)
        val decl = radToDeg(asin(sin(degToRad(e)) * sin(degToRad(l))))

        // Solar transit (Dhuhr)
        val noon = fixHour(12.0 + timeZoneOffset - longitude / 15.0 - eqt)

        // Sunrise & Sunset (sun center altitude = -0.833° for refraction and semidiameter)
        val sunriseHour = noon - hourAngle(-0.833, latitude, decl)
        val sunsetHour = noon + hourAngle(-0.833, latitude, decl)

        // Fajr (depression angle below horizon: altitude = -fajrAngle)
        val fajrHour = noon - hourAngle(-method.fajrAngle, latitude, decl)

        // Asr (solar altitude above horizon: arccot(t + shadowFactor))
        val term = school.shadowFactor + tan(degToRad(abs(latitude - decl)))
        val asrAltitude = radToDeg(atan(1.0 / term))
        val asrHour = noon + hourAngle(asrAltitude, latitude, decl)

        // Maghrib
        val maghribHour = sunsetHour

        // Isha
        val ishaHour = if (method.ishaIntervalMinutes != null) {
            maghribHour + method.ishaIntervalMinutes / 60.0
        } else {
            noon + hourAngle(-method.ishaAngle, latitude, decl)
        }

        fun toLocalDateTime(hourVal: Double, minuteAdjustment: Int): LocalDateTime {
            val adjustedHour = fixHour(hourVal)
            val h = floor(adjustedHour).toInt().coerceIn(0, 23)
            val m = floor((adjustedHour - h) * 60.0).toInt().coerceIn(0, 59)
            val s = floor(((adjustedHour - h) * 60.0 - m) * 60.0).toInt().coerceIn(0, 59)
            val baseTime = LocalDateTime.of(date, LocalTime.of(h, m, s))
            return baseTime.plusMinutes(minuteAdjustment.toLong())
        }

        val fajrTime = toLocalDateTime(fajrHour, fajrAdjustment)
        val sunriseTime = toLocalDateTime(sunriseHour, 0)
        val dhuhrTime = toLocalDateTime(noon, dhuhrAdjustment)
        val asrTime = toLocalDateTime(asrHour, asrAdjustment)
        val maghribTime = toLocalDateTime(maghribHour, maghribAdjustment)
        val ishaTime = toLocalDateTime(ishaHour, ishaAdjustment)

        val prayersList = listOf(
            SinglePrayerTime(PrayerType.FAJR, fajrTime),
            SinglePrayerTime(PrayerType.SUNRISE, sunriseTime),
            SinglePrayerTime(PrayerType.DHUHR, dhuhrTime),
            SinglePrayerTime(PrayerType.ASR, asrTime),
            SinglePrayerTime(PrayerType.MAGHRIB, maghribTime),
            SinglePrayerTime(PrayerType.ISHA, ishaTime)
        )

        val now = LocalDateTime.now(zoneId)

        // Determine next prayer
        val next = prayersList.firstOrNull { it.time.isAfter(now) }
            ?: SinglePrayerTime(PrayerType.FAJR, fajrTime.plusDays(1))

        // Determine current prayer
        var currentPrayerType: PrayerType? = null
        for (i in prayersList.indices.reversed()) {
            if (now.isAfter(prayersList[i].time) || now.isEqual(prayersList[i].time)) {
                currentPrayerType = prayersList[i].type
                break
            }
        }

        val updatedPrayers = prayersList.map { p ->
            p.copy(
                isCurrent = p.type == currentPrayerType,
                isNext = p.type == next.type && (p.time == next.time || p.time.isEqual(next.time.minusDays(1)))
            )
        }

        val timeRemaining = ChronoUnit.MILLIS.between(now, next.time).coerceAtLeast(0L)

        // Hijri date string
        val hijriFormatted = try {
            val adjustedDate = date.plusDays(hijriDayOffset)
            val hijri = HijrahChronology.INSTANCE.date(adjustedDate)
            val arabicFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("ar"))
            arabicFormatter.format(hijri) + " هـ"
        } catch (e: Exception) {
            ""
        }

        val gregorianFormatted = date.format(DateTimeFormatter.ofPattern("EEEE, d MMMM yyyy", Locale.getDefault()))

        return DayPrayerSchedule(
            gregorianDate = gregorianFormatted,
            hijriDate = hijriFormatted,
            cityName = cityName,
            countryName = countryName,
            prayers = updatedPrayers,
            nextPrayer = next,
            timeRemainingMillis = timeRemaining
        )
    }

    private fun hourAngle(altitude: Double, latitude: Double, declination: Double): Double {
        val sinAlt = sin(degToRad(altitude))
        val sinLat = sin(degToRad(latitude))
        val sinDecl = sin(degToRad(declination))
        val cosLat = cos(degToRad(latitude))
        val cosDecl = cos(degToRad(declination))

        val denom = cosLat * cosDecl
        if (denom == 0.0) return 0.0
        val cosOmega = (sinAlt - sinLat * sinDecl) / denom
        val clamped = cosOmega.coerceIn(-1.0, 1.0)
        return radToDeg(acos(clamped)) / 15.0
    }

    private fun julianDays(year: Int, month: Int, day: Int): Double {
        var y = year
        var m = month
        if (m <= 2) {
            y -= 1
            m += 12
        }
        val a = floor(y / 100.0)
        val b = 2 - a + floor(a / 4.0)
        return floor(365.25 * (y + 4716)) + floor(30.6001 * (m + 1)) + day + b - 1524.5
    }

    private fun fixHour(h: Double): Double {
        var a = h - 24.0 * floor(h / 24.0)
        if (a < 0.0) a += 24.0
        return a
    }

    private fun fixAngle(a: Double): Double {
        var res = a - 360.0 * floor(a / 360.0)
        if (res < 0.0) res += 360.0
        return res
    }

    private fun degToRad(deg: Double) = deg * Math.PI / 180.0
    private fun radToDeg(rad: Double) = rad * 180.0 / Math.PI
}
