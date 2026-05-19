package com.example.musicapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        auth = FirebaseAuth.getInstance()

        val email = findViewById<EditText>(R.id.email)
        val password = findViewById<EditText>(R.id.password)
        val confirmPassword = findViewById<EditText>(R.id.confirmPassword)
        val signupBtn = findViewById<Button>(R.id.signupBtn)
        val loginText = findViewById<TextView>(R.id.loginText)

        signupBtn.setOnClickListener {

            val emailText = email.text.toString().trim()
            val passText = password.text.toString().trim()
            val confirmText = confirmPassword.text.toString().trim()

            when {
                emailText.isEmpty() -> email.error = "Enter email"
                passText.length < 6 -> password.error = "Minimum 6 characters"
                passText != confirmText ->
                    confirmPassword.error = "Passwords do not match"

                else -> {

                    auth.createUserWithEmailAndPassword(
                        emailText,
                        passText
                    ).addOnCompleteListener {

                        if (it.isSuccessful) {

                            Toast.makeText(
                                this,
                                "Account Created",
                                Toast.LENGTH_SHORT
                            ).show()

                            startActivity(
                                Intent(
                                    this,
                                    HomeActivity::class.java
                                )
                            )
                            finish()

                        } else {

                            Toast.makeText(
                                this,
                                it.exception?.message,
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            }
        }

        loginText.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}