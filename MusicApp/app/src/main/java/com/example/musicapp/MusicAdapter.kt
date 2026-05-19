package com.example.musicapp

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MusicAdapter(private val musicList: List<Music>) :
    RecyclerView.Adapter<MusicAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val addBtn: ImageView = itemView.findViewById(R.id.addToPlaylistBtn)
        val image: ImageView = itemView.findViewById(R.id.songImage)
        val title: TextView = itemView.findViewById(R.id.songTitle)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_music, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val music = musicList[position]

        holder.title.text = music.title

        Glide.with(holder.itemView.context)
            .load(music.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_menu_report_image)
            .into(holder.image)

        // 🔥 ADD TO PLAYLIST BUTTON
        holder.addBtn.setOnClickListener {
            showPlaylistDialog(holder.itemView.context, music)
        }

        // 🔥 PLAY SONG
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context

            // ✅ Set queue
            MusicQueue.currentList = musicList
            MusicQueue.currentIndex = position

            // 🔥 START MUSIC SERVICE (IMPORTANT)
            val serviceIntent = Intent(context, MusicService::class.java)
            serviceIntent.putExtra("audioUrl", music.audioUrl)
            context.startService(serviceIntent)

            // 🎧 OPEN PLAYER UI
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("title", music.title)
            intent.putExtra("subtitle", music.subtitle)
            intent.putExtra("imageUrl", music.imageUrl)
            intent.putExtra("audioUrl", music.audioUrl)

            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = musicList.size

    // 🔥 PLAYLIST DIALOG FUNCTION
    private fun showPlaylistDialog(context: Context, music: Music) {

        val user = FirebaseAuth.getInstance().currentUser ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("playlists")
            .document(user.uid)
            .collection("userPlaylists")
            .get()
            .addOnSuccessListener { result ->

                if (result.isEmpty) {
                    Toast.makeText(context, "No playlists found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val names = result.map { it.getString("name") ?: "Unnamed" }
                val ids = result.map { it.id }

                AlertDialog.Builder(context)
                    .setTitle("Add to Playlist")
                    .setItems(names.toTypedArray()) { _, index ->

                        val playlistId = ids[index]

                        db.collection("playlists")
                            .document(user.uid)
                            .collection("userPlaylists")
                            .document(playlistId)
                            .collection("songs")
                            .add(
                                hashMapOf(
                                    "title" to music.title,
                                    "artist" to music.subtitle,
                                    "imageUrl" to music.imageUrl,
                                    "audioUrl" to music.audioUrl,
                                    "timestamp" to System.currentTimeMillis()
                                )
                            )
                            .addOnSuccessListener {
                                Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .show()
            }
    }
}