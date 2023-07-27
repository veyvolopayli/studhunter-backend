package com.example.chat

import com.example.chat.model.Message
import io.ktor.http.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*

fun Route.chat() {
//    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())
//    webSocket("/chat") {
//        println("Adding user!")
//        val thisConnection = Connection(this)
//        connections += thisConnection
//        try {
//            send("You are connected! There are ${connections.count()} users here.")
//            for (frame in incoming) {
//                frame as? Frame.Text ?: continue
//                val receivedText = frame.readText()
//                val textWithUsername = "[${thisConnection.name}]: $receivedText"
//                connections.forEach {
//                    it.session.send(textWithUsername)
//                }
//            }
//        } catch (e: Exception) {
//            println(e.localizedMessage)
//        } finally {
//            println("Removing $thisConnection!")
//            connections -= thisConnection
//        }
//    }

    val chatIds = mutableListOf<String>()
    val messages = mapOf<String, MutableList<Message>>()

    webSocket("chat/{id}") {
        val id = call.parameters["id"] ?: run {
            call.respond(status = HttpStatusCode.BadRequest, "Chat id is required")
            return@run
        }




        /*if (id !in chats) {
            if (id !is String) return@webSocket
            chats.add(id)
        } else {

        }*/

    }
}