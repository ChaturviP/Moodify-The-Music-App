package com.example.musicapp

import android.app.Service
import android.content.Intent
import android.media.MediaPlayer
import android.os.IBinder

class MusicService : Service() {

    companion object {
        var mediaPlayer: MediaPlayer? = null
        var isPlaying = false
        var isPreparing = false
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        val action = intent?.getStringExtra("action")
        val audioUrl = intent?.getStringExtra("audioUrl")

        try {
            when (action) {

                "PLAY_NEW" -> {
                    if (!audioUrl.isNullOrEmpty()) {

                        // 🔥 create only once
                        if (mediaPlayer == null) {
                            mediaPlayer = MediaPlayer()
                        }

                        try {
                            // 🔥 THIS LINE IS THE MAIN FIX
                            mediaPlayer?.reset()

                            mediaPlayer?.setAudioAttributes(
                                android.media.AudioAttributes.Builder()
                                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_MUSIC)
                                    .setUsage(android.media.AudioAttributes.USAGE_MEDIA)
                                    .build()
                            )

                            mediaPlayer?.setDataSource(audioUrl)

                            mediaPlayer?.setOnPreparedListener {
                                it.start()
                                isPlaying = true
                            }

                            mediaPlayer?.setOnCompletionListener {
                                isPlaying = false
                            }

                            mediaPlayer?.setOnErrorListener { _, _, _ ->
                                isPlaying = false
                                true
                            }

                            mediaPlayer?.prepareAsync()

                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }

                "PAUSE" -> {
                    if (mediaPlayer?.isPlaying == true) {
                        mediaPlayer?.pause()
                        MusicService.isPlaying = false
                    }
                }

                "RESUME" -> {
                    if (mediaPlayer != null && !mediaPlayer!!.isPlaying) {
                        mediaPlayer?.start()
                        MusicService.isPlaying = true
                    }
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        mediaPlayer?.release()
        mediaPlayer = null
        isPlaying = false
        super.onDestroy()
    }
}