package com.example.miaplicacion

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var mediaPlayer: MediaPlayer
    private var isPlaying = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnMute: ImageButton = findViewById(R.id.btn_mute)
        val btnStart: Button = findViewById(R.id.btn_start)

        mediaPlayer = MediaPlayer.create(this, R.raw.fondo)
        mediaPlayer.isLooping = true
        mediaPlayer.start()

        btnMute.setOnClickListener {
            if (isPlaying) {
                mediaPlayer.pause()
                btnMute.setImageResource(R.drawable.mute)
            } else {
                mediaPlayer.start()
                btnMute.setImageResource(R.drawable.volumen)
            }
            isPlaying = !isPlaying
        }

        btnStart.setOnClickListener {
            val intent = Intent(this, QuestionActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mediaPlayer.isPlaying) {
            mediaPlayer.stop()
        }
        mediaPlayer.release()
    }
}
