package com.example.musicapp

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesActivity : AppCompatActivity() {

    private lateinit var favCount: TextView
    private lateinit var recycler: RecyclerView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val favList = mutableListOf<Music>()
    private lateinit var adapter: RecentAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_favorites)

        try {

            favCount = findViewById(R.id.favCount)
            recycler = findViewById(R.id.favRecycler)

            recycler.layoutManager = LinearLayoutManager(this)
            recycler.setHasFixedSize(true)

            adapter = RecentAdapter(favList, this)
            recycler.adapter = adapter



        } catch (e: Exception) {
            e.printStackTrace()

            Toast.makeText(
                this,
                "Favorites screen error",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun loadFavorites() {

        val user = auth.currentUser

        if (user == null) {
            favCount.text = "0"
            return
        }

        db.collection("favorites")
            .document(user.uid)
            .collection("songs")
            .get()
            .addOnSuccessListener { result ->

                favList.clear()

                for (doc in result.documents) {

                    val title =
                        doc.getString("title")
                            ?: "Unknown Song"

                    val artist =
                        doc.getString("artist")
                            ?: "Unknown Artist"

                    val imageUrl =
                        doc.getString("imageUrl")
                            ?: ""

                    val audioUrl =
                        doc.getString("audioUrl")
                            ?: ""

                    favList.add(
                        Music(
                            id = doc.id,                // 🔥 ADD THIS
                            title = title,
                            subtitle = artist,
                            imageUrl = imageUrl,
                            audioUrl = audioUrl
                        )
                    )

                }

                favCount.text = favList.size.toString()

                adapter.notifyDataSetChanged()
            }

            .addOnFailureListener {

                Toast.makeText(
                    this,
                    "Failed to load favorites",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onResume() {
        super.onResume()
        loadFavorites()
    }
}