//package com.example.clearsky.notifications
//
//import android.Manifest
//import android.content.Context
//import android.content.pm.PackageManager
//import android.media.RingtoneManager
//import androidx.core.app.ActivityCompat
//import androidx.core.app.NotificationCompat
//import androidx.core.app.NotificationManagerCompat
//import androidx.work.OneTimeWorkRequestBuilder
//import androidx.work.WorkManager
//import androidx.work.Worker
//import androidx.work.WorkerParameters
//import com.example.clearsky.R
//import java.util.Random
//import java.util.concurrent.TimeUnit
//
//class NotificationWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {
//
//    override fun doWork(): Result {
//        // Check for notification permission
//        if (ActivityCompat.checkSelfPermission(
//                applicationContext,
//                Manifest.permission.POST_NOTIFICATIONS
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            // Permission is not granted; return failure
//            return Result.failure()
//        }
//
//        // Create notification
//        val notification = NotificationCompat.Builder(applicationContext, NotificationsFragment.NOTIFICATION_CHANNEL_ID)
//            .setSmallIcon(R.drawable.ic_notification)
//            .setContentTitle("Scheduled Notification")
//            .setContentText("This is a notification message.")
//            .setPriority(NotificationCompat.PRIORITY_HIGH)
//            .setAutoCancel(true)
//            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
//            .build()
//
//        // Notify using NotificationManagerCompat
//        NotificationManagerCompat.from(applicationContext).notify(Random().nextInt(), notification)
//
//        return Result.success()
//    }
//}
//
//// To schedule the notification
//fun scheduleNotification(context: Context) {
//    val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
//        .setInitialDelay(1, TimeUnit.MINUTES) // Adjust delay as per requirement
//        .build()
//
//    WorkManager.getInstance(context).enqueue(workRequest)
//}
