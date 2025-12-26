package com.example.my_music

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.my_music.databinding.MusicViewBinding

class MusicAdapter(private val context: Context, private var musicList: ArrayList<Music>, private val playlistDetails: Boolean = false,
 private val selectionActivity: Boolean= false)
    : RecyclerView.Adapter<MusicAdapter.MyHolder>() {
    class MyHolder(binding: MusicViewBinding): RecyclerView.ViewHolder(binding.root){
        val title = binding.songName
        val album = binding.songAlbum
        val image = binding.musicView
        val duration = binding.songDuration
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): MyHolder {
        return MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    @SuppressLint("CheckResult")
    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.default_music_icon).centerCrop())
            .into(holder.image)
        when {
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlaylistDetailsAdapter", pos = position)
                }
            }

            selectionActivity -> {
                val isSelected = checkIfExists(musicList[position])
                holder.root.setBackgroundColor(
                    if (isSelected) ContextCompat.getColor(context, R.color.cool_pink)
                    else ContextCompat.getColor(context, R.color.white)
                )

                holder.root.setOnClickListener {
                    if (addSong(musicList[position])) {
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.cool_pink))
                    } else {
                        holder.root.setBackgroundColor(ContextCompat.getColor(context, R.color.white))
                    }
                }
            }
            else -> {
                holder.root.setOnClickListener {
                    when {
                        MainActivity.search -> sendIntent(("MusicAdapterSearch"), pos = position)
                        musicList[position].id == PlayerActivity.nowPlayingId ->
                            sendIntent("NowPlaying", pos = PlayerActivity.songPosition)
                        else -> sendIntent("MusicAdapter", pos = position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList : ArrayList<Music>){
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, pos: Int){
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", pos)
        intent.putExtra("class", ref)
        context.startActivity(intent ,null)
    }

    private fun checkIfExists(song: Music): Boolean {
        if (PlaylistDetails.currentPlaylistPos < 0 || PlaylistDetails.currentPlaylistPos >= PlaylistActivity.musicPlaylist.ref.size) {
            return false
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEach { music ->
            if (song.id == music.id) {
                return true
            }
        }
        return false
    }

    private fun addSong(song: Music): Boolean{
        if (PlaylistDetails.currentPlaylistPos < 0 || PlaylistDetails.currentPlaylistPos >= PlaylistActivity.musicPlaylist.ref.size) {
            return false
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id == music.id) {
                PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(
                    index
                )
                return false
            }
        }
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    fun refreshPlaylist(){
        musicList = ArrayList()
        if (PlaylistDetails.currentPlaylistPos >= 0 && PlaylistDetails.currentPlaylistPos < PlaylistActivity.musicPlaylist.ref.size) {
            musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        }
        notifyDataSetChanged()
    }
}