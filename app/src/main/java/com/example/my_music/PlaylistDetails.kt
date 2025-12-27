package com.example.my_music

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.my_music.databinding.ActivityPlaylistDetailsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {

    lateinit var binding: ActivityPlaylistDetailsBinding
    lateinit var adapter: MusicAdapter

    companion object{
        var currentPlaylistPos: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPos = intent.extras?.get("index") as Int
        
        if (currentPlaylistPos < 0 || currentPlaylistPos >= PlaylistActivity.musicPlaylist.ref.size) {
            finish()
            return
        }
    
        PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist = checkPlaylist(playlist = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
        
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.cool_pink)
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist, playlistDetails = true)
        binding.playlistDetailsRV.adapter = adapter
        binding.backBtnPD.setOnClickListener { finish() }
        binding.shufflePD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            startActivity(intent)
        }
        binding.addBtnPD.setOnClickListener {
            startActivity(Intent(this, selectionActivity::class.java))
        }
        binding.removeAllPD.setOnClickListener {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Remove")
                .setMessage("Do you really want to remove all songs from the playlist ?")
                .setPositiveButton("Yes"){dialog,_->
                    PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.clear()
                    dialog.dismiss()
                    adapter.refreshPlaylist()
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.red))
            customDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.cool_green))
        }

        
        // Apply padding to header for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        
        // Check if playlist still exists
        if (currentPlaylistPos < 0 || currentPlaylistPos >= PlaylistActivity.musicPlaylist.ref.size) {
            finish()
            return
        }
        
        binding.playlistNamePD.text = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.playlistInfoPD.text = "Total ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.size} Songs.\n\n" +
                "Created On:\n${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdOn}\n\n" +
                "   --${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdBy}"
        if(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist.isNotEmpty()){
            Glide.with(this)
                .load(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist[0].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.default_music_icon).centerCrop())
                .into(binding.playlistImgPD)
            binding.shufflePD.visibility = View.VISIBLE
        }
        adapter.refreshPlaylist()
        //for storing data using sharedprefrences
        val editor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavActivity.favSongs)
        editor.putString("FavSongs", jsonString)

        //for storing data using sharedprefrences for playlist
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }

}