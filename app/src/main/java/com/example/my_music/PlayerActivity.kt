package com.example.my_music

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.my_music.databinding.ActivityPlayerBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {
    companion object {
        lateinit var musicListPA: ArrayList<Music>
        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
        var repeat: Boolean =false
        var min5s: Boolean = false
        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false
        var nowPlayingId: String = ""
        var isFav: Boolean = false
        var fIndex: Int = -1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        if(intent.data?.scheme.contentEquals("content")){
            songPosition = 0
            musicListPA = ArrayList()
            musicListPA.add(getMusicDetails(intent.data!!))
            
            // Set up UI for external file
            binding.currentSongPA.text = musicListPA[songPosition].title
            binding.currentSongPA.isSelected = true
            
            // Use default icon for external files
            Glide.with(this)
                .load(R.mipmap.default_music_icon)
                .apply(RequestOptions().centerCrop())
                .into(binding.songImagePA)
            
            if(musicService != null) {
                try {
                    createMediaPlayer()
                } catch (e: Exception) {
                    val intentService = Intent(this, MusicService::class.java)
                    bindService(intentService, this, BIND_AUTO_CREATE)
                    startService(intentService)
                }
            } else {
                val intentService = Intent(this, MusicService::class.java)
                bindService(intentService, this, BIND_AUTO_CREATE)
                startService(intentService)
            }
        }
        else initializeLayout()
        binding.playPauseBtn.setOnClickListener {if (isPlaying) pauseMusic() else playMusic()}
        binding.prevBtn.setOnClickListener { prevNextSong(increment = false) }
        binding.nextBtn.setOnClickListener { prevNextSong(increment = true) }
        binding.backBtn.setOnClickListener { finish() }
        binding.seekBarPA.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?,progress: Int,fromUser: Boolean) {
                if (fromUser) musicService!!.mediaPlayer!!.seekTo(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) = Unit
            override fun onStopTrackingTouch(seekBar: SeekBar?) = Unit
        })
        binding.repeatBtnPA.setOnClickListener {
            if(!repeat){
                repeat = true
                binding.repeatBtnPA.setImageResource(R.drawable.repeat_one_ic)
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.red))
            }else{
                repeat = false
                binding.repeatBtnPA.setImageResource(R.drawable.repeat_ic)
                binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            }
        }

        //equilizer
        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService!!.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)
                startActivityForResult(eqIntent, 13)
            }catch (e: Exception){
                Toast.makeText(this, "Equaliser not supported", Toast.LENGTH_SHORT).show()}
        }

        // timer
        binding.timerBtnPA.setOnClickListener {
            val timer = min5s || min15 || min30 || min60
            if(!timer) showBottomSheetDialog()
            else { val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer ?")
                    .setPositiveButton("Yes"){_,_->
                        min5s = false
                        min15 = false
                        min30 = false
                        min60 = false
                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                    }
                    .setNegativeButton("No"){dialog, _ ->
                        dialog.dismiss()
                    }
                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.red))
                customDialog.getButton(android.app.AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.cool_green))
            }
        }
        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, musicListPA[songPosition].path.toUri())
            startActivity(Intent.createChooser(shareIntent, "Share Music File!!"))
        }
        binding.favBtnPA.setOnClickListener{
            if(isFav) {
                isFav = false
                binding.favBtnPA.setImageResource(R.drawable.fav_empty_ic)
                FavActivity.favSongs.removeAt(fIndex)
            }
            else{
                isFav = true
                binding.favBtnPA.setImageResource(R.drawable.favorite_ic)
                FavActivity.favSongs.add(musicListPA[songPosition])
            }
        }

    }
    private fun setLayout() {
        fIndex = favChecker(musicListPA[songPosition].id)
        Glide.with(this)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.mipmap.default_music_icon).centerCrop())
            .into(binding.songImagePA)
        binding.currentSongPA.text = musicListPA[songPosition].title
        if(repeat) {
            binding.repeatBtnPA.setImageResource(R.drawable.repeat_one_ic)
        binding.repeatBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.red))
        }
        if(min5s || min15 || min30 || min60) binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
        if(isFav) binding.favBtnPA.setImageResource(R.drawable.favorite_ic)
        else binding.favBtnPA.setImageResource(R.drawable.fav_empty_ic)
    }

    private fun createMediaPlayer() {
        try {
            if (musicService!!.mediaPlayer == null) musicService!!.mediaPlayer = MediaPlayer()
            else musicService!!.mediaPlayer!!.reset()
            
            val path = musicListPA[songPosition].path
            if (path.startsWith("content://")) {
                musicService!!.mediaPlayer!!.setDataSource(this, Uri.parse(path))
            } else {
                musicService!!.mediaPlayer!!.setDataSource(path)
            }
            
            musicService!!.mediaPlayer!!.prepare()
            musicService!!.mediaPlayer!!.start()
            isPlaying = true
            binding.playPauseBtn.setImageResource(R.drawable.pause_ic)
            musicService!!.showNotification(R.drawable.pause_ic)
            binding.seekbarStartTV.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
            binding.seekbarEndTV.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
            binding.seekBarPA.progress = 0
            binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
            musicService!!.mediaPlayer!!.setOnCompletionListener(this)
            nowPlayingId = musicListPA[songPosition].id
        } catch (e: Exception) {
            android.util.Log.e("PlayerActivity", "Error creating media player: ${e.message}", e)
            Toast.makeText(this, "Error playing audio: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)
        when (intent.getStringExtra("class")) {
            "NowPlaying" -> {
                setLayout()
                binding.seekbarStartTV.text = formatDuration(musicService!!.mediaPlayer!!.currentPosition.toLong())
                binding.seekbarEndTV.text = formatDuration(musicService!!.mediaPlayer!!.duration.toLong())
                binding.seekBarPA.progress = musicService!!.mediaPlayer!!.currentPosition
                binding.seekBarPA.max = musicService!!.mediaPlayer!!.duration
                if(isPlaying) binding.playPauseBtn.setImageResource(R.drawable.pause_ic)
                else binding.playPauseBtn.setImageResource(R.drawable.play_ic)
            }
            "MusicAdapterSearch" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
                
                if(musicService != null) {
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }
            }
            "MusicAdapter" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                setLayout()
                
                if(musicService != null) {
                    // Service already running, create media player immediately
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        // If direct creation fails, rebind to service
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    // Service not running, start and bind to it
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }
            }

            "MainActivity" -> {
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                musicListPA.shuffle()
                setLayout()
                
                if(musicService != null) {
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }
            }

            "FavAdapter" ->{
                musicListPA = ArrayList()
                musicListPA.addAll(FavActivity.favSongs)
                setLayout()
                if(musicService != null) {
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }
            }
            "FavShuffle" ->{
                musicListPA = ArrayList()
                musicListPA.addAll(FavActivity.favSongs)
                musicListPA.shuffle()
                setLayout()
                
                if(musicService != null) {
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }
            }
            "PlaylistDetailsAdapter" ->{
                musicListPA = ArrayList()
                if (PlaylistDetails.currentPlaylistPos >= 0 && PlaylistDetails.currentPlaylistPos < PlaylistActivity.musicPlaylist.ref.size) {
                    musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                }
                setLayout()
                
                if(musicService != null) {
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }
            }
            "PlaylistDetailsShuffle" ->{
                musicListPA = ArrayList()
                if (PlaylistDetails.currentPlaylistPos >= 0 && PlaylistDetails.currentPlaylistPos < PlaylistActivity.musicPlaylist.ref.size) {
                    musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                    musicListPA.shuffle()
                }
                setLayout()
                
                if(musicService != null) {
                    try {
                        createMediaPlayer()
                    } catch (e: Exception) {
                        val intent = Intent(this, MusicService::class.java)
                        bindService(intent, this, BIND_AUTO_CREATE)
                        startService(intent)
                    }
                } else {
                    val intent = Intent(this, MusicService::class.java)
                    bindService(intent, this, BIND_AUTO_CREATE)
                    startService(intent)
                }

            }
        }
    }

    private fun playMusic() {
        binding.playPauseBtn.setImageResource(R.drawable.pause_ic)
        musicService!!.showNotification(R.drawable.pause_ic)
        isPlaying = true
        musicService!!.mediaPlayer!!.start()
    }

    private fun pauseMusic() {
        binding.playPauseBtn.setImageResource(R.drawable.play_ic)
        musicService!!.showNotification(R.drawable.play_ic)
        isPlaying = false
        musicService!!.mediaPlayer!!.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        if (increment) {
            setSongPosition(increment = true)
            setLayout()
            createMediaPlayer()
        } else {
            setSongPosition(increment = false)
            setLayout()
            createMediaPlayer()
        }
    }

    override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()
        createMediaPlayer()
        musicService!!.seekBarSetup()
        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        musicService!!.audioManager.requestAudioFocus(musicService, AudioManager.STREAM_MUSIC,
            AudioManager.AUDIOFOCUS_GAIN)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(mp: MediaPlayer?) {
        setSongPosition(increment = true)
        createMediaPlayer()
        try {
            setLayout()
        }catch (e: Exception){return}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    private fun showBottomSheetDialog(){
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()
        dialog.findViewById<LinearLayout>(R.id.min_test_5s)?.setOnClickListener {
            Toast.makeText(this, "5 Seconds", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            min5s = true
            Thread{ Thread.sleep(5000)
            if(min5s) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(this, "Music Will Stop After 15 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            min5s = true
            Thread{ Thread.sleep((15*60000).toLong())
                if(min15) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(this, "Music Will Stop After 30 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            min5s = true
            Thread{ Thread.sleep((30*60000).toLong())
                if(min30) exitApplication()}.start()
            dialog.dismiss()
        }
        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(this, "Music Will Stop After 60 Minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_pink))
            min5s = true
            Thread{ Thread.sleep((60*60000).toLong())
                if(min60) exitApplication()}.start()
            dialog.dismiss()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if(musicListPA[songPosition].id == "UNKNOWN" && !isPlaying) exitApplication()
        // Save favorites when activity is destroyed
        val editor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE).edit()
        val jsonString = com.google.gson.GsonBuilder().create().toJson(FavActivity.favSongs)
        editor.putString("FavSongs", jsonString)
        editor.apply()
    }

    private fun getMusicDetails(contentUri: Uri): Music{
        var cursor: Cursor? = null
        try{
            val projection = arrayOf(
                MediaStore.Audio.Media.DATA, 
                MediaStore.Audio.Media.DURATION,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media._ID
            )
            cursor = this.contentResolver.query(contentUri, projection, null, null, null)
            cursor!!.moveToFirst()
            
            val dataColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            val durationColumn = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION)
            val titleColumn = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val artistColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val idColumn = cursor.getColumnIndex(MediaStore.Audio.Media._ID)
            
            val path = if (dataColumn >= 0) cursor.getString(dataColumn) else null
            val finalPath = if (path.isNullOrEmpty()) contentUri.toString() else path
            
            val duration = if (durationColumn >= 0) cursor.getLong(durationColumn) else 0L
            val title = if (titleColumn >= 0) cursor.getString(titleColumn) ?: "Unknown" else "Unknown"
            val artist = if (artistColumn >= 0) cursor.getString(artistColumn) ?: "Unknown Artist" else "Unknown Artist"
            val album = if (albumColumn >= 0) cursor.getString(albumColumn) ?: "Unknown Album" else "Unknown Album"
            val id = if (idColumn >= 0) cursor.getString(idColumn) ?: "UNKNOWN" else "UNKNOWN"
            
            return Music(id = id, title = title, album = album, artist = artist, path = finalPath, duration = duration, artUri = "")
        }finally {
            cursor?.close()
        }
    }
}

    