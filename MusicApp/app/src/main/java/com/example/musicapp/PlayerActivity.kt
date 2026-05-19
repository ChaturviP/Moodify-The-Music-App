package com.example.musicapp

import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

class PlayerActivity : AppCompatActivity() {

    private val songList get() = MusicQueue.currentList
    private var currentIndex = MusicQueue.currentIndex

    private lateinit var playBtn: ImageView
    private lateinit var seekBar: SeekBar

    lateinit var currentTime: TextView
    lateinit var totalTime: TextView
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_player)

// ✅ FIRST initialize
        playBtn = findViewById(R.id.playBtn)

// ✅ THEN use it
        playBtn.setOnClickListener {

            val intent = Intent(this, MusicService::class.java)

            if (MusicService.isPlaying) {
                intent.putExtra("action", "PAUSE")
            } else {
                intent.putExtra("action", "RESUME")
            }

            startService(intent)
        }


        if (songList.isEmpty()) {
            finish()
            return
        }

        val playerTitle = findViewById<TextView>(R.id.playerTitle)
        val playerArtist = findViewById<TextView>(R.id.playerArtist)
        val playerImage = findViewById<ImageView>(R.id.playerImage)



        if (playBtn == null) {
            throw RuntimeException("playBtn not found in layout")
        }
        val prevBtn = findViewById<ImageView>(R.id.prevBtn)
        val nextBtn = findViewById<ImageView>(R.id.nextBtn)

        seekBar = findViewById(R.id.seekBar)
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    MusicService.mediaPlayer?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        handler.post(object : Runnable {
            override fun run() {

                if (MusicService.isPlaying) {
                    playBtn.setImageResource(android.R.drawable.ic_media_pause)
                } else {
                    playBtn.setImageResource(android.R.drawable.ic_media_play)
                }

                handler.postDelayed(this, 500)
            }
        })
        handler.post(object : Runnable {
            override fun run() {

                val mp = MusicService.mediaPlayer

                if (mp != null && mp.isPlaying) {
                    seekBar.max = mp.duration
                    seekBar.progress = mp.currentPosition

                    currentTime.text = formatTime(mp.currentPosition)
                    totalTime.text = formatTime(mp.duration)
                }

                handler.postDelayed(this, 500)
            }
        })

        currentTime = findViewById(R.id.currentTime)
        totalTime = findViewById(R.id.totalTime)




        loadSong(currentIndex)









        nextBtn.setOnClickListener {

            if (songList.isNotEmpty()) {
                currentIndex = (currentIndex + 1) % songList.size
                loadSong(currentIndex)
            }
        }

        prevBtn.setOnClickListener {

            if (songList.isNotEmpty()) {
                currentIndex =
                    if (currentIndex - 1 < 0) songList.size - 1
                    else currentIndex - 1

                loadSong(currentIndex)
            }
        }
    }

    private fun loadSong(index: Int) {

        currentIndex = index
        MusicQueue.currentIndex = index

        val song = songList[index]
        if (song.audioUrl.isEmpty()) return// ✅ FIRST define song

        // 🔥 THEN start service
        val intent = Intent(this, MusicService::class.java)
        intent.putExtra("action", "PLAY_NEW")
        intent.putExtra("audioUrl", song.audioUrl)
        startService(intent)




        val playerTitle = findViewById<TextView>(R.id.playerTitle)
        val playerArtist = findViewById<TextView>(R.id.playerArtist)
        val playerImage = findViewById<ImageView>(R.id.playerImage)

        playerTitle.text = song.title
        playerArtist.text = song.subtitle

        Glide.with(this)
            .load(song.imageUrl)
            .into(playerImage)

        // Save to recent
        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        if (user != null) {
            db.collection("recent")
                .document(user.uid)
                .collection("songs")
                .document(song.title)
                .set(
                    hashMapOf(
                        "title" to song.title,
                        "artist" to song.subtitle,
                        "imageUrl" to song.imageUrl,
                        "audioUrl" to song.audioUrl,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
        }


    }
    private fun formatTime(ms: Int): String {
        val minutes = ms / 1000 / 60
        val seconds = (ms / 1000) % 60
        return String.format("%d:%02d", minutes, seconds)
    }





}