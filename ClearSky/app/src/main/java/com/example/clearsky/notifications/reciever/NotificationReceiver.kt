package com.example.clearsky.notifications.receiver

import android.content.Context
import android.app.NotificationManager
import androidx.core.app.NotificationCompat
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.clearsky.R
import com.example.clearsky.db.WeatherLocalDataSource
import com.example.clearsky.model.repository.WeatherRepository
import com.example.clearsky.network.RemoteDataStructure
import com.example.clearsky.notifications.view.NotificationsFragment
import kotlinx.coroutines.runBlocking

class NotificationReceiver(
    context: Context,
    params: WorkerParameters
) : Worker(context, params) {

    private val weatherRepository = WeatherRepository(
        remoteDataStructure = RemoteDataStructure,
        localDataSource = WeatherLocalDataSource.getInstance(context)
    )

    override fun doWork(): Result {
        val notificationManager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val sharedPreferences = applicationContext.getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        val lat = sharedPreferences.getFloat("lat", 30.0444f).toDouble()
        val lng = sharedPreferences.getFloat("lon", 31.2357f).toDouble()
        val tempUnit = sharedPreferences.getString("tempUnit", "metric") ?: "metric"
        val language = sharedPreferences.getString("language", "en") ?: "en"

        var contentTitle: String
        var contentText: String

        runBlocking {
            val weather = weatherRepository.getCurrentWeather(lat, lng, tempUnit, language)
            val tempInKelvin = weather.main.temp
            val tempInCelsius = tempInKelvin - 273.15 // Convert from Kelvin to Celsius
            val condition = weather.weather[0].description.replaceFirstChar { it.uppercaseChar() }

            if (tempInCelsius > 35 || tempInCelsius < 5) {
                contentTitle = "ClearSky has bad news for you :|"
                contentText = "Unfavorable weather: $condition, Temperature: %.2f°C. Please stay safe indoors.".format(tempInCelsius)
            } else {
                contentTitle = "ClearSky has good news for you :)"
                contentText = "Mild weather: $condition, Temperature: %.2f°C. Enjoy your day!".format(tempInCelsius)
            }
        }

        val notification = NotificationCompat.Builder(applicationContext, NotificationsFragment.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(contentTitle)
            .setContentText(contentText)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(0, notification)

        return Result.success()
    }
}
