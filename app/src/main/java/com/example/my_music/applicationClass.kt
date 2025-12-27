package com.example.my_music

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build

class applicationClass: Application() {

    companion object {
        const val CHANNEL_ID = "channel1"
        const val PLAY = "play"
        const val NEXT = "next"
        const val PREVIOUS = "previous"
        const val EXIT = "exit"
    }

    override fun onCreate() {
        super.onCreate()
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val notificationChannel = NotificationChannel(CHANNEL_ID,"Now Playing",NotificationManager.IMPORTANCE_LOW)
            notificationChannel.description = "Important Channel For Showing Current Song"
            notificationChannel.setSound(null, null) // Disable sound
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}