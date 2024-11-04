package com.example.clearsky.notifications.view

import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.clearsky.R
import com.example.clearsky.databinding.FragmentNotificationsBinding
import com.example.clearsky.model.NotificationCard
import com.example.clearsky.notifications.receiver.NotificationReceiver
import com.example.clearsky.notifications.reciever.AlarmReceiver
import com.example.clearsky.notifications.viewmodel.NotificationsViewModel
import com.example.clearsky.notifications.viewmodel.NotificationsViewModelFactory
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences

    private lateinit var startDateEditText: EditText
    private lateinit var startTimeEditText: EditText
    private lateinit var endDateEditText: EditText
    private lateinit var endTimeEditText: EditText
    private lateinit var adapter: NotificationAdapter
    private val calendar = Calendar.getInstance()
    private val viewModel: NotificationsViewModel by viewModels { NotificationsViewModelFactory() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        sharedPreferences = requireContext().getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE)

        applyPreferences()
        setupSharedPreferencesListener()

        return binding.root
    }

    private fun applyPreferences() {
        val language = sharedPreferences.getString("language", "Default") ?: "Default"
        updateLanguage(language)
    }

    private fun updateLanguage(language: String) {
        when (language) {
            "Arabic" -> Locale.setDefault(Locale("ar"))
            "English" -> Locale.setDefault(Locale("en"))
            else -> Locale.setDefault(Locale.getDefault())
        }
    }

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

        startDateEditText = binding.startDate
        startTimeEditText = binding.startTime
        endDateEditText = binding.endDate
        endTimeEditText = binding.endTime

        adapter = NotificationAdapter(viewModel.notifications.value ?: mutableListOf())
        binding.recyclerViewNotifications.adapter = adapter
        binding.recyclerViewNotifications.layoutManager = LinearLayoutManager(requireContext())

        // Observe changes in notifications
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            adapter = NotificationAdapter(notifications)
            binding.recyclerViewNotifications.adapter = adapter
        }

        startDateEditText.setOnClickListener { showDatePickerDialog(startDateEditText) }
        startTimeEditText.setOnClickListener { showTimePickerDialog(startTimeEditText) }
        endDateEditText.setOnClickListener { showDatePickerDialog(endDateEditText) }
        endTimeEditText.setOnClickListener { showTimePickerDialog(endTimeEditText) }

        checkOverlayPermission()

        binding.btnSaveNotification.setOnClickListener {
            if (checkNotificationPermission()) {
                saveNotificationSettings()
            } else {
                requestNotificationPermission()
            }
        }

        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for notification sound"
            }
            val notificationManager = requireContext().getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun checkOverlayPermission() {
        if (!Settings.canDrawOverlays(requireContext())) {
            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${requireContext().packageName}"))
            startActivity(intent)
        }
    }

    private fun checkNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requireContext().checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), NOTIFICATION_PERMISSION_CODE)
        }
    }

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

    private fun showTimePickerDialog(editText: EditText) {
        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, hourOfDay, minute ->
                editText.setText(String.format("%02d:%02d", hourOfDay, minute))
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun saveNotificationSettings() {
        val startDate = "${binding.startDate.text} "
        val startTime = "${binding.startTime.text}"
        val endDate = "${binding.endDate.text} "
        val endTime = "${binding.endTime.text}"
        val type = binding.radioGroupNotificationType.checkedRadioButtonId

        val newNotification = NotificationCard(
            startTime = startTime,
            startDate = startDate,
            endTime = endTime,
            endDate = endDate,
            notificationType = type.toString()
        )

        viewModel.addNotification(newNotification)

        when (type) {
            R.id.radio_alarm_sound -> {
                scheduleAlarm(requireContext(), startDate, startTime)
                Toast.makeText(requireContext(), "Notification saved!", Toast.LENGTH_SHORT).show()
            }
            R.id.radio_notification_only -> {
                scheduleNotification(requireContext(), startDate, startTime)
            }
        }
    }

    private fun scheduleNotification(context: Context, startDate: String, startTime: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val notificationTime = dateFormat.parse("$startDate $startTime") ?: return

        val delay = notificationTime.time - System.currentTimeMillis()
        if (delay > 0) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationReceiver>()
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        } else {
            Toast.makeText(context, "Selected time is in the past!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleAlarm(context: Context, startDate: String, startTime: String) {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val notificationTime = dateFormat.parse("$startDate $startTime") ?: return

        Log.d("NotificationsFragment", "Scheduled Alarm Time: ${notificationTime.time}, Current Time: ${System.currentTimeMillis()}")

        if (notificationTime.time < System.currentTimeMillis()) {
            Toast.makeText(context, "Selected time is in the past!", Toast.LENGTH_SHORT).show()
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(context, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(context, 0, alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                notificationTime.time,
                pendingIntent
            )
        } else {
            alarmManager.setExact(
                AlarmManager.RTC_WAKEUP,
                notificationTime.time,
                pendingIntent
            )
        }

        Toast.makeText(context, "Alarm set for $startDate $startTime", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sharedPreferences.unregisterOnSharedPreferenceChangeListener { _, _ -> }
        _binding = null
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "notification_channel_id"
        const val NOTIFICATION_PERMISSION_CODE = 1234
    }
}