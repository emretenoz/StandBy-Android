package com.emretenoz.standby.data.weather

import com.emretenoz.standby.data.settings.WorldClockZone
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import org.json.JSONObject

enum class WeatherKind { CLEAR, PARTLY_CLOUDY, CLOUDY, FOG, RAIN, SNOW, THUNDERSTORM }

data class WeatherState(
    val enabled: Boolean = false,
    val isLoading: Boolean = false,
    val temperatureCelsius: Double? = null,
    val apparentTemperatureCelsius: Double? = null,
    val windSpeedKmh: Double? = null,
    val kind: WeatherKind? = null,
    val isDay: Boolean = true,
    val hasError: Boolean = false,
)

class WeatherRepository {
    fun observe(enabled: Boolean, location: WorldClockZone): Flow<WeatherState> = flow {
        if (!enabled) {
            emit(WeatherState())
            return@flow
        }

        var previous: WeatherState? = null
        while (true) {
            emit(previous?.copy(isLoading = true, hasError = false) ?: WeatherState(enabled = true, isLoading = true))
            val result = runCatching { fetch(location) }
            previous = result.getOrElse {
                (previous ?: WeatherState(enabled = true)).copy(isLoading = false, hasError = true)
            }
            emit(previous)
            delay(if (result.isSuccess) REFRESH_INTERVAL_MILLIS else RETRY_INTERVAL_MILLIS)
        }
    }.flowOn(Dispatchers.IO)

    private fun fetch(location: WorldClockZone): WeatherState {
        val endpoint = buildString {
            append("https://api.open-meteo.com/v1/forecast")
            append("?latitude=${location.latitude}")
            append("&longitude=${location.longitude}")
            append("&current=temperature_2m,apparent_temperature,weather_code,is_day,wind_speed_10m")
            append("&temperature_unit=celsius&wind_speed_unit=kmh&timezone=auto")
        }
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        return try {
            connection.requestMethod = "GET"
            connection.connectTimeout = 8_000
            connection.readTimeout = 8_000
            connection.setRequestProperty("Accept", "application/json")
            connection.setRequestProperty("User-Agent", "StandBy-Android/1.0")
            if (connection.responseCode !in 200..299) {
                error("Weather request failed with HTTP ${connection.responseCode}")
            }
            parseWeatherResponse(connection.inputStream.bufferedReader().use { it.readText() })
        } finally {
            connection.disconnect()
        }
    }

    internal fun parseWeatherResponse(json: String): WeatherState {
        val current = JSONObject(json).getJSONObject("current")
        return WeatherState(
            enabled = true,
            temperatureCelsius = current.getDouble("temperature_2m"),
            apparentTemperatureCelsius = current.optDouble("apparent_temperature").takeUnless { it.isNaN() },
            windSpeedKmh = current.optDouble("wind_speed_10m").takeUnless { it.isNaN() },
            kind = weatherKindForCode(current.getInt("weather_code")),
            isDay = current.optInt("is_day", 1) == 1,
        )
    }

    companion object {
        private const val REFRESH_INTERVAL_MILLIS = 30 * 60 * 1_000L
        private const val RETRY_INTERVAL_MILLIS = 5 * 60 * 1_000L
    }
}

internal fun weatherKindForCode(code: Int): WeatherKind = when (code) {
    0 -> WeatherKind.CLEAR
    1, 2 -> WeatherKind.PARTLY_CLOUDY
    3 -> WeatherKind.CLOUDY
    45, 48 -> WeatherKind.FOG
    in 51..67, in 80..82 -> WeatherKind.RAIN
    in 71..77, 85, 86 -> WeatherKind.SNOW
    in 95..99 -> WeatherKind.THUNDERSTORM
    else -> WeatherKind.CLOUDY
}
