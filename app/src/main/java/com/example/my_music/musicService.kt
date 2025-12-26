package com.example.my_music

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import com.example.my_music.PlayerActivity.Companion.binding
import com.example.my_music.PlayerActivity.Companion.isPlaying
import com.example.my_music.PlayerActivity.Companion.musicListPA
import com.example.my_music.PlayerActivity.Companion.musicService
import com.example.my_music.PlayerActivity.Companion.nowPlayingId
import com.example.my_music.PlayerActivity.Companion.songPosition

class MusicService: Service(), AudioManager.OnAudioFocusChangeListener {
    private var myBinder = MyBinder()
    public var mediaPlayer: MediaPlayer? = null
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var runnable: Runnable
    lateinit var audioManager: AudioManager

    override fun onBind(intent: Intent?): IBinder {
        mediaSession = MediaSessionCompat(baseContext, "My Music")
        return myBinder
    }
    inner class MyBinder: Binder(){
        fun currentService(): MusicService{
            return this@MusicService
        }
    }

    fun showNotification(PlayPauseBtn: Int){

        // if we wanna open the player activity directy from notification, just uncomment it and comment the next line
//        val intent = Intent(baseContext, PlayerActivity::class.java)
//        intent.putExtra("index", songPosition)
//        intent.putExtra("class", "NowPlaying")
        val intent = Intent(baseContext, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val contentIntent = PendingIntent.getActivity(baseContext, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val prevIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(applicationClass.PREVIOUS)
        val prevPendingIntent = PendingIntent.getBroadcast(baseContext, 0, prevIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val nextIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(applicationClass.NEXT)
        val nextPendingIntent = PendingIntent.getBroadcast(baseContext, 0, nextIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val playIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(applicationClass.PLAY)
        val playPendingIntent = PendingIntent.getBroadcast(baseContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val exitIntent = Intent(baseContext, NotificationReceiver::class.java).setAction(applicationClass.EXIT)
        val exitPendingIntent = PendingIntent.getBroadcast(baseContext, 0, exitIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        val imageArt = getImageArt(PlayerActivity.musicListPA[PlayerActivity.songPosition].path)
        val image = if(imageArt != null){
            BitmapFactory.decodeByteArray(imageArt, 0, imageArt.size)
        }else{
            BitmapFactory.decodeResource(resources, R.mipmap.blaze_music_round)
        }

        val notification = NotificationCompat.Builder(baseContext, applicationClass.Companion.CHANNEL_ID)
            .setContentTitle(PlayerActivity.musicListPA[PlayerActivity.songPosition].title)
            .setContentText(PlayerActivity.musicListPA[PlayerActivity.songPosition].artist)
            .setSmallIcon(R.drawable.music_ic)  
            .setLargeIcon(image)
            .setStyle(androidx.media.app.NotificationCompat.MediaStyle().setMediaSession(mediaSession.sessionToken))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(contentIntent)  
            .setOnlyAlertOnce(true)
            .addAction(R.drawable.prev_ic, "Previous", prevPendingIntent)
            .addAction(PlayPauseBtn, "Play", playPendingIntent)
            .addAction(R.drawable.next_ic, "Next", nextPendingIntent)
            .addAction(R.drawable.exit_ic, "Exit", exitPendingIntent)
            .build()

        // Post notification first (required for Android 14+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val notificationManager = getSystemService(android.content.Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.notify(13, notification)
        }
        
        startForeground(13, notification)
    }

    fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            else musicService!!.mediaPlayer!!.reset()
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            PlayerActivity.isPlaying = true
            binding.playPauseBtn.setImageResource(R.drawable.pause_ic)
            musicService!!.showNotification(R.drawable.pause_ic)
            binding.seekbarStartTV.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekbarEndTV.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            nowPlayingId = musicListPA[songPosition].id
        }catch (e: Exception){return}
    }

    fun seekBarSetup(){
        runnable = Runnable {
            binding.seekbarStartTV.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekBarPA.progress = mediaPlayer!!.currentPosition
            Handler(Looper.getMainLooper()).postDelayed(runnable, 200)
        }
        Handler(Looper.getMainLooper()).postDelayed(runnable, 0)
    }

    override fun onAudioFocusChange(focusChange: Int) {
        if(focusChange <= 0){
            // Pause Music
            binding.playPauseBtn.setImageResource(R.drawable.play_ic)
            NowPlaying.binding.playPauseBtnNp.setIconResource(R.drawable.play_ic)
            showNotification(R.drawable.play_ic)
            isPlaying = false
            mediaPlayer!!.pause()
        }
        else {
            // Play Music
            binding.playPauseBtn.setImageResource(R.drawable.pause_ic)
            NowPlaying.binding.playPauseBtnNp.setIconResource(R.drawable.pause_ic)
            showNotification(R.drawable.pause_ic)
            isPlaying = true
            mediaPlayer!!.start()
        }
    }
}
