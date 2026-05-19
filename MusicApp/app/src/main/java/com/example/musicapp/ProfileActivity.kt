package com.example.musicapp

import com.bumptech.glide.Glide
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var profileName: TextView
    private lateinit var profileBio: TextView

    private lateinit var profileImage: ImageView
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()

        profileBio = findViewById(R.id.profileBio)
        profileName = findViewById(R.id.profileName)

        profileImage = findViewById(R.id.profileImage)

        val profileEmail =
            findViewById<TextView>(R.id.profileEmail)

        val favCount =
            findViewById<TextView>(R.id.favStat)

        val playlistCount =
            findViewById<TextView>(R.id.playlistStat)

        val joinedDate =
            findViewById<TextView>(R.id.joinedDate)

        val logoutBtn =
            findViewById<Button>(R.id.logoutBtn)

        val editBtn = findViewById<Button>(R.id.editProfileBtn)

        val playlistsBtn = findViewById<TextView>(R.id.myPlaylistsBtn)

        val likedBtn = findViewById<TextView>(R.id.likedSongsBtn)

        val historyBtn = findViewById<TextView>(R.id.historyBtn)

        editBtn.setOnClickListener {
            startActivity(Intent(this, EditProfileActivity::class.java))
        }



        val user = auth.currentUser

        if (user != null) {

            profileEmail.text =
                user.email ?: "No Email"

            loadUserData(user.uid)


            joinedDate.text =
                "Joined Moodify"

            loadStats(user.uid, favCount, playlistCount)

        } else {

            profileName.text = "Guest"
            profileEmail.text = "guest@moodify.com"
        }



        playlistsBtn.setOnClickListener {
            startActivity(Intent(this, PlaylistActivity::class.java))
        }


        likedBtn.setOnClickListener {
            startActivity(Intent(this, FavoritesActivity::class.java))
        }


        historyBtn.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }
        logoutBtn.setOnClickListener {

            auth.signOut()

            startActivity(
                Intent(
                    this,
                    LoginActivity::class.java
                )
            )

            finishAffinity()
        }
    }

    private fun loadUserData(uid: String) {
        db.collection("users")
            .document(uid)
            .get()
            .addOnSuccessListener { doc ->

                val name = doc.getString("name")
                val bio = doc.getString("bio")
                val imageUrl = doc.getString("profileImageUrl")

                profileName.text = name ?: "Moodify User"
                profileBio.text =
                    if (bio.isNullOrEmpty()) "No bio yet" else bio

                // 🔥 LOAD IMAGE HERE
                if (!imageUrl.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imageUrl)
                        .into(profileImage)
                }
            }
    }
    private fun loadStats(
        uid: String,
        favCount: TextView,
        playlistCount: TextView
    ) {

        db.collection("favorites")
            .document(uid)
            .collection("songs")
            .get()
            .addOnSuccessListener {
                favCount.text =
                    it.size().toString()
            }

        db.collection("playlists")
            .document(uid)
            .collection("userPlaylists")
            .get()
            .addOnSuccessListener {
                playlistCount.text =
                    it.size().toString()
            }
    }
    override fun onResume() {
        super.onResume()

        val user = auth.currentUser

        if (user != null) {

            loadUserData(user.uid)
        }

    }
}