package com.example.clonestagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.clonestagram.R
import com.example.clonestagram.databinding.FragmentGridBinding
import com.example.clonestagram.databinding.FragmentUserBinding
import com.example.clonestagram.navigation.model.ContentDTO
import com.google.firebase.firestore.FirebaseFirestore

class GridFragment : Fragment() {
    var firestore: FirebaseFirestore? = null
    var _binding: FragmentGridBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentGridBinding.inflate(inflater, container, false)
        firestore = FirebaseFirestore.getInstance()
        binding.gridfragmentRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.gridfragmentRecyclerview.layoutManager = GridLayoutManager(activity, 3)
        return binding.root
    }
    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.addSnapshotListener { querySnapshot, firebaseFireStore ->
                if (querySnapshot == null) {
                    return@addSnapshotListener
                }

                // 데이터 받기
                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                notifyDataSetChanged()
            }
        }


        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var width = resources.displayMetrics.widthPixels / 3
            var imageview = ImageView(parent.context)
            imageview.layoutParams = LinearLayoutCompat.LayoutParams(width, width)
            return CustomViewHolder(imageview)
        }

        inner class  CustomViewHolder(var imageView: ImageView): RecyclerView.ViewHolder(imageView) {
        }

        override fun getItemCount(): Int {
            return contentDTOs.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var imageview = (holder as CustomViewHolder).imageView
            Glide.with(holder.imageView.context).load(contentDTOs[position].imageUrl).apply(
                RequestOptions().centerCrop()).into(imageview)
        }

    }
}