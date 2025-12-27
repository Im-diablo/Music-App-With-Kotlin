package com.example.my_music

import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.os.Bundle
import android.os.IBinder
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
        initializeLayout()
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
            musicService!!.mediaPlayer!!.setDataSource(musicListPA[songPosition].path)
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
                // for starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" -> {
                // for starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                setLayout()
            }

            "MainActivity" -> {
                // for starting service
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListMA)
                musicListPA.shuffle()
                setLayout()
            }

            "FavAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavActivity.favSongs)
                setLayout()
            }
            "FavShuffle" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                musicListPA.addAll(FavActivity.favSongs)
                musicListPA.shuffle()
                setLayout()
            }
            "PlaylistDetailsAdapter" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                if (PlaylistDetails.currentPlaylistPos >= 0 && PlaylistDetails.currentPlaylistPos < PlaylistActivity.musicPlaylist.ref.size) {
                    musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                }
                setLayout()
            }
            "PlaylistDetailsShuffle" ->{
                val intent = Intent(this, MusicService::class.java)
                bindService(intent, this, BIND_AUTO_CREATE)
                startService(intent)
                musicListPA = ArrayList()
                if (PlaylistDetails.currentPlaylistPos >= 0 && PlaylistDetails.currentPlaylistPos < PlaylistActivity.musicPlaylist.ref.size) {
                    musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                    musicListPA.shuffle()
                }
                setLayout()

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
        // Save favorites when activity is destroyed
        val editor = getSharedPreferences("FAV_SONGS", MODE_PRIVATE).edit()
        val jsonString = com.google.gson.GsonBuilder().create().toJson(FavActivity.favSongs)
        editor.putString("FavSongs", jsonString)
        editor.apply()
    }
}

    