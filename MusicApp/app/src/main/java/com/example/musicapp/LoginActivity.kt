package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

import com.google.android.gms.auth.api.signin.*
import com.google.android.gms.common.api.ApiException

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var googleClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()

        // Auto Login
        if (auth.currentUser != null) {
            startActivity(Intent(this, HomeActivity::class.java))
            finish()
        }

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val loginBtn = findViewById<Button>(R.id.loginBtn)
        val signupText = findViewById<TextView>(R.id.signupText)
        val googleBtn = findViewById<Button>(R.id.btnGoogle)

        // Google Config
        val gso =
            GoogleSignInOptions.Builder(
                GoogleSignInOptions.DEFAULT_SIGN_IN
            )
                .requestIdToken(
                    getString(R.string.default_web_client_id)
                )
                .requestEmail()
                .build()

        googleClient = GoogleSignIn.getClient(this, gso)

        // Email Login
        loginBtn.setOnClickListener {

            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()

            when {
                emailText.isEmpty() ->
                    email.error = "Enter email"

                passText.isEmpty() ->
                    password.error = "Enter password"

                else -> {

                    auth.signInWithEmailAndPassword(
                        emailText,
                        passText
                    ).addOnCompleteListener {

                        if (it.isSuccessful) {

                            val user = FirebaseAuth.getInstance().currentUser
                            val db = FirebaseFirestore.getInstance()

                            if (user != null) {

                                val userRef = db.collection("users").document(user.uid)

                                userRef.get().addOnSuccessListener { doc ->

                                    if (!doc.exists()) {

                                        val data = hashMapOf(
                                            "name" to (user.displayName ?: "User"),
                                            "email" to user.email,
                                            "bio" to ""
                                        )

                                        userRef.set(data)
                                    }

                                    // 🚀 move AFTER Firestore check
                                    startActivity(
                                        Intent(this, HomeActivity::class.java)
                                    )
                                    finish()
                                }
                            }
                        } else {

                            Toast.makeText(
                                this,
                                "Invalid Credentials",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        // Google Login
        googleBtn.setOnClickListener {
            startActivityForResult(
                googleClient.signInIntent,
                100
            )
        }

        signupText.setOnClickListener {
            startActivity(
                Intent(
                    this,
                    SignupActivity::class.java
                )
            )
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100) {

            val task =
                GoogleSignIn.getSignedInAccountFromIntent(data)

            try {

                val account =
                    task.getResult(ApiException::class.java)

                firebaseAuthWithGoogle(
                    account.idToken!!
                )

            } catch (e: Exception) {

                Toast.makeText(
                    this,
                    "Google Login Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(
        idToken: String
    ) {

        val credential =
            GoogleAuthProvider.getCredential(
                idToken,
                null
            )

        auth.signInWithCredential(
            credential
        ).addOnCompleteListener {

            if (it.isSuccessful) {

                val user = FirebaseAuth.getInstance().currentUser
                val db = FirebaseFirestore.getInstance()

                if (user != null) {

                    val userRef = db.collection("users").document(user.uid)

                    userRef.get().addOnSuccessListener { doc ->

                        if (!doc.exists()) {

                            val data = hashMapOf(
                                "name" to (user.displayName ?: "User"),
                                "email" to user.email,
                                "bio" to ""
                            )

                            userRef.set(data)
                        }

                        startActivity(Intent(this, HomeActivity::class.java))
                        finish()
                    }
                }
            } else {

                Toast.makeText(
                    this,
                    "Authentication Failed",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

}