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
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient : GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("905164990116-87pkt6kncmfpgu6snc9han9v7bpidh96.apps.googleusercontent.com")
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(this, gso)


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val emaliLoginButton: Button = findViewById(R.id.email_login_button)
        emaliLoginButton.setOnClickListener {
            signinAndSignUp()
        }
        val googleSignInButton: Button = findViewById(R.id.google_sign_inbutton)
        googleSignInButton.setOnClickListener{
            //First step
            googleLogin()
        }
    }
    fun firebaseAuthWithGoogle(account: GoogleSignInAccount?) {
        var credential = GoogleAuthProvider.getCredential(account?.idToken, null)
        auth?.signInWithCredential(credential)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }

            if (result != null) {
                if (result.isSuccess) {
                    var account = result.signInAccount
                    //second step
                    firebaseAuthWithGoogle(account)
                }
            }
        }
    }
    fun googleLogin() {
        var signInIntent =  googleSignInClient?.signInIntent
        startActivityForResult(signInIntent!!, GOOGLE_LOGIN_CODE)
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
                    moveMainPage(task.result?.user)
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
                moveMainPage(task.result?.user)
            } else if (task.exception?.message.isNullOrBlank()) {
                // Show the error message
                Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
            } else {
                // Login if you have account
                signinEmail()
            }
        }
    }

}