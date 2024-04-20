package com.example.clonestagram.navigation

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.clonestagram.R
import com.example.clonestagram.navigation.model.ContentDTO
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import java.text.SimpleDateFormat
import java.util.Date

class AddPhotoActivity : AppCompatActivity() {
    var PICK_IMAGE_FROM_ALBUM = 0
    var storage: FirebaseStorage? = null
    var photoUri: Uri? = null
    var auth: FirebaseAuth? = null
    var fireStore: FirebaseFirestore? = null
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
        auth = FirebaseAuth.getInstance()
        fireStore = FirebaseFirestore.getInstance()

        // 앨범 열기
        var photoPickerIntent = Intent(Intent.ACTION_PICK)
        startActivityForResult(photoPickerIntent, PICK_IMAGE_FROM_ALBUM)

        // 이미지 업로드 이벤트
        val addPhotoBtnUpload: Button = findViewById(R.id.addphoto_btn_upload)
        addPhotoBtnUpload.setOnClickListener {
            contentUpload()
        }
    }

    fun contentUpload() {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_.png"
        val storageRef = storage?.reference?.child("images")?.child(imageFileName)

        // 방법 1. promise method
        storageRef?.putFile(photoUri!!)?.continueWithTask { task: Task<UploadTask.TaskSnapshot> ->
            return@continueWithTask storageRef.downloadUrl
        } ?.addOnSuccessListener { uri ->
            var contentDTO = ContentDTO()

            // image 다운로드 url insert
            contentDTO.imageUrl = uri.toString()

            // uid insert
            contentDTO.uid = auth?.currentUser?.uid

            // userid insert
            contentDTO.userId = auth?.currentUser?.email

            // content 설명 insert
            val addPhotoEditExplain: EditText = findViewById(R.id.addphoto_edit_explain)
            contentDTO.explain = addPhotoEditExplain.text.toString()

            // timestamp insert
            contentDTO.timestamp = System.currentTimeMillis()

            fireStore?.collection("images")?.document()?.set(contentDTO)

            setResult(Activity.RESULT_OK)

            finish()

        }

        // 방법 2. callback method
        /*
        storageRef?.putFile(photoUri!!)?.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                var contentDTO = ContentDTO()

                // image 다운로드 url insert
                contentDTO.imageUrl = uri.toString()

                // uid insert
                contentDTO.uid = auth?.currentUser?.uid

                // userid insert
                contentDTO.userId = auth?.currentUser?.email

                // content 설명 insert
                val addPhotoEditExplain: EditText = findViewById(R.id.addphoto_edit_explain)
                contentDTO.explain = addPhotoEditExplain.text.toString()

                // timestamp insert
                contentDTO.timestamp = System.currentTimeMillis()

                fireStore?.collection("images")?.document()?.set(contentDTO)

                setResult(Activity.RESULT_OK)

                finish()

            }
            Toast.makeText(
                this, getString(R.string.upload_success),
                Toast.LENGTH_SHORT
            ).show()


            setResult(Activity.RESULT_OK)
            finish()
        }*/

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