package com.example.clonestagram.navigation

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.clonestagram.R
import com.example.clonestagram.databinding.ActivityCommentBinding
import com.example.clonestagram.navigation.model.ContentDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class CommentActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCommentBinding
    var contentUid: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_comment)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        binding = ActivityCommentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        contentUid = intent.getStringExtra("contentUid")

        binding.commentRecyclerview.adapter = CommentRecyclerviewAdapter()
        binding.commentRecyclerview.layoutManager = LinearLayoutManager(this)
        binding.commentBtnSend.setOnClickListener {
            var comment = ContentDTO.Comment()
            comment.userId = FirebaseAuth.getInstance().currentUser?.email
            comment.uid = FirebaseAuth.getInstance().currentUser?.uid
            comment.comment = binding.commentEditMessage.text.toString()
            comment.timestamp = System.currentTimeMillis()

            FirebaseFirestore.getInstance().collection("images").document(contentUid!!)
                .collection("comments").document().set(comment)
            binding.commentEditMessage.setText("")
        }

    }

    inner class CommentRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var comments: ArrayList<ContentDTO.Comment> = arrayListOf()

        init {
            FirebaseFirestore.getInstance()
                .collection("images")
                .document(contentUid!!)
                .collection("comments")
                .orderBy("timestamp")
                .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                    comments.clear()
                    if (querySnapshot == null) {
                        return@addSnapshotListener

                    }
                    for (snapshot in querySnapshot?.documents!!) {
                        comments.add(snapshot.toObject(ContentDTO.Comment::class.java)!!)

                    }
                    notifyDataSetChanged()


                }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        private inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return comments.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).text =
                comments[position].comment
            view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text =
                comments[position].userId

            FirebaseFirestore.getInstance()
                .collection("profileImages")
                .document(comments[position].uid!!)
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        var url = task.result!!["image"]
                        Glide.with(holder.itemView.context).load(url)
                            .apply(RequestOptions().circleCrop())
                            .into(view.findViewById<ImageView>(R.id.commentviewitem_imageview_profile))
                    }
                }
        }

    }
}