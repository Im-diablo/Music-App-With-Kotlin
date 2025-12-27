package com.example.my_music

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import androidx.core.content.edit
import com.example.my_music.PlayerActivity.Companion.musicListPA
import com.example.my_music.PlayerActivity.Companion.songPosition
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id:String, val title:String, val album:String, val artist:String, val duration: Long = 0, val path:String,
var artUri:String)

class Playlist{
    lateinit var name: String
    lateinit var playlist: ArrayList<Music>
    lateinit var createdBy: String
    lateinit var createdOn: String
}

class musicPlaylist{
    var ref: ArrayList<Playlist> = ArrayList()
}

@SuppressLint("DefaultLocale")
fun formatDuration(duration: Long):String {
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) - minutes*TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
    return String.format("%02d:%02d", minutes, seconds)
}

fun getImageArt(path: String): ByteArray?{
    val retriever = MediaMetadataRetriever()
    try {
        if (path.startsWith("content://")) {
            // For content URIs, return null (can't extract album art easily)
            return null
        } else {
            // For file paths
            retriever.setDataSource(path)
            return retriever.embeddedPicture
        }
    } catch (e: Exception) {
        return null
    } finally {
        retriever.release()
    }
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
        PlayerActivity.musicService!!.audioManager.abandonAudioFocus(PlayerActivity.musicService)
        // Save favorites, playlists, and theme before exiting
        PlayerActivity.musicService!!.getSharedPreferences(
            "FAV_SONGS",
            android.content.Context.MODE_PRIVATE
        ).edit(commit = true) {
            val jsonString = com.google.gson.GsonBuilder().create().toJson(FavActivity.favSongs)
            putString("FavSongs", jsonString)
            val jsonStringPlaylist = com.google.gson.GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
            putString("MusicPlaylist", jsonStringPlaylist)
        }
        
        // Save current theme
        PlayerActivity.musicService!!.getSharedPreferences(
            "THEMES",
            android.content.Context.MODE_PRIVATE
        ).edit(commit = true) {
            putInt("themeIndex", MainActivity.themeIndex)
        }
        
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

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music>{
    playlist.removeAll { music ->
        val file = java.io.File(music.path)
        !file.exists()
    }
    return playlist
}
