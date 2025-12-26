package com.example.my_music

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.my_music.databinding.ActivityPlaylistBinding
import com.example.my_music.databinding.AddPlaylistDialogBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.text.SimpleDateFormat
import java.util.Locale

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var adapter: PlaylistAdapter

    companion object{
        var musicPlaylist: musicPlaylist = musicPlaylist()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ForestGreen)
        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set status bar color and handle window insets
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.cool_pink)
        
        // Apply padding to header for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity, 2)
        adapter = PlaylistAdapter(this, playlistList = musicPlaylist.ref)
        binding.playlistRV.adapter = adapter
        binding.backBtnPlaylist.setOnClickListener { finish() }
        binding.playlistAddBtn.setOnClickListener { customAlertDialog() }
    }

    private fun customAlertDialog(){
        val customDialog = LayoutInflater.from(this@PlaylistActivity).inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binder = AddPlaylistDialogBinding.bind(customDialog)
        val builder = MaterialAlertDialogBuilder(this)
        builder.setView(customDialog)
        builder.setTitle("Playlist Name ")
            .setPositiveButton("ADD"){dialog,_->
               val playlistName = binder.playlistNameAdd.text
                val createdBy = binder.playlistAddUser.text
                if(playlistName != null && createdBy != null){
                    if(playlistName.isNotEmpty() && createdBy.isNotEmpty()){
                        addPlaylist(playlistName.toString(), createdBy.toString())
                    }
                }
               dialog.dismiss()
            }
            .setNegativeButton("No"){dialog,_->
                dialog.dismiss()
            }.show()
    }

    private fun addPlaylist(name: String, createdBy: String){
        var playlistExist = false
        for(i in musicPlaylist.ref){
            if(name.equals(i.name)){
                playlistExist = true
                break
            }
        }
        if(playlistExist) Toast.makeText(this, "Playlist Exist!!", Toast.LENGTH_SHORT).show()
        else {
            val tempPlaylist = Playlist()
            tempPlaylist.name = name
            tempPlaylist.playlist = ArrayList()
            tempPlaylist.createdBy = createdBy
            val calender = java.util.Calendar.getInstance().time
            val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
            tempPlaylist.createdOn = sdf.format(calender)
            musicPlaylist.ref.add(tempPlaylist)
            adapter.refreshPlaylist()
        }
    }
    @SuppressLint("NotifyDataSetChanged")
    override fun onResume(){
        super.onResume()
        adapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        // Save playlist data when leaving this activity
        val editor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE).edit()
        val jsonStringPlaylist = com.google.gson.GsonBuilder().create().toJson(musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }
}