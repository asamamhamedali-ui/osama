package com.example.data.calculation

import kotlin.math.*

object QiblaCalculator {
    const val KAABA_LATITUDE = 21.422487
    const val KAABA_LONGITUDE = 39.826206
    private const val EARTH_RADIUS_KM = 6371.0

    data class QiblaResult(
        val bearingDegrees: Float,
        val distanceKm: Int
    )

    fun calculate(userLat: Double, userLng: Double): QiblaResult {
        val lat1 = Math.toRadians(userLat)
        val lng1 = Math.toRadians(userLng)
        val lat2 = Math.toRadians(KAABA_LATITUDE)
        val lng2 = Math.toRadians(KAABA_LONGITUDE)

        val deltaLng = lng2 - lng1

        // Great circle initial bearing
        val y = sin(deltaLng) * cos(lat2)
        val x = cos(lat1) * sin(lat2) - sin(lat1) * cos(lat2) * cos(deltaLng)
        var bearing = Math.toDegrees(atan2(y, x))
        bearing = (bearing + 360.0) % 360.0

        // Distance using Haversine formula
        val dLat = lat2 - lat1
        val dLng = deltaLng
        val a = sin(dLat / 2) * sin(dLat / 2) +
                cos(lat1) * cos(lat2) *
                sin(dLng / 2) * sin(dLng / 2)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = (EARTH_RADIUS_KM * c).toInt()

        return QiblaResult(
            bearingDegrees = bearing.toFloat(),
            distanceKm = distance
        )
    }
}
