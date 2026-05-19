package com.example.musicapp

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class HistoryActivity : AppCompatActivity() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        val recycler = findViewById<RecyclerView>(R.id.historyRecycler)
        recycler.layoutManager = LinearLayoutManager(this)

        val list = mutableListOf<Music>()

        val user = auth.currentUser

        if (user == null) {
            Toast.makeText(this, "Please login", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        db.collection("recent")
            .document(user.uid)
            .collection("songs")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener {

                for (doc in it) {
                    list.add(
                        Music(
                            id = doc.id,   // 🔥 IMPORTANT
                            title = doc.getString("title") ?: "",
                            subtitle = doc.getString("artist") ?: "",
                            imageUrl = doc.getString("imageUrl") ?: "",
                            audioUrl = doc.getString("audioUrl") ?: ""
                        )
                    )
                }

                if (list.isEmpty()) {
                    Toast.makeText(this, "No listening history yet", Toast.LENGTH_SHORT).show()
                }

                recycler.adapter = RecentAdapter(list, this)
            }
    }
}