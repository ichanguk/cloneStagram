package com.example.clonestagram

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.facebook.AccessToken
import com.facebook.CallbackManager
import com.facebook.FacebookButtonBase
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.GraphRequest
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.facebook.login.widget.LoginButton
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FacebookAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.Arrays
import java.util.Base64.getEncoder
import kotlin.io.encoding.Base64


class LoginActivity : AppCompatActivity() {
    var auth: FirebaseAuth? = null
    var googleSignInClient: GoogleSignInClient? = null
    var GOOGLE_LOGIN_CODE = 9001
    var callbackManager: CallbackManager? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)


        // firebase 로그인 통합 관리 object 생성
        auth = FirebaseAuth.getInstance()

        // 구글 로그인 옵션
        var gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("905164990116-87pkt6kncmfpgu6snc9han9v7bpidh96.apps.googleusercontent.com")
            .requestEmail()
            .build()

        // 구글 로그인 클래스
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
        googleSignInButton.setOnClickListener {
            //First step
            googleLogin()
        }
        var accessToken = AccessToken.getCurrentAccessToken()
        var isLoggedIn = accessToken != null && !accessToken.isExpired
        if (isLoggedIn) {
            LoginManager.getInstance().logOut()
        }


        //printHashKey()
        callbackManager = CallbackManager.Factory.create()
        val facebookSignInButton: LoginButton = findViewById(R.id.facebook_login_button)
        facebookSignInButton.setReadPermissions(Arrays.asList("public_profile"))

        facebookSignInButton.setOnClickListener {
            accessToken = AccessToken.getCurrentAccessToken()
            isLoggedIn = accessToken != null && !accessToken!!.isExpired
            if (isLoggedIn) {
                LoginManager.getInstance().logOut()
            }
        }

        facebookSignInButton.registerCallback(callbackManager!!, object : FacebookCallback<LoginResult> {
            override fun onCancel() {
            }

            override fun onError(error: FacebookException) {
            }

            override fun onSuccess(result: LoginResult) {
                handleFacebookAccessToken(result.accessToken)
            }
        });

    }


    fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        var credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //다음페이지 호출
                    moveMainPage(auth?.currentUser)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        callbackManager?.onActivityResult(resultCode, resultCode, data)
        if (requestCode == GOOGLE_LOGIN_CODE) {
            var result = data?.let { Auth.GoogleSignInApi.getSignInResultFromIntent(it) }

            if (result != null) {
                if (result.isSuccess) {
                    var account = result.signInAccount
                    //second step
                    firebaseAuthWithGoogle(account!!)
                }
            }
        }
    }

    // pxoUVSMQe2u1pUektdXrQruohE4=
    fun printHashKey() {
        try {
            val info = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(getEncoder().encode(md.digest()))
                Log.i("TAG", "printHashKey() Hash Key: $hashKey")
            }
        } catch (e: Exception) {
            Log.e("TAG", "error:", e)
        }

    }

    override fun onStart() {
        super.onStart()
        moveMainPage(auth?.currentUser)
    }

    fun googleLogin() {
        var signInIntent = googleSignInClient?.signInIntent
        if (signInIntent != null) {
            startActivityForResult(signInIntent, GOOGLE_LOGIN_CODE)
        }
    }


    fun handleFacebookAccessToken(token: AccessToken?) {
        var credential = FacebookAuthProvider.getCredential(token?.token!!)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    //Third step
                    //다음페이지 호출
                    moveMainPage(auth?.currentUser)
                } else {
                    Toast.makeText(this, task.exception?.message, Toast.LENGTH_LONG).show()
                }
            }
    }

    fun moveMainPage(user: FirebaseUser?) {
        if (user != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
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
            } else {
                // Login if you have account
                signinEmail()
            }
        }
    }

}