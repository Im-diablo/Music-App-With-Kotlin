package com.example.my_music

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.my_music.PlayerActivity.Companion.musicListPA
import com.example.my_music.PlayerActivity.Companion.songPosition
import com.example.my_music.databinding.FragmentNowPlayingBinding

class NowPlaying : Fragment() {

    companion object{
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE
        binding.playPauseBtnNp.setOnClickListener {
            if(PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }
        return view
    }

    override fun onResume() {
        super.onResume()
        if(PlayerActivity.musicService != null){
            binding.root.visibility = View.VISIBLE
            Glide.with(this)
                .load(musicListPA[songPosition].artUri)
                .apply(RequestOptions().placeholder(R.mipmap.default_music_icon).centerCrop())
                .into(binding.songImageNP)
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if(PlayerActivity.isPlaying) binding.playPauseBtnNp.setIconResource(R.drawable.pause_ic)
            else binding.playPauseBtnNp.setIconResource(R.drawable.play_ic)
        }
    }

    private fun playMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.start()
        binding.playPauseBtnNp.setIconResource(R.drawable.pause_ic)
        PlayerActivity.musicService!!.showNotification(R.drawable.pause_ic)
        PlayerActivity.binding.nextBtn.setImageResource(R.drawable.pause_ic)
        PlayerActivity.isPlaying = true
    }

    private  fun pauseMusic(){
        PlayerActivity.musicService!!.mediaPlayer!!.pause()
        binding.playPauseBtnNp.setIconResource(R.drawable.play_ic)
        PlayerActivity.musicService!!.showNotification(R.drawable.play_ic)
        PlayerActivity.binding.nextBtn.setImageResource(R.drawable.play_ic)
        PlayerActivity.isPlaying = false
    }
}