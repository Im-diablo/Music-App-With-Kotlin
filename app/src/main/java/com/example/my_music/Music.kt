package com.example.my_music

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import com.example.my_music.PlayerActivity.Companion.musicListPA
import com.example.my_music.PlayerActivity.Companion.songPosition
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

class Music (val id:String, val title:String, val album:String, val artist:String, val duration: Long = 0, val path:String,
var artUri:String)

@SuppressLint("DefaultLocale")
fun formatDuration(duration: Long):String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) - minutes*TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImageArt(path: String): ByteArray?{
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if(!PlayerActivity.repeat){
        if(increment) {
            if (musicListPA.size - 1 == songPosition)
                songPosition = 0
            else ++songPosition
        }else{
            if (0 == songPosition)
                songPosition = musicListPA.size - 1
            else --songPosition
        }
    }
}

fun exitApplication(){
    if(PlayerActivity.musicService != null){
        // Save favorites before exiting
        val editor = PlayerActivity.musicService!!.getSharedPreferences("FAV_SONGS", android.content.Context.MODE_PRIVATE).edit()
        val jsonString = com.google.gson.GsonBuilder().create().toJson(FavActivity.favSongs)
        editor.putString("FavSongs", jsonString)
        editor.commit()  // Use commit() instead of apply() to ensure synchronous save before exit
        
        PlayerActivity.musicService!!.stopForeground(true)
        PlayerActivity.musicService!!.mediaPlayer!!.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun favChecker(id: String): Int{
    PlayerActivity.isFav = false
    FavActivity.favSongs.forEachIndexed { index, music ->
        if(id == music.id){
            PlayerActivity.isFav = true
            return index
        }
    }
    return -1
}