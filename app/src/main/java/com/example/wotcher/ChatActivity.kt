package com.example.wotcher

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.wotcher.databinding.ActivityChatBinding
import com.example.wotcher.databinding.LeftChatMsgBinding
import com.example.wotcher.databinding.RightChatMsgBinding
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class ChatActivity : AppCompatActivity() {

    private lateinit var chatBinding: ActivityChatBinding
    private lateinit var appUtil: AppUtil
    private lateinit var myId: String
    var chatId: String? = null
    private lateinit var hisId : String


    private var firebaseRecyclerAdapter: FirebaseRecyclerAdapter<MessageModel, ViewHolder>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        chatBinding = ActivityChatBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)

        val name = intent.getStringExtra("name")
        val profileImage = intent.getStringExtra("picture")
        hisId = intent.getStringExtra("userId").toString()



        appUtil = AppUtil()
        myId = appUtil.getUID()!!


        chatBinding.nameChat.text = name

        if (profileImage == null) {
            chatBinding.profilePictureChat.setImageResource(R.drawable.user_no_profile_image);
        } else {
            Picasso.get().load(profileImage).into(chatBinding.profilePictureChat)
        }


        chatBinding.sendMsgBtn.setOnClickListener {

            val message = chatBinding.msgText.text.toString()
            if (message.isEmpty()) {
                Toast.makeText(this, "Enter Message", Toast.LENGTH_SHORT).show()
            } else {
                sendMessage(message)
                chatBinding.msgText.setText("")
            }

        }

        if (chatId == null) {
            checkChat(hisId)
        }



        chatBinding.arrowBackIntent.setOnClickListener {

            var intent = Intent(this@ChatActivity, MainActivity::class.java)
            startActivity(intent)

        }

        setContentView(chatBinding.root)
    }

    private fun checkChat(hisId : String) {

        val databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId)
        val query = databaseReference.orderByChild("member").equalTo(hisId)
        query.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (ds in snapshot.children) {
                    val member = ds.child("member").value.toString()
                    if(member==hisId) {
                        chatId = ds.key
                        readMessages(chatId!!)
                        break
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

    }

    private fun createChat(message: String) {
        var databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId)
        chatId = databaseReference.push().key

        val chatListMode = ChatListModel(hisId, message, System.currentTimeMillis().toString(), hisId)

        databaseReference.child(hisId).setValue(chatListMode)

        databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisId)

        val chatList = ChatListModel(hisId, message, System.currentTimeMillis().toString(), myId)

        databaseReference.child(hisId).setValue(chatList)

        databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(hisId)

        val messageModel = MessageModel(myId, hisId, message, type="text")
        databaseReference.push().setValue(messageModel)
    }

    private fun sendMessage(message: String) {

        if(chatId == null) {
            createChat(message)
        } else {
            var databaseReference = FirebaseDatabase.getInstance().getReference("Chat").child(chatId!!)
            val messageModel = MessageModel(myId, hisId, message, type = "text")
            databaseReference.push().setValue(messageModel)

            val map:MutableMap<String, Any> = HashMap()
            map["lastMessage"] = message
            map["date"] = System.currentTimeMillis().toString()

            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(myId).child(chatId!!)
            databaseReference.updateChildren(map)

            databaseReference = FirebaseDatabase.getInstance().getReference("ChatList").child(hisId).child(chatId!!)
            databaseReference.updateChildren(map)

        }

    }

    private fun readMessages(chatId: String) {

        val query = FirebaseDatabase.getInstance().getReference("Chat").child(chatId)
        val firebaseRecyclerOptions = FirebaseRecyclerOptions.Builder<MessageModel>()
            .setLifecycleOwner(this)
            .setQuery(query,MessageModel::class.java)
            .build()
        query.keepSynced(true)

        firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<MessageModel, ViewHolder>(firebaseRecyclerOptions) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                var viewDataBinding: ViewDataBinding? = null

                if (viewType == 0)
                    viewDataBinding = RightChatMsgBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )

                if (viewType == 1)
                    viewDataBinding = LeftChatMsgBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )

                return ViewHolder(viewDataBinding!!)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int, messageModel: MessageModel) {
                if(getItemViewType(position)==0) {
                    holder.viewDataBinding.setVariable(BR.message, messageModel)
                }

                if(getItemViewType(position)==1) {
                    holder.viewDataBinding.setVariable(BR.message, messageModel)
                }
            }

            override fun getItemViewType(position: Int): Int {
                val messageModel = getItem(position)
                return if (messageModel.senderId==myId)
                    0
                else
                    1
            }

        }

        chatBinding.messageRecyclerView.layoutManager=LinearLayoutManager(this)
        chatBinding.messageRecyclerView.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter!!.startListening()

    }


    class ViewHolder (var viewDataBinding: ViewDataBinding):
            RecyclerView.ViewHolder(viewDataBinding.root)

    override fun onPause() {
        super.onPause()
        if (firebaseRecyclerAdapter != null)
            firebaseRecyclerAdapter!!.stopListening()
    }

}