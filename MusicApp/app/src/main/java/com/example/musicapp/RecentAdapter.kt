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
import com.bumptech.glide.Glide   // ✅ IMPORTANT
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RecentAdapter(
    private val list: List<Music>,
    private val context: Context
) : RecyclerView.Adapter<RecentAdapter.ViewHolder>() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {

        val image: ImageView =
            itemView.findViewById(R.id.recentImage)

        val title: TextView =
            itemView.findViewById(R.id.recentTitle)

        val subtitle: TextView =
            itemView.findViewById(R.id.recentSubtitle)

        val addBtn: ImageView =
            itemView.findViewById(R.id.addToPlaylistBtnRecent)
        val favBtn: ImageView =
            itemView.findViewById(R.id.favBtn)

        val playBtn: ImageView =
            itemView.findViewById(R.id.playBtnRecent)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {

        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {

        val item = list[position]

        holder.title.text = item.title
        holder.subtitle.text = item.subtitle

        // ✅ Load image from URL
        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .into(holder.image)

        val user = auth.currentUser

        if (user != null) {

            val favRef = db.collection("favorites")
                .document(user.uid)
                .collection("songs")
                .document(item.id)

            favRef.get()
                .addOnSuccessListener { doc ->

                    if (doc.exists()) {
                        holder.favBtn.setImageResource(
                            android.R.drawable.btn_star_big_on
                        )
                    } else {
                        holder.favBtn.setImageResource(
                            android.R.drawable.btn_star_big_off
                        )
                    }
                }

            holder.addBtn.setOnClickListener {
                showPlaylistDialog(context, item)
            }

            holder.favBtn.setOnClickListener {

                favRef.get()
                    .addOnSuccessListener { doc ->

                        if (doc.exists()) {

                            favRef.delete()

                            holder.favBtn.setImageResource(
                                android.R.drawable.btn_star_big_off
                            )

                            Toast.makeText(
                                context,
                                "Removed from Favorites",
                                Toast.LENGTH_SHORT
                            ).show()

                        } else {

                            val data = hashMapOf(
                                "title" to item.title,
                                "artist" to item.subtitle,
                                "imageUrl" to item.imageUrl,   // ✅ fixed
                                "audioUrl" to item.audioUrl    // ✅ added
                            )

                            favRef.set(data)

                            holder.favBtn.setImageResource(
                                android.R.drawable.btn_star_big_on
                            )

                            Toast.makeText(
                                context,
                                "Added to Favorites",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }

        holder.playBtn.setOnClickListener {
            openPlayer(position)
        }

        holder.itemView.setOnClickListener {
            openPlayer(position)
        }
    }

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

                        Toast.makeText(context, "Added to playlist", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            }
    }

    private fun openPlayer(position: Int) {

        val music = list[position]

        MusicQueue.currentList = list
        MusicQueue.currentIndex = position

        val serviceIntent = Intent(context, MusicService::class.java)
        serviceIntent.putExtra("audioUrl", music.audioUrl)
        serviceIntent.putExtra("title", music.title)
        serviceIntent.putExtra("subtitle", music.subtitle)
        serviceIntent.putExtra("imageUrl", music.imageUrl)
        context.startService(serviceIntent)

        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("title", music.title)
        intent.putExtra("subtitle", music.subtitle)
        intent.putExtra("imageUrl", music.imageUrl)
        intent.putExtra("audioUrl", music.audioUrl)

        context.startActivity(intent)
    }

    override fun getItemCount(): Int = list.size
}