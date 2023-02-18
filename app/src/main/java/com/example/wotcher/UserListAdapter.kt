package com.example.wotcher

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.wotcher.databinding.UsersItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso

class UserListAdapter(var context : Context
                      , var userList: ArrayList<Users>) : RecyclerView.Adapter<UserListAdapter.UserHolder>() {

    inner class UserHolder(val adapterBinding : UsersItemBinding)
        : RecyclerView.ViewHolder(adapterBinding.root){}


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserHolder {

        val binding = UsersItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return UserHolder(binding)
    }

    override fun onBindViewHolder(holder: UserHolder, position: Int) {

        holder.adapterBinding.textName.text = userList[position].name

        var userID = userList[position].userId
        var auth: FirebaseAuth = FirebaseAuth.getInstance()
        var currentUserID: String = FirebaseAuth.getInstance().currentUser!!.uid

        var databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(currentUserID).child(userID)



        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                auth.currentUser?.let {

                    val lastMessageText = snapshot.child("lastMessage").value

                    if (lastMessageText != null) {
                        holder.adapterBinding.lastMsg.text = lastMessageText.toString()
                    } else {
                        holder.adapterBinding.lastMsg.text = "Chat is empty :C"
                    }


                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


        if (userList[position].profilePicture.isEmpty()) {
            holder.adapterBinding.profilePicture.setImageResource(R.drawable.user_no_profile_image);
        } else{
            Picasso.get().load(userList[position].profilePicture).into(holder.adapterBinding.profilePicture);
        }


        holder.adapterBinding.linearlayout.setOnClickListener {

            val intent = Intent(context, ChatActivity::class.java)
            intent.putExtra("userId", userList[position].userId)
            intent.putExtra("name", userList[position].name)

            if (userList[position].profilePicture.isEmpty()) {
                holder.adapterBinding.profilePicture.setImageResource(R.drawable.user_no_profile_image);
            } else {
                intent.putExtra("picture", userList[position].profilePicture)
            }

            context.startActivity(intent)

        }

    }

    override fun getItemCount(): Int {
        return userList.size
    }


}