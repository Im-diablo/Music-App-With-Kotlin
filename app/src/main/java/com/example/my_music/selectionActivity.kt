package com.example.my_music

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my_music.MainActivity.Companion.musicListMA
import com.example.my_music.MainActivity.Companion.musicListSearch
import com.example.my_music.MainActivity.Companion.search
import com.example.my_music.databinding.ActivitySelectionBinding

class selectionActivity : AppCompatActivity() {

    private  lateinit var binding: ActivitySelectionBinding
    private lateinit var adapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setTheme(R.style.Theme_ForestGreen)
        setContentView(binding.root)
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, musicListMA, selectionActivity = true)
        binding.selectionRV.adapter = adapter
        binding.backBtnSA.setOnClickListener { finish() }
        //search
        binding.searchSA.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true
            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if(newText !=null){
                    val userInput = newText.lowercase()
                    for(song in musicListMA)
                        if(song.title.lowercase().contains(userInput)){
                            musicListSearch.add(song)
                            search =true
                            adapter.updateMusicList(searchList = musicListSearch)
                        }
                }
                return true
            }
        })


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}