package com.example.clonestagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clonestagram.R
import com.google.android.gms.tasks.OnFailureListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import java.text.SimpleDateFormat
import java.util.Date

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_photo)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        // storage 초기화
        storage = FirebaseStorage.getInstance()

        // 앨범 열기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // 이미지 업로드 이벤트
        val addPhotoBtnUpload: Button = findViewById(R.id.addphoto_btn_upload)
        addPhotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    internal inner class MyFailureListener : OnFailureListener {
        override fun onFailure(exception: Exception) {
            val errorCode = (exception as StorageException).errorCode
            val errorMessage = exception.message
            // test the errorCode and errorMessage, and handle accordingly
            Log.e("failfailfail", errorMessage.toString())
        }
    }

    fun contentUpload() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)
        val failureListener = MyFailureListener()

        storageRef?.putFile(photoUri!!)?.addOnSuccessListener { taskSnapshot ->

            Toast.makeText(
                this, getString(R.string.upload_success),
                Toast.LENGTH_SHORT
            ).show()


            setResult(Activity.RESULT_OK)
            finish()
        }

        storageRef?.putFile(photoUri!!)?.addOnFailureListener(this, failureListener)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE_FROM_ALBUM) {
            if (resultCode == Activity.RESULT_OK) {
                // 선택된 이미지 경로
                photoUri = data?.data
                val addPhotoImage: ImageView = findViewById(R.id.addphoto_image)
                addPhotoImage.setImageURI(photoUri)
            } else {
                // 취소 버튼 눌렀을때 경로
                finish()
            }
        }
    }
}