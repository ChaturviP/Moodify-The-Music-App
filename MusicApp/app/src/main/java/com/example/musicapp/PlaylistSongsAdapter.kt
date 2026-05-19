package com.example.musicapp

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

class PlaylistSongsAdapter(
    private val list: MutableList<Music>,private val playlistId: String
) : RecyclerView.Adapter<PlaylistSongsAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.recentTitle)
        val image: ImageView = itemView.findViewById(R.id.recentImage)
        val favBtn: ImageView = itemView.findViewById(R.id.favBtn)

        // 🔥 ADD THIS
        val addBtn: ImageView = itemView.findViewById(R.id.addToPlaylistBtnRecent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recent, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val item = list[position]

        holder.title.text = item.title

        holder.addBtn.setImageResource(
            android.R.drawable.ic_menu_close_clear_cancel
        )
        holder.addBtn.setColorFilter(android.graphics.Color.RED)
        holder.addBtn.setOnClickListener {

            val user = auth.currentUser ?: return@setOnClickListener

            db.collection("playlists")
                .document(user.uid)
                .collection("userPlaylists")
                .document(playlistId)
                .collection("songs")
                .document(item.id)
                .delete()
                .addOnSuccessListener {

                    val pos = holder.adapterPosition
                    if (pos != RecyclerView.NO_POSITION) {
                        list.removeAt(pos)
                        notifyItemRemoved(pos)
                    }

                    Toast.makeText(
                        holder.itemView.context,
                        "Removed from playlist",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }

        Glide.with(holder.itemView.context)
            .load(item.imageUrl)
            .placeholder(android.R.drawable.ic_menu_gallery)
            .error(android.R.drawable.ic_delete)
            .into(holder.image)

        val user = auth.currentUser

        // 🔥 PLAY SONG
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context

            // ✅ Set queue
            MusicQueue.currentList = list
            MusicQueue.currentIndex = position

            // 🔥 Start music service
            val serviceIntent = Intent(context, MusicService::class.java)
            serviceIntent.putExtra("audioUrl", item.audioUrl)
            context.startService(serviceIntent)

            // 🎧 Open player UI
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("title", item.title)
            intent.putExtra("subtitle", item.subtitle)
            intent.putExtra("imageUrl", item.imageUrl)
            intent.putExtra("audioUrl", item.audioUrl)

            context.startActivity(intent)
        }

        if (user != null) {

            val favRef = db.collection("favorites")
                .document(user.uid)
                .collection("songs")
                .document(item.id)

            // 🔥 SET INITIAL STATE
            favRef.get().addOnSuccessListener { doc ->
                if (doc.exists()) {
                    holder.favBtn.setImageResource(android.R.drawable.btn_star_big_on)
                } else {
                    holder.favBtn.setImageResource(android.R.drawable.btn_star_big_off)
                }
            }

            // 🔥 TOGGLE FAVORITE
            holder.favBtn.setOnClickListener {

                favRef.get().addOnSuccessListener { doc ->

                    if (doc.exists()) {

                        favRef.delete()
                        holder.favBtn.setImageResource(android.R.drawable.btn_star_big_off)

                        Toast.makeText(
                            holder.itemView.context,
                            "Removed from favorites",
                            Toast.LENGTH_SHORT
                        ).show()

                    } else {

                        val data = hashMapOf(
                            "title" to item.title,
                            "artist" to item.subtitle,
                            "imageUrl" to item.imageUrl,
                            "audioUrl" to item.audioUrl
                        )

                        favRef.set(data)
                        holder.favBtn.setImageResource(android.R.drawable.btn_star_big_on)

                        Toast.makeText(
                            holder.itemView.context,
                            "Added to favorites",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }


        }
    }

    override fun getItemCount(): Int = list.size
}