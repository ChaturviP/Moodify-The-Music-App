package com.example.musicapp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreatePlaylistActivity : AppCompatActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private var selectedMood = "Chill"   // default mood

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_create_playlist)

        val playlistName = findViewById<EditText>(R.id.playlistName)
        val description = findViewById<EditText>(R.id.playlistDescription)

        val saveBtn = findViewById<Button>(R.id.savePlaylistBtn)

        // Mood Buttons
        val chillBtn = findViewById<Button>(R.id.chillBtn)
        val gymBtn = findViewById<Button>(R.id.gymBtn)
        val partyBtn = findViewById<Button>(R.id.partyBtn)

        // 🔥 FUNCTION TO RESET BUTTON COLORS
        fun resetButtons() {
            chillBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.card)
            gymBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.card)
            partyBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.card)
        }

        // 🔥 SET DEFAULT SELECTED BUTTON
        chillBtn.backgroundTintList =
            ContextCompat.getColorStateList(this, R.color.primary)

        // 🔥 CLICK LOGIC
        chillBtn.setOnClickListener {
            selectedMood = "Chill"
            resetButtons()
            chillBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.primary)
        }

        gymBtn.setOnClickListener {
            selectedMood = "Gym"
            resetButtons()
            gymBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.primary)
        }

        partyBtn.setOnClickListener {
            selectedMood = "Party"
            resetButtons()
            partyBtn.backgroundTintList =
                ContextCompat.getColorStateList(this, R.color.primary)
        }

        saveBtn.setOnClickListener {

            val name = playlistName.text.toString().trim()
            val desc = description.text.toString().trim()

            if (name.isEmpty()) {
                playlistName.error = "Enter playlist name"
                return@setOnClickListener
            }

            val user = auth.currentUser

            if (user == null) {
                Toast.makeText(
                    this,
                    "Please login first",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            val data = hashMapOf(
                "name" to name,
                "description" to desc,
                "mood" to selectedMood,
                "createdBy" to user.uid,
                "timestamp" to System.currentTimeMillis()
            )

            db.collection("playlists")
                .document(user.uid)
                .collection("userPlaylists")
                .add(data)
                .addOnSuccessListener {

                    Toast.makeText(
                        this,
                        "Playlist Created!",
                        Toast.LENGTH_SHORT
                    ).show()

                    finish()
                }
                .addOnFailureListener {

                    Toast.makeText(
                        this,
                        "Failed to create playlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }
}