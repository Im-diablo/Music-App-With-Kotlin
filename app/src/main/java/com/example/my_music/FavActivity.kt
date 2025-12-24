package com.example.my_music

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import com.example.my_music.databinding.ActivityFavBinding

class FavActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavBinding
    private lateinit var adapter: FavAdapter
    companion object{
        var favSongs: ArrayList<Music> = ArrayList()
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_GoldenAmber)
        binding = ActivityFavBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set status bar color and handle window insets
        window.statusBarColor = androidx.core.content.ContextCompat.getColor(this, R.color.cool_pink)
        
        // Apply padding to header for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.linearLayout) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        binding.backBtnFav.setOnClickListener { finish() }
        binding.favRV.setHasFixedSize(true)
        binding.favRV.setItemViewCacheSize(13)
        binding.favRV.layoutManager = GridLayoutManager(this, 4)
        adapter = FavAdapter(this, favSongs)
        binding.favRV.adapter = adapter
        if(favSongs.size<1) binding.favShuffle.visibility = View.INVISIBLE
        binding.favShuffle.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavShuffle")
            startActivity(intent)
        }
    }
}