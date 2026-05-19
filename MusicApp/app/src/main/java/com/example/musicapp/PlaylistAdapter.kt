package com.example.musicapp

import android.app.AlertDialog
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PlaylistAdapter(private val list: MutableList<Playlist>) :
    RecyclerView.Adapter<PlaylistAdapter.ViewHolder>() {

    private val db = FirebaseFirestore.getInstance()
    private val user = FirebaseAuth.getInstance().currentUser

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.playlistName)
        val desc: TextView = itemView.findViewById(R.id.playlistDesc)
        val mood: TextView = itemView.findViewById(R.id.playlistMood)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_playlist, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val playlist = list[position]

        holder.name.text = playlist.name

        holder.desc.text =
            if (playlist.description.isEmpty()) "No description"
            else playlist.description

        holder.mood.text =
            if (playlist.mood.isEmpty()) ""
            else "Mood: ${playlist.mood}"

        // 🔥 NORMAL CLICK (UNCHANGED)
        holder.itemView.setOnClickListener {

            val context = holder.itemView.context

            if (playlist.id.isEmpty()) {
                Toast.makeText(context, "Invalid playlist", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val intent = Intent(context, PlaylistSongsActivity::class.java)
            intent.putExtra("playlistId", playlist.id)
            intent.putExtra("playlistName", playlist.name)

            context.startActivity(intent)
        }

        // 🔥 LONG PRESS → DELETE
        holder.itemView.setOnLongClickListener {

            val context = holder.itemView.context

            if (user == null) return@setOnLongClickListener true

            AlertDialog.Builder(context)
                .setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete this playlist?")
                .setPositiveButton("Delete") { _, _ ->

                    db.collection("playlists")
                        .document(user.uid)
                        .collection("userPlaylists")
                        .document(playlist.id)
                        .delete()
                        .addOnSuccessListener {

                            val pos = holder.adapterPosition
                            if (pos != RecyclerView.NO_POSITION) {
                                list.removeAt(pos)
                                notifyItemRemoved(pos)
                            }

                            Toast.makeText(
                                context,
                                "Playlist deleted",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                }
                .setNegativeButton("Cancel", null)
                .show()

            true
        }
    }

    override fun getItemCount(): Int = list.size
}