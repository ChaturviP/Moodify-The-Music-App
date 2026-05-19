package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistActivity : AppCompatActivity() {

    private lateinit var playlistCount: TextView
    private lateinit var createBtn: Button
    private lateinit var recycler: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val playlistList = mutableListOf<Playlist>()
    private lateinit var adapter: PlaylistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist)

        playlistCount = findViewById(R.id.playlistCount)
        createBtn = findViewById(R.id.createPlaylistBtn)
        recycler = findViewById(R.id.playlistRecycler)

        adapter = PlaylistAdapter(playlistList)

        recycler.layoutManager = LinearLayoutManager(this)
        recycler.adapter = adapter

        createBtn.setOnClickListener {
            startActivity(Intent(this, CreatePlaylistActivity::class.java))
        }

        loadPlaylists()
    }

    private fun loadPlaylists() {

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("playlists")
            .document(user.uid)
            .collection("userPlaylists")
            .get()
            .addOnSuccessListener { result ->

                playlistList.clear()

                for (doc in result.documents) {

                    val id = doc.id
                    val name = doc.getString("name") ?: "Unnamed"
                    val description = doc.getString("description") ?: ""
                    val mood = doc.getString("mood") ?: ""

                    // 🔥 safety check
                    if (id.isNotEmpty()) {
                        playlistList.add(
                            Playlist(
                                id = id,
                                name = name,
                                description = description,
                                mood = mood
                            )
                        )
                    }
                }

                playlistCount.text = playlistList.size.toString()

                adapter.notifyDataSetChanged()
            }

            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Failed to load playlists",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadPlaylists()
    }
}