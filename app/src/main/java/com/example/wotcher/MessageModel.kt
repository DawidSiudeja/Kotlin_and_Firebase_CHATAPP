package com.example.wotcher

data class MessageModel(val senderId : String? = "",
                        val receiverId : String? = "",
                        val message : String? = "",
                        val date : String = System.currentTimeMillis().toString(),
                        val type: String? = ""
                         ) {}