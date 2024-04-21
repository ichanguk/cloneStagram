package com.example.clonestagram.navigation

import android.content.Intent
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.clonestagram.LoginActivity
import com.example.clonestagram.MainActivity
import com.example.clonestagram.R
import com.example.clonestagram.databinding.FragmentUserBinding
import com.example.clonestagram.navigation.model.ContentDTO
import com.example.clonestagram.navigation.model.FollowDTO
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class UserFragment : Fragment() {
    var fragmentView: View? = null
    var firestore: FirebaseFirestore? = null
    var uid: String? = null
    var auth: FirebaseAuth? = null
    var currentUserUid:String? = null
    var _binding: FragmentUserBinding? = null
    val binding get() = _binding!!
    companion object {
        var PICK_PROFILE_FROM_ALBOM = 10
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentUserBinding.inflate(inflater, container, false)
        uid = arguments?.getString("destinationUid")
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth?.currentUser?.uid

        if (uid == currentUserUid) {
            // my page
            binding.accountBtnFollowSignout.text = getString(R.string.signout)
            binding.accountBtnFollowSignout.setOnClickListener {
                activity?.finish()
                startActivity(Intent(activity, LoginActivity::class.java))
                auth?.signOut()
            }

        } else {
            // 다른 사람 페이지
            binding.accountBtnFollowSignout.text = getString(R.string.follow)
            var mainActivity = (activity as MainActivity)
            mainActivity?.findViewById<TextView>(R.id.toolbar_username)?.text = arguments?.getString("userid")
            mainActivity?.findViewById<ImageView>(R.id.toolbar_btn_back)?.setOnClickListener {
                mainActivity.findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.action_home
            }
            mainActivity?.findViewById<ImageView>(R.id.toolbar_title_image)?.visibility = View.GONE
            mainActivity?.findViewById<TextView>(R.id.toolbar_username)?.visibility = View.VISIBLE
            mainActivity?.findViewById<ImageView>(R.id.toolbar_btn_back)?.visibility = View.VISIBLE
            binding.accountBtnFollowSignout.setOnClickListener {
                requestFollow()
            }
        }
        binding.accountRecyclerview.adapter = UserFragmentRecyclerViewAdapter()
        binding.accountRecyclerview.layoutManager = GridLayoutManager(activity, 3)

        binding.accountIvProfile.setOnClickListener {
            var photoPickerIntent = Intent(Intent.ACTION_PICK)
            photoPickerIntent.type = "image/*"
            activity?.startActivityForResult(photoPickerIntent, PICK_PROFILE_FROM_ALBOM)
        }
        getProfileImage()
        getFollowerAndFollowing()
        return binding.root
    }
    fun getFollowerAndFollowing() {
        firestore?.collection("users")?.document(uid!!)?.addSnapshotListener { documentSnapshot, firbaseFirestoreException ->
            if (documentSnapshot == null) {
                return@addSnapshotListener
            }
            var followDTO = documentSnapshot.toObject(FollowDTO::class.java)
            if (followDTO?.followingCount != null) {
                binding.accountTvFollowingCount.text = followDTO?.followingCount?.toString()
            }
            if (followDTO?.followerCount != null) {
                binding.accountTvFollowerCount.text = followDTO?.followerCount?.toString()
                if (followDTO?.followers?.containsKey(currentUserUid!!) == true) {
                    binding.accountBtnFollowSignout.text = getString(R.string.follow_cancel)
                    binding.accountBtnFollowSignout.background?.setColorFilter(ContextCompat.getColor(requireActivity(), R.color.colorLightGray), PorterDuff.Mode.MULTIPLY)
                } else {
                    if (uid != currentUserUid) {
                        binding.accountBtnFollowSignout.text = getString(R.string.follow)
                        binding.accountBtnFollowSignout.background?.colorFilter = null
                    }
                }
            }

        }
    }
    fun requestFollow() {
        // 내 계정 데이터 저장
        var tsDocFollowing = firestore?.collection("users")?.document(currentUserUid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollowing!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followingCount = 1
                followDTO!!.followers[uid!!] = true

                transaction.set(tsDocFollowing, followDTO)
                return@runTransaction
            }

            if (followDTO.followings.containsKey(uid)) {
                // unfollow
                followDTO?.followingCount = followDTO?.followingCount!! - 1
                followDTO?.followers?.remove(uid)
            } else {
                // follow
                followDTO?.followingCount = followDTO?.followingCount!! + 1
                followDTO?.followers?.set(uid!!, true)
            }
            transaction.set(tsDocFollowing, followDTO)
            return@runTransaction
        }
        // 제3자 데이터 저장
        var tsDocFollower = firestore?.collection("users")?.document(uid!!)
        firestore?.runTransaction { transaction ->
            var followDTO = transaction.get(tsDocFollower!!).toObject(FollowDTO::class.java)
            if (followDTO == null) {
                followDTO = FollowDTO()
                followDTO!!.followerCount = 1
                followDTO!!.followers[currentUserUid!!] = true

                transaction.set(tsDocFollower, followDTO!!)
                return@runTransaction

            }

            if (followDTO!!.followers.containsKey(currentUserUid)) {
                // unfollow
                followDTO?.followerCount = followDTO?.followerCount!! - 1
                followDTO?.followers?.remove(currentUserUid)
            } else {
                // follow
                followDTO?.followerCount = followDTO?.followerCount!! + 1
                followDTO?.followers?.set(currentUserUid!!, true)
            }
            transaction.set(tsDocFollower, followDTO!!)
            return@runTransaction


        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    inner class UserFragmentRecyclerViewAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var contentDTOs: ArrayList<ContentDTO> = arrayListOf()
        init {
            firestore?.collection("images")?.whereEqualTo("uid", uid)?.addSnapshotListener { querySnapshot, firebaseFireStore ->
                if (querySnapshot == null) {
                    return@addSnapshotListener
                }

                // 데이터 받기
                for (snapshot in querySnapshot.documents) {
                    contentDTOs.add(snapshot.toObject(ContentDTO::class.java)!!)
                }
                binding.accountTvPostCount.text = contentDTOs.size.toString()
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
            Glide.with(holder.imageView.context).load(contentDTOs[position].imageUrl).apply(RequestOptions().centerCrop()).into(imageview)
        }

    }
    fun getProfileImage() {
        firestore?.collection("profileImages")?.document(uid!!)?.addSnapshotListener { documentSnapshot, error ->
            if (documentSnapshot == null) {
                return@addSnapshotListener
            }
            if (documentSnapshot.data != null) {
                var url = documentSnapshot?.data!!["image"]
                Glide.with(requireActivity()).load(url).apply(RequestOptions().circleCrop()).into(binding.accountIvProfile)
            }
        }
    }
}