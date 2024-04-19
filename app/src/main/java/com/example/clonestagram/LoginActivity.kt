package com.example.clonestagram

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        fun moveMainPage(user: FirebaseUser?) {
            if (user != null) {
                startActivity(Intent(this, MainActivity::class.java))
            }
        }

        fun signinEmail() {
            var emailEditText: EditText = findViewById(R.id.email_edittext)
            var passwordEditText: EditText = findViewById(R.id.password_edittext)
            auth?.signInWithEmailAndPassword(emailEditText.toString(), passwordEditText.toString())
                ?.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login
                        moveMainPage(task.result.user)
                    } else {
                        // Show the error message
                        Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                    }
                }
        }

        fun signinAndSignUp() {
            var emailEditText: EditText = findViewById(R.id.email_edittext)
            var passwordEditText: EditText = findViewById(R.id.password_edittext)
            auth?.createUserWithEmailAndPassword(
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Creating a user account
                    moveMainPage(task.result.user)
                } else if (task.exception?.message.isNullOrBlank()) {
                    // Show the error message
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                } else {
                    // Login if you have account
                    signinEmail()
                }
            }
        }
        val emaliLoginButton: Button = findViewById(R.id.email_login_button)
        emaliLoginButton.setOnClickListener {
            signinAndSignUp()
        }

    }
}