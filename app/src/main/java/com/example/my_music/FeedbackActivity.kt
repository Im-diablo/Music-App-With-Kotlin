package com.example.my_music

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.my_music.databinding.ActivityFeedbackBinding

class FeedbackActivity : AppCompatActivity() {

    lateinit var binding: ActivityFeedbackBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        binding = ActivityFeedbackBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Feedback"

        binding.sendFA.setOnClickListener {
            val feedbackMsg = binding.messageFA.text.toString()
            val userEmail = binding.emailFA.text.toString()
            val subject = binding.topicFA.text.toString()
            
            if (feedbackMsg.isNotEmpty() && subject.isNotEmpty()) {
           
                val emailBody = "$feedbackMsg\n\nFrom: $userEmail"
                
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "message/rfc822"
                    putExtra(Intent.EXTRA_EMAIL, arrayOf("multiacc101@outlook.com"))
                    putExtra(Intent.EXTRA_SUBJECT, subject)
                    putExtra(Intent.EXTRA_TEXT, emailBody)
                }
                
                try {
                    startActivity(Intent.createChooser(intent, "Send feedback via:"))
                } catch (e: Exception) {
                    Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}