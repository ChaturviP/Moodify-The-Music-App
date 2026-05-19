package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import java.util.Calendar
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var continueCard: View
    private lateinit var continueTitle: TextView
    private lateinit var continueSubtitle: TextView

    private var lastSong: Music? = null
    private lateinit var topMixRecycler: RecyclerView
    private lateinit var recentRecycler: RecyclerView
    private lateinit var greetingText: TextView
    private lateinit var usernameText: TextView
    private lateinit var searchBar: EditText

    private lateinit var musicList: MutableList<Music>

    override fun onResume() {
        super.onResume()
        loadRecentSongs()
        loadUserName()   // 🔥 ADD THIS
        loadContinueListening()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        continueCard = findViewById(R.id.continueCard)
        continueTitle = findViewById(R.id.continueTitle)
        continueSubtitle = findViewById(R.id.continueSubtitle)
        try {

            val profileIcon =
                findViewById<ImageView>(R.id.profileIcon)

            val favBtn =
                findViewById<ImageView>(R.id.favoritesBtn)

            val playlistBtn =
                findViewById<ImageView>(R.id.playlistBtn)

            val playBtnRecent =
                findViewById<ImageView>(R.id.playBtnRecent)
            playBtnRecent.setOnClickListener {

                val context = this

                // ❌ If nothing is playing, do nothing
                if (MusicQueue.currentList.isEmpty()) return@setOnClickListener

                // ✅ Just open player
                val intent = Intent(context, PlayerActivity::class.java)
                startActivity(intent)
            }

            greetingText =
                findViewById(R.id.greetingText)

            usernameText =
                findViewById(R.id.usernameText)

            searchBar =
                findViewById(R.id.searchBar)

            topMixRecycler =
                findViewById(R.id.topMixRecycler)

            recentRecycler =
                findViewById(R.id.recentRecycler)

            setupGreeting()
            loadUserName()
            loadSongs()
            setupSearch()
            seedSongs()

            topMixRecycler.layoutManager =
                LinearLayoutManager(
                    this,
                    LinearLayoutManager.HORIZONTAL,
                    false
                )

            recentRecycler.layoutManager =
                LinearLayoutManager(this)

            favBtn.setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        FavoritesActivity::class.java
                    )
                )
            }

            playlistBtn.setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        PlaylistActivity::class.java
                    )
                )
            }

            profileIcon.setOnClickListener {
                startActivity(
                    Intent(
                        this,
                        ProfileActivity::class.java
                    )
                )
            }
            continueCard.setOnClickListener {

                val song = lastSong ?: return@setOnClickListener

                // ✅ Set queue
                MusicQueue.currentList = listOf(song)
                MusicQueue.currentIndex = 0

                // 🔥 Start service
                val serviceIntent = Intent(this, MusicService::class.java)
                serviceIntent.putExtra("action", "PLAY_NEW")
                serviceIntent.putExtra("audioUrl", song.audioUrl)
                startService(serviceIntent)

                // 🎧 Open player
                startActivity(Intent(this, PlayerActivity::class.java))
            }



        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun loadContinueListening() {

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("recent")
            .document(user.uid)
            .collection("songs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { result ->

                if (!result.isEmpty) {

                    val doc = result.documents[0]

                    val song = Music(
                        id = doc.id,
                        title = doc.getString("title") ?: "",
                        subtitle = doc.getString("artist") ?: "",
                        imageUrl = doc.getString("imageUrl") ?: "",
                        audioUrl = doc.getString("audioUrl") ?: ""
                    )

                    lastSong = song

                    continueTitle.text = song.title
                    continueSubtitle.text = song.subtitle
                }
            }
    }
    private fun setupGreeting() {

        val hour =
            Calendar.getInstance().get(Calendar.HOUR_OF_DAY)

        val text = when {
            hour < 12 -> "Good Morning 👋"
            hour < 17 -> "Good Afternoon 👋"
            else -> "Good Evening 👋"
        }

        greetingText.text = text
    }


    private fun loadUserName() {

        val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()

        if (user != null) {

            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener {

                    val name = it.getString("name")

                    usernameText.text = name ?: "User"
                }
        }
    }

    private fun loadSongs() {

        val db = FirebaseFirestore.getInstance()

        db.collection("songs")
            .get()
            .addOnSuccessListener { result ->

                val list = mutableListOf<Music>()

                for (doc in result) {

                    val title = doc.getString("title") ?: ""
                    val artist = doc.getString("artist") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val audioUrl = doc.getString("audioUrl") ?: ""

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

                musicList = list
                updateRecyclerViews(musicList)
            }
            .addOnFailureListener {
                it.printStackTrace()
            }
    }
    private fun seedSongs() {

        val db = FirebaseFirestore.getInstance()

        val inputStream = assets.open("songs.json")
        val jsonString = inputStream.bufferedReader().use { it.readText() }

        val jsonArray = org.json.JSONArray(jsonString)

        val collection = db.collection("songs")

        for (i in 0 until jsonArray.length()) {

            val obj = jsonArray.getJSONObject(i)

            val title = obj.getString("title")

            val data = hashMapOf(
                "title" to title,
                "artist" to obj.getString("artist"),
                "imageUrl" to obj.getString("imageUrl"),
                "audioUrl" to obj.getString("audioUrl")
            )

            // 🔥 Use title as document ID
            val docRef = collection.document(title)

            docRef.set(data)
        }
    }
    private fun setupSearch() {

        searchBar.addTextChangedListener(
            object : TextWatcher {

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(
                    s: CharSequence?,
                    start: Int,
                    before: Int,
                    count: Int
                ) {

                    val query =
                        s.toString().trim().lowercase()

                    val filtered =
                        musicList.filter {

                            it.title.lowercase()
                                .contains(query) ||

                                    it.subtitle.lowercase()
                                        .contains(query)
                        }

                    updateRecyclerViews(filtered)
                }

                override fun afterTextChanged(
                    s: Editable?
                ) {
                }
            })
    }

    private fun updateRecyclerViews(
        list: List<Music>
    ) {

        topMixRecycler.adapter =
            MusicAdapter(list)

        recentRecycler.adapter =
            RecentAdapter(list, this)
    }

    private fun loadRecentSongs() {

        val db = FirebaseFirestore.getInstance()
        val user = FirebaseAuth.getInstance().currentUser

        if (user == null) return

        db.collection("recent")
            .document(user.uid)
            .collection("songs")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .limit(10)
            .get()
            .addOnSuccessListener { result ->

                val list = mutableListOf<Music>()

                for (doc in result) {

                    val title = doc.getString("title") ?: ""
                    val artist = doc.getString("artist") ?: ""
                    val imageUrl = doc.getString("imageUrl") ?: ""
                    val audioUrl = doc.getString("audioUrl") ?: ""

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

                recentRecycler.adapter = RecentAdapter(list, this)
            }
    }
}