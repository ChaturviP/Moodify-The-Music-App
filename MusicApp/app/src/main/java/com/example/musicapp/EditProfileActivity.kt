package com.example.musicapp

import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class EditProfileActivity : AppCompatActivity() {

    private lateinit var editName: EditText
    private lateinit var editBio: EditText
    private lateinit var saveBtn: Button
    private lateinit var profileImage: ImageView

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        editName = findViewById(R.id.editName)
        editBio = findViewById(R.id.editBio)
        saveBtn = findViewById(R.id.saveBtn)
        profileImage = findViewById(R.id.editProfileImage)

        val user = auth.currentUser

        if (user != null) {

            // 🔹 Load existing data
            db.collection("users")
                .document(user.uid)
                .get()
                .addOnSuccessListener { doc ->

                    val name = doc.getString("name") ?: ""
                    val bio = doc.getString("bio") ?: ""
                    val imageUrl = doc.getString("profileImageUrl")

                    editName.setText(name)
                    editBio.setText(bio)

                    // 🔥 Load avatar preview
                    if (!imageUrl.isNullOrEmpty()) {
                        Glide.with(this)
                            .load(imageUrl)
                            .into(profileImage)
                    }
                }
        }

        // 🔥 Save changes
        saveBtn.setOnClickListener {

            val name = editName.text.toString().trim()
            val bio = editBio.text.toString().trim()

            if (name.isEmpty()) {
                editName.error = "Name required"
                return@setOnClickListener
            }

            if (user != null) {

                val avatarUrl =
                    "https://api.dicebear.com/7.x/initials/png?seed=${name.replace(" ", "%20")}"

                val data = hashMapOf(
                    "name" to name,
                    "bio" to bio,
                    "profileImageUrl" to avatarUrl
                )

                db.collection("users")
                    .document(user.uid)
                    .update(data as Map<String, Any>)
                    .addOnSuccessListener {

                        Toast.makeText(
                            this,
                            "Profile updated",
                            Toast.LENGTH_SHORT
                        ).show()

                        finish()
                    }
                    .addOnFailureListener {

                        Toast.makeText(
                            this,
                            "Update failed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        }
    }
}