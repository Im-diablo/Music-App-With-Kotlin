package com.example.my_music

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_music.databinding.ActivitySettingsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Set up toolbar as action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Settings"
        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        val savedThemeIndex = themeEditor.getInt("themeIndex", 0)
        when(savedThemeIndex){
            0 -> binding.coolPink.setBackgroundColor(Color.BLUE)
            1 -> binding.coolBlue.setBackgroundColor(Color.BLUE)
            2 -> binding.coolPurple.setBackgroundColor(Color.BLUE)
            3 -> binding.coolGreen.setBackgroundColor(Color.BLUE)
            4 -> binding.coolBlack.setBackgroundColor(Color.BLUE)
        }
        binding.coolPink.setOnClickListener { saveTheme(0) }
        binding.coolBlue.setOnClickListener { saveTheme(1) }
        binding.coolPurple.setOnClickListener { saveTheme(2) }
        binding.coolGreen.setOnClickListener { saveTheme(3) }
        binding.coolBlack.setOnClickListener { saveTheme(4) }
        binding.versionName.text = setVersion()
        binding.sortBy.setOnClickListener {
            val menuList = arrayOf("Recently Added", "Song Title", "File Size")
            var currentSort = MainActivity.sortOrder
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sort By")
                .setPositiveButton("GO"){_,_->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("sortOrder", currentSort)
                    editor.apply()

                }
                .setSingleChoiceItems(menuList, currentSort){_,which->
                   currentSort = which
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.cool_green))
        }

        
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
    private fun saveTheme(index: Int){
        if(MainActivity.themeIndex != index) {
            val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
            editor.putInt("themeIndex", index)
            editor.apply()
            
            // Save Favorites and Playlist Data manually before restart to ensure persistence
            val dataEditor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE).edit()
            val gson = com.google.gson.GsonBuilder().create()
            dataEditor.putString("FavSongs", gson.toJson(FavActivity.favSongs))
            dataEditor.putString("MusicPlaylist", gson.toJson(PlaylistActivity.musicPlaylist))
            dataEditor.apply()

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply Theme")
                .setMessage("Do you really want to apply the theme ?")
                .setPositiveButton("Yes"){_,_->
                    // Stop music service if running
                    if(PlayerActivity.musicService != null){
                        PlayerActivity.musicService!!.stopForeground(true)
                        PlayerActivity.musicService!!.mediaPlayer!!.release()
                        PlayerActivity.musicService = null
                    }
                    // Restart App
                    val intent = packageManager.getLaunchIntentForPackage(packageName)
                    val componentName = intent?.component
                    val mainIntent = Intent.makeRestartActivityTask(componentName)
                    startActivity(mainIntent)
                    Runtime.getRuntime().exit(0)
                }
                .setNegativeButton("No"){dialog, _ ->
                    dialog.dismiss()
                }
            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.cool_green))
            customDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.red))
        }
    }

    private fun setVersion(): String {
        return "Version Name: ${BuildConfig.VERSION_NAME}"
    }
}