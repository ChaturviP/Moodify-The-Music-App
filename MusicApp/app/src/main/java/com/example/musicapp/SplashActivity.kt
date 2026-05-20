package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide action bar
        supportActionBar?.hide()

        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({

            // 🔥 Firebase session check
            val user = FirebaseAuth.getInstance().currentUser

            val nextScreen = if (user != null) {
                HomeActivity::class.java
            } else {
                LoginActivity::class.java
            }

            startActivity(Intent(this, nextScreen))
            finish()

        }, 1500)
    }
}