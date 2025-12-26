package com.example.my_music

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.my_music.databinding.PlaylistViewBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistAdapter(private val context: Context, private var playlistList: ArrayList<Playlist>, private var playlistDetails: Boolean = false): RecyclerView.Adapter<PlaylistAdapter.MyHolder>() {
    class MyHolder(binding: PlaylistViewBinding) : RecyclerView.ViewHolder(binding.root) {
        val image = binding.playlistImg
        val name = binding.playlistName
        val root = binding.root
        val delete = binding.playlistDeleteBtn
    }

    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): PlaylistAdapter.MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: PlaylistAdapter.MyHolder, position: Int) {
        holder.name.text = playlistList[position].name
        holder.name.isSelected = true
        holder.delete.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition == -1 || currentPosition >= PlaylistActivity.musicPlaylist.ref.size) return@setOnClickListener
            
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle(playlistList[currentPosition].name)
                .setMessage("Do you really want to delete this playlist?")
                .setPositiveButton("Yes"){dialog,_->
                    if (currentPosition < PlaylistActivity.musicPlaylist.ref.size) {
                        PlaylistActivity.musicPlaylist.ref.removeAt(currentPosition)
                        refreshPlaylist()
                    }
                   dialog.dismiss()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.red))
            customDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(context, R.color.cool_green))
        }
        holder.root.setOnClickListener {
            val currentPosition = holder.adapterPosition
            if (currentPosition == -1) return@setOnClickListener
            
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index", currentPosition)
            ContextCompat.startActivity(context, intent, null)
        }
        if(position < PlaylistActivity.musicPlaylist.ref.size && PlaylistActivity.musicPlaylist.ref[position].playlist.size > 0){
            Glide.with(context)
                .load(PlaylistActivity.musicPlaylist.ref[position].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.default_music_icon).centerCrop())
                .into(holder.image)
        }
    }

    override fun getItemCount(): Int {
        return playlistList.size
    }

    fun refreshPlaylist(){
        playlistList = ArrayList()
        playlistList.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }

}