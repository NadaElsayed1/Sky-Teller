package com.example.clearsky.notifications.reciever

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.util.Log
import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.model.repository.WeatherRepository
import com.example.clearsky.network.RemoteDataStructure
import com.example.clearsky.notifications.view.AlertActivity
import kotlinx.coroutines.runBlocking

class AlarmReceiver : BroadcastReceiver() {

    private lateinit var weatherRepository: WeatherRepository
    companion object {
        var mediaPlayer: MediaPlayer? = null

        fun stopAlarm() {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                }
                it.release()
                mediaPlayer = null
            }
        }
    }
    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AlarmReceiver", "Alarm triggered")

        weatherRepository = WeatherRepository(
            remoteDataStructure = RemoteDataStructure,
            localDataSource = WeatherLocalDataSource.getInstance(context)
        )

        val sharedPreferences =
            context.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)
        val lat = sharedPreferences.getFloat("lat", 30.0444f).toDouble()
        val lng = sharedPreferences.getFloat("lon", 31.2357f).toDouble()
        val tempUnit = sharedPreferences.getString("tempUnit", "metric") ?: "metric"
        val language = sharedPreferences.getString("language", "en") ?: "en"

        var alertMessage: String

        runBlocking {
            val weather = weatherRepository.getCurrentWeather(lat, lng, tempUnit, language)
            val tempInKelvin = weather.main.temp
            val wind = weather.wind.speed
            val tempInCelsius = tempInKelvin - 273.15
            val condition =
                weather.weather[0].description.replaceFirstChar { it.uppercaseChar() }

            alertMessage = if (tempInCelsius > 35 || tempInCelsius < 5) {
                "Unfavorable weather: $condition, Temperature: %.2f°C. , %.2fm/s Please stay safe indoors.".format(
                    tempInCelsius, wind
                )
            } else {
                "Mild weather: $condition, Temperature: %.2f°C, %.2fm/s. Enjoy your day!".format(
                    tempInCelsius, wind
                )
            }
        }

        if (mediaPlayer == null) {
            val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            mediaPlayer = MediaPlayer.create(context, alarmSound)
            mediaPlayer?.start()
        }

        val alarmIntent = Intent(context, AlertActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("message", alertMessage)
        }

        context.startActivity(alarmIntent)
    }
}