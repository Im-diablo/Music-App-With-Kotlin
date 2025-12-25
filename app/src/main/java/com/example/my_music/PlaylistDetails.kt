package com.example.my_music

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_music.databinding.ActivityPlaylistDetailsBinding

class PlaylistDetails : AppCompatActivity() {

    lateinit var binding: ActivityPlaylistDetailsBinding

    companion object{
        var currentPlaylistPos: Int = -1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setTheme(R.style.Theme_ForestGreen)
        binding = ActivityPlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        currentPlaylistPos = intent.extras?.get("index") as Int

        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.cool_pink)
        
        // Apply padding to header for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }
}