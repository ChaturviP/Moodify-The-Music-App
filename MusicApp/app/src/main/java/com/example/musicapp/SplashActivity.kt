package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Optional: remove title bar flash
        supportActionBar?.hide()

        setContentView(R.layout.activity_splash)

        val sharedPref = getSharedPreferences("MusicApp", MODE_PRIVATE)

        Handler(Looper.getMainLooper()).postDelayed({

            val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)

            val nextScreen = if (isLoggedIn) {
                HomeActivity::class.java
            } else {
                LoginActivity::class.java
            }

            startActivity(Intent(this, nextScreen))
            finish()

        }, 1500) // slightly faster (feels better)
    }
}