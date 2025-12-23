package com.example.my_music

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.my_music.PlayerActivity.Companion.binding
import com.example.my_music.PlayerActivity.Companion.musicListPA
import com.example.my_music.PlayerActivity.Companion.songPosition

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action){
            applicationClass.PREVIOUS -> PrevNextSong(increment = false, context= context!!)
            applicationClass.NEXT -> PrevNextSong(increment = true, context= context!!)
            applicationClass.PLAY -> if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
            applicationClass.EXIT -> {
                exitApplication()
            }

        }
    }
    private fun playMusic(){
        PlayerActivity.isPlaying= true
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
        PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.pause_ic)
        NowPlaying.binding.playPauseBtnNp.setIconResource(R.drawable.pause_ic)
    }
    private fun pauseMusic(){
        PlayerActivity.isPlaying= false
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        PlayerActivity.musicService!!.showNotification(R.drawable.play_ic)
        PlayerActivity.binding.playPauseBtn.setImageResource(R.drawable.play_ic)
        NowPlaying.binding.playPauseBtnNp.setIconResource(R.drawable.play_ic)
    }

    private fun PrevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment= increment)
        PlayerActivity.musicService!!.createdMediaPlayer()
        Glide.with(context)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.default_music_icon).centerCrop())
            .into(binding.songImagePA)
        binding.currentSongPA.text = musicListPA[songPosition].title
        playMusic()

    }
}