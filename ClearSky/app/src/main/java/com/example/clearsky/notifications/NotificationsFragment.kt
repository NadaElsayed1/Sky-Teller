package com.example.clearsky.notifications

import android.Manifest
import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.example.clearsky.R
import com.example.clearsky.databinding.FragmentNotificationsBinding
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    // EditText fields for setting start and end times and dates
    private lateinit var startDateEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var adapter: NotificationAdapter
    private val calendar = Calendar.getInstance()
    private val notificationsList = mutableListOf<NotificationCard>() // List of notifications

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        // Initialize shared preferences for app settings
        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        // Apply language preferences when the view is created
        applyPreferences()

        // Set up a listener to apply changes when shared preferences are updated
        setupSharedPreferencesListener()

        return binding.root
    }

    // Apply saved language settings based on the stored preference
    private fun applyPreferences() {
        val language = sharedPreferences.getString("language", "Default") ?: "Default"
        updateLanguage(language)
    }

    // Update the language of the app based on the chosen preference
    private fun updateLanguage(language: String) {
        when (language) {
            "Arabic" -> Locale.setDefault(Locale("ar")) // Arabic language setting
            "English" -> Locale.setDefault(Locale("en")) // English language setting
            else -> Locale.setDefault(Locale.getDefault()) // Default system language
        }
    }

    // Set up a listener to monitor changes in shared preferences
    private fun setupSharedPreferencesListener() {
        sharedPreferences.registerOnSharedPreferenceChangeListener { sharedPrefs, key ->
            when (key) {
                "language" -> {
                    val language = sharedPrefs.getString(key, "Default")
                    updateLanguage(language ?: "Default")
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize EditText references for date and time inputs
        startDateEditText = binding.startDate
        startTimeEditText = binding.startTime
        endDateEditText = binding.endDate
        endTimeEditText = binding.endTime

        // Set up the notification adapter and assign it to RecyclerView
        adapter = NotificationAdapter(notificationsList)
        binding.recyclerViewNotifications.adapter = adapter
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(requireContext())

        // Set click listeners to open date and time pickers
        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        startTimeEditText.setOnClickListener { showTimePickerDialog(startTimeEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }
        endTimeEditText.setOnClickListener { showTimePickerDialog(endTimeEditText) }

        // Check if the app has permission to draw over other apps
        checkOverlayPermission()

        // Set up the save button to save notification settings
        binding.btnSaveNotification.setOnClickListener {
            if (checkNotificationPermission()) {
                saveNotificationSettings(adapter)
            } else {
                requestNotificationPermission()
            }
        }

        // Create a notification channel for displaying notifications
        createNotificationChannel()
    }

    // Create a notification channel for sending notifications
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for notification sound"
            }
            val notificationManager = requireContext().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Check if the app has permission to overlay on other apps
    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
            startActivity(intent)
        }
    }

    // Check if the app has notification permission for Android 13 and above
    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // Permissions are granted by default for older Android versions
        }
    }

    // Request notification permission for Android 13 and above
    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
        }
    }

    // Show a date picker dialog to the user for selecting a date
    private fun showDatePickerDialog(editText: EditText) {
        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, dayOfMonth ->
                editText.setText(String.format("%02d/%02d/%d", dayOfMonth, month + 1, year))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    // Show a time picker dialog to the user for selecting a time
    private fun showTimePickerDialog(editText: EditText) {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                editText.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true // Use 24-hour time format
        )
        timePickerDialog.show()
    }

    // Save notification settings and schedule the notification
    private fun saveNotificationSettings(adapter: NotificationAdapter) {
        val startDate = "${binding.startDate.text} "
        val startTime = "${binding.startTime.text}"
        val endDate = "${binding.endDate.text} "
        val endTime = "${binding.endTime.text}"
        val type = "${binding.radioGroupNotificationType.checkedRadioButtonId}" // Fetch the selected type

        val newNotification = NotificationCard(startTime = startTime, startDate = startDate, endTime = endTime, endDate = endDate, notificationType = type)

        adapter.addNotificationCard(newNotification)
        scheduleNotification(requireContext(),startDate,startTime)

        Toast.makeText(requireContext(), "Notification saved!", Toast.LENGTH_SHORT).show()
    }



    // Schedule a notification at the specified start time
//    private fun scheduleNotification(startTime: String) {
//        val alarmManager = requireActivity().getSystemService(Context.ALARM_SERVICE) as AlarmManager
//        val intent = Intent(requireContext(), NotificationReceiver::class.java)
//        val pendingIntent = PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
//
//        // Set the notification time in milliseconds (currently set to 1 minute from now)
//        val notificationTimeInMillis = System.currentTimeMillis() + 60000
//        alarmManager.setExact(AlarmManager.RTC_WAKEUP, notificationTimeInMillis, pendingIntent)
//    }

    // To schedule the notification
//    private fun scheduleNotification(context: Context, startTime: String) {
//        val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//            .setInitialDelay(1, TimeUnit.MINUTES) // Adjust this to the actual time based on startTime
//            .build()
//
//        WorkManager.getInstance(context).enqueue(workRequest)
//    }

    private fun scheduleNotification(context: Context, startDate: String, startTime: String) {
        // Parse the user input to create a Calendar instance for the scheduled time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val notificationTime = dateFormat.parse("$startDate $startTime") ?: return

        // Calculate the delay from the current time
        val delay = notificationTime.time - System.currentTimeMillis()
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        } else {
            Toast.makeText(context, "Selected time is in the past!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Unregister the shared preference listener to prevent memory leaks
        sharedPreferences.unregisterOnSharedPreferenceChangeListener { _, _ -> }
        _binding = null
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        const val NOTIFICATION_PERMISSION_CODE = 1234
    }
}

// BroadcastReceiver class to handle the display of notifications when triggered
//class NotificationReceiver : BroadcastReceiver() {
//    override fun onReceive(context: Context, intent: Intent) {
//        val notification = NotificationCompat.Builder(context, NotificationsFragment.NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("Scheduled Notification")
//            .setContentText("This is a notification message.")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//            .build()
//
//        // Check if the app has permission to post notifications
//        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
//            // Return if permission is not granted
//            return
//        }
//
//        // Display the notification
//        NotificationManagerCompat.from(context).notify(Random().nextInt(), notification)
//    }
//}

class NotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // Create a notification
        val notification = NotificationCompat.Builder(context, NotificationsFragment.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scheduled Notification")
            .setContentText("This is a notification message.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        // Check for notification permission
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            NotificationManagerCompat.from(context).notify(0, notification) // Use a fixed ID for simplicity
        }

        // Play alarm sound
        playAlarmSound(context)
    }

    private fun playAlarmSound(context: Context) {
        val alarmUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        val mediaPlayer = MediaPlayer.create(context, alarmUri)
        mediaPlayer.setOnCompletionListener {
            it.release() // Release the MediaPlayer resources after playback
        }
        mediaPlayer.start()
    }
}

// Worker implementation remains the same
class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
    override fun doWork(): Result {
        // Check for notification permission and create notification
        if (ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return Result.failure()
        }

        val notification = NotificationCompat.Builder(applicationContext, NotificationsFragment.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Scheduled Notification")
            .setContentText("This is a notification message.")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        NotificationManagerCompat.from(applicationContext).notify(0, notification) // Use a fixed ID

        return Result.success()
    }
}

