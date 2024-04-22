package com.example.clonestagram.navigation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.clonestagram.R
import com.example.clonestagram.databinding.FragmentAlarmBinding
import com.example.clonestagram.navigation.model.AlarmDTO
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.toObject

class AlarmFragment : Fragment() {
    var _binding: FragmentAlarmBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlarmBinding.inflate(inflater, container, false)
        binding.alarmfragmentRecyclerview.adapter = AlarmRecyclerviewAdapter()
        binding.alarmfragmentRecyclerview.layoutManager = LinearLayoutManager(activity)
        return binding.root
    }

    inner class AlarmRecyclerviewAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
        var alarmDTOList: ArrayList<AlarmDTO> = arrayListOf()
        init {
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            FirebaseFirestore.getInstance().collection("alarms").whereEqualTo("destinationUid", uid).addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                alarmDTOList.clear()
                if (querySnapshot == null) return@addSnapshotListener

                for (snapshot in querySnapshot.documents) {
                    alarmDTOList.add(snapshot.toObject(AlarmDTO::class.java)!!)
                }
                notifyDataSetChanged()

            }
        }
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
            var view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_comment, parent, false)
            return CustomViewHolder(view)
        }

        inner class CustomViewHolder(view: View) : RecyclerView.ViewHolder(view)

        override fun getItemCount(): Int {
            return alarmDTOList.size
        }

        override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            var view = holder.itemView
            FirebaseFirestore.getInstance().collection("profileImages").document(alarmDTOList[position].uid!!).get().addOnCompleteListener {task ->
                if (task.isSuccessful) {
                    val url = task.result["image"]
                    Glide.with(view.context).load(url).apply(RequestOptions().circleCrop()).into(view.findViewById<ImageView>(R.id.commentviewitem_imageview_profile))
                }
            }
            when (alarmDTOList[position].kind) {
                0 -> {
                    val str_0 = alarmDTOList[position].userId + getString(R.string.alarm_favorite)
                    view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = str_0
                }
                1 -> {
                    val str_1 = alarmDTOList[position].userId + " " + getString(R.string.alarm_comment) + " of " + alarmDTOList[position].message
                    view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = str_1
                }
                2 -> {
                    val str_2 = alarmDTOList[position].userId + " " + getString(R.string.alarm_follow)
                    view.findViewById<TextView>(R.id.commentviewitem_textview_profile).text = str_2
                }

            }
            view.findViewById<TextView>(R.id.commentviewitem_textview_comment).visibility = View.INVISIBLE
        }

    }
}