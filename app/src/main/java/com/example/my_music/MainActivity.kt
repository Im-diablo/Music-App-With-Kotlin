package com.example.my_music

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my_music.databinding.ActivityMainBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter
    companion object{
        lateinit var musicListMA: ArrayList<Music>
        lateinit var musicListSearch : ArrayList<Music>
        var search: Boolean = false
    }
    
    // Modern permissions launcher using Activity Result API
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
            initializeLayout()
            // After storage permission is granted, request notification permission
            requestNotificationPermission()
        } else {
            Toast.makeText(this, "Permission Denied - App may not work properly", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, "Notification Permission Granted", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Notification Permission Denied - Notifications won't appear", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.Theme_ElectricBlue)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // Set status bar color and handle window insets
        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_200)
        
        // Apply padding to toolbar for status bar
        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbar) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }
        
        setSupportActionBar(binding.toolbar)
        
        //for Nav Drawer
        toggle = ActionBarDrawerToggle(this, binding.drawerLayout, binding.toolbar, R.string.open, R.string.close)
        binding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Request permissions and initialize if already granted
        requestRuntimePermissions()

        binding.shuffleBtn.setOnClickListener {
            // Toast.makeText(this@MainActivity, "Shuffle", Toast.LENGTH_SHORT).show()
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.FavButton.setOnClickListener {
            startActivity(Intent(this@MainActivity, FavActivity::class.java))
        }

        binding.playlistBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
        }

        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId){
                R.id.NFeedback -> {
                    Toast.makeText(applicationContext, "Feedback", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.NSettings -> {
                    Toast.makeText(applicationContext, "Settings", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.NAbout -> {
                    Toast.makeText(applicationContext, "About", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.NExit -> {
                   val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Do you really want to exit the app ?")
                        .setPositiveButton("Exit"){_,_->
                            exitApplication()
                        }
                        .setNegativeButton("Cancel"){dialog, _ ->
                            dialog.dismiss()
                        }
                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.red))
                    customDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.cool_green))
                }
                else -> false
            }
            true
        }
    }

    // Request appropriate permission based on Android version
    private fun requestRuntimePermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO  // Android 13+
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE  // Android 12 and below
        }
        
        // Always request if not granted - will ask again on each app launch if denied
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        } else {
            initializeLayout()
            requestNotificationPermission()
        }
        //for retriving fav data using sharedprefrences
        FavActivity.favSongs = ArrayList()
        val editor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE)
        val jsonString = editor.getString("FavSongs", null)
        val typeToken = object: TypeToken<ArrayList<Music>>(){}.type
        if(jsonString != null){
            val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)
            FavActivity.favSongs.addAll(data)
        }
        PlaylistActivity.musicPlaylist = musicPlaylist()
        val jsonStringPlaylist = editor.getString("MusicPlaylist", null)
        if(jsonStringPlaylist != null){
            try {
                val dataPlaylist: musicPlaylist = GsonBuilder().create().fromJson(jsonStringPlaylist, musicPlaylist::class.java)
                PlaylistActivity.musicPlaylist = dataPlaylist
            } catch (e: Exception) {
                PlaylistActivity.musicPlaylist = musicPlaylist()
                editor.edit().remove("MusicPlaylist").apply()
            }
        }
    }

    private fun requestNotificationPermission() {
        // Only request notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
        search = false
        // Trigger media scan to ensure all files are indexed
        scanMediaFiles()
        // Set up RecyclerView
        musicListMA = getAllAudio()
        binding.MusicRV.setHasFixedSize(true)
        binding.MusicRV.setItemViewCacheSize(13)
        binding.MusicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, musicListMA)
        binding.MusicRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs : "+musicAdapter.itemCount
    }

    @SuppressLint("SetTextI18n")
    private fun scanMediaFiles() {
        // Scan common music directories
        val musicDirs = arrayOf(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_MUSIC),
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        )
        
        for (dir in musicDirs) {
            if (dir.exists()) {
                android.media.MediaScannerConnection.scanFile(
                    this,
                    arrayOf(dir.absolutePath),
                    null
                ) { path, uri ->
                    android.util.Log.d("MusicDebug", "Scanned: $path")
                }
            }
        }
        
        // Give scanner a moment to complete
        android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
            // Refresh list after scan
            if (::musicAdapter.isInitialized) {
                musicListMA = getAllAudio()
                musicAdapter = MusicAdapter(this@MainActivity, musicListMA)
                binding.MusicRV.adapter = musicAdapter
                binding.totalSongs.text = "Total Songs : "+musicAdapter.itemCount
            }
        }, 1000)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)){
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @SuppressLint("Range")
    private fun getAllAudio(): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID, MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.DURATION, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.ALBUM_ID)
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, null, MediaStore.Audio.Media.DATE_ADDED + " DESC", null)
        if (cursor != null)
            if(cursor.moveToFirst())
                do{
                    val titleC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val idC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media._ID))
                    val albumC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artistC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val pathC = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val durationC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val albumIdC = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = "content://media/external/audio/albumart".toUri()
                    val artUriC = Uri.withAppendedPath(uri, albumIdC).toString()
                    val music = Music(id = idC, title = titleC, album = albumC, artist = artistC, path = pathC, duration = durationC, artUri = artUriC)
                    val file = File(music.path)
                    if(file.exists()) {
                        tempList.add(music)
                        android.util.Log.d("MusicDebug", "Added song: ${music.title} - ${music.path}")
                    } else {
                        android.util.Log.d("MusicDebug", "File not found: ${music.title} - ${music.path}")
                    }
                }while (cursor.moveToNext())
        cursor?.close()
        android.util.Log.d("MusicDebug", "Total songs found: ${tempList.size}")
        return tempList
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)
        val searchView =menu?.findItem(R.id.searchView)?.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                musicListSearch = ArrayList()
                if(newText !=null){
                    val userInput = newText.lowercase()
                    for(song in musicListMA)
                        if(song.title.lowercase().contains(userInput)){
                            musicListSearch.add(song)
                            search =true
                            musicAdapter.updateMusicList(searchList = musicListSearch)
                        }
                }
                return true
            }
        })
        return super.onCreateOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        //for storing data using sharedprefrences
        val editor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavActivity.favSongs)
        editor.putString("FavSongs", jsonString)

        //for storing data using sharedprefrences for playlist
        val jsonStringPlaylist = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        editor.putString("MusicPlaylist", jsonStringPlaylist)
        editor.apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying && PlayerActivity.musicService != null) {
            exitApplication()
        }
    }
}
