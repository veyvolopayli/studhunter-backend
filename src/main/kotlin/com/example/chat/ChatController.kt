package com.example.chat

import com.example.chat.model.ChatMember
import com.example.chat.model.Message
import com.example.postgresdatabase.chat.UserChat
import com.google.gson.Gson
import io.ktor.websocket.*
import java.util.concurrent.ConcurrentHashMap

class ChatController {
    private val chatMembers = ConcurrentHashMap<String, ChatMember>()

    fun onJoin(
        id: String,
        sessionId: String,
        socket: WebSocketSession
    ) {
        if (chatMembers.containsKey(id)) {
            return
        }
        chatMembers[id] = ChatMember(id, sessionId, socket)
    }

    suspend fun sendMessage(message: Message) {
        UserChat.insertMessage(message)

        val parsedMessage = Gson().toJson(message)
        chatMembers.values.forEach { chatMember ->
            chatMember.socket.send(Frame.Text(parsedMessage))
        }
    }

    suspend fun tryDisconnect(userId: String) {
        chatMembers[userId]?.socket?.close()
        if (chatMembers.containsKey(userId)) {
            chatMembers.remove(userId)
        }
    }
}