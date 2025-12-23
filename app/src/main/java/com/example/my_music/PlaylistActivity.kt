package com.example.my_music

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_music.databinding.ActivityPlaylistBinding

class PlaylistActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPlaylistBinding

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
        
        binding.backBtnPlaylist.setOnClickListener { finish() }
    }
}