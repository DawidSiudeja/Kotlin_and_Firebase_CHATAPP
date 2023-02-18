package com.example.wotcher

data class ChatListModel(val chatId : String,
                         val lastMessage : String,
                         val date : String,
                         val member: String
                         ) {}