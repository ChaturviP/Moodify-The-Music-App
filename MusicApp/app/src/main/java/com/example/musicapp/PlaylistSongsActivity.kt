package com.example.musicapp

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistSongsActivity : AppCompatActivity() {

    private lateinit var recycler: RecyclerView
    private lateinit var title: TextView

    private val db = FirebaseFirestore.getInstance()
    private val user get() = FirebaseAuth.getInstance().currentUser

    private val list = mutableListOf<Music>()
    private lateinit var adapter: PlaylistSongsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playlist_songs)

        try {

            recycler = findViewById(R.id.songRecycler)
            title = findViewById(R.id.playlistTitle)

            val playlistId = intent.getStringExtra("playlistId")
            val playlistName = intent.getStringExtra("playlistName") ?: "Playlist"

            if (playlistId.isNullOrEmpty()) {
                Toast.makeText(this, "Invalid playlist ID", Toast.LENGTH_SHORT).show()
                finish()
                return
            }

            title.text = playlistName

            adapter = PlaylistSongsAdapter(list, playlistId)

            recycler.layoutManager = LinearLayoutManager(this)
            recycler.adapter = adapter

            Log.d("PLAYLIST_DEBUG", "Opening playlist: $playlistId")

            loadSongs(playlistId)

        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Playlist screen crashed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loadSongs(playlistId: String) {

        val currentUser = user

        if (currentUser == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        db.collection("playlists")
            .document(currentUser.uid)
            .collection("userPlaylists")
            .document(playlistId)
            .collection("songs")
            .get()
            .addOnSuccessListener { result ->

                list.clear()

                Log.d("PLAYLIST_DEBUG", "Songs count: ${result.size()}")

                for (doc in result.documents) {

                    val music = Music(
                        id = doc.id,   // ✅ FIXED
                        title = doc.getString("title") ?: "Unknown",
                        subtitle = doc.getString("artist") ?: "Unknown",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        audioUrl = doc.getString("audioUrl") ?: ""
                    )

                    list.add(music)
                }

                adapter.notifyDataSetChanged()
            }

            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to load songs", Toast.LENGTH_SHORT).show()
                Log.e("PLAYLIST_DEBUG", "Error: ${e.message}")
            }
    }
}