package com.studhunter.api.chat.routing

import com.studhunter.api.chat.model.Chat
import com.studhunter.api.chat.model.Connection
import com.studhunter.api.chat.model.MessageDTO
import com.studhunter.api.chat.model.toMessage
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.chat.tables.Chats
import com.studhunter.api.chat.tables.UserChatMessages
import com.studhunter.api.publications.tables.Publications
import com.google.gson.Gson
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.util.*

fun Route.chatRoutes() {
    val connections = Collections.synchronizedSet<Connection?>(LinkedHashSet())

    authenticate {
        webSocket("/chat") {
            val currentUserId = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Unauthorized"))
                return@webSocket
            }

            val chatIdParam = call.parameters["chatID"]
            val pubIdParam = call.parameters["pubID"]

            if (chatIdParam != null && pubIdParam != null) {
                call.respond(
                    status = HttpStatusCode.BadRequest,
                    message = "Both params chatID and pubID cannot be applied"
                )
                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Both params chatID and pubID cannot be applied"))
                return@webSocket
            }

            val thisConnection = Connection(userId = currentUserId, session = this)
            connections.add(thisConnection)

            chatIdParam?.let { chatID ->
                val chat = Chats.fetchChat(chatID)
                val chatMessages = UserChatMessages.getMessages(chatID)

                if (chat == null || chatMessages == null) {
                    call.respond(status = HttpStatusCode.BadRequest, "Chat doesn't exist")
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Chat doesn't exist"))
                    return@webSocket
                }

                chatMessages.forEach { message ->
                    thisConnection.session.send(Frame.Text(Gson().toJson(message)))
                }

                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue

                        val messageDTO = try {
                            Json.decodeFromString<MessageDTO>(frame.readText())
                        } catch (e: Exception) {
                            null
                        } ?: continue

                        val message = messageDTO.toMessage(chatID = chatID, fromID = currentUserId)

                        UserChatMessages.insertMessage(message) ?: run {
                            call.respond(status = HttpStatusCode.Conflict, message = "Failed DB")
                            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Failed to save message in DB"))
                            return@webSocket
                        }

                        connections.forEach {
                            it.session.send(Frame.Text(Gson().toJson(message)))
                        }
                    }
                } finally {
                    connections.remove(thisConnection)
                }
            }

            pubIdParam?.let { pubID ->
                val chatID = Chats.fetchChatID(publicationID = pubID, userID = currentUserId) ?: run {
                    val publication = Publications.getPublication(pubID) ?: run {
                        call.respond(status = HttpStatusCode.BadRequest, "Publication doesn't exist")
                        close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Publication doesn't exist"))
                        return@webSocket
                    }

                    try {
                        var chatID: String? = null

                        for (frame in incoming) {
                            frame as? Frame.Text ?: continue

                            val messageDTO = try {
                                Json.decodeFromString<MessageDTO>(frame.readText())
                            } catch (e: Exception) {
                                null
                            } ?: continue

                            if (chatID == null) {
                                val chat = Chat(
                                    publicationId = pubID,
                                    customerId = currentUserId,
                                    sellerId = publication.userId,
                                    lastMessage = messageDTO.messageBody
                                )
                                chatID = Chats.insertChat(chat) ?: run {
                                    call.respond(status = HttpStatusCode.Conflict, "Couldn't create chat")
                                    close(CloseReason(CloseReason.Codes.INTERNAL_ERROR, "Couldn't create chat"))
                                    return@webSocket
                                }
                            }

                            val message = messageDTO.toMessage(chatID = chatID, fromID = currentUserId)

                            UserChatMessages.insertMessage(message) ?: run {
                                call.respond(status = HttpStatusCode.Conflict, message = "Failed DB")
                                close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Failed to save message in DB"))
                                return@webSocket
                            }

                            connections.forEach {
                                it.session.send(Frame.Text(Gson().toJson(message)))
                            }
                        }
                    } finally {
                        connections.remove(thisConnection)
                    }

                    return@webSocket
                }

                // Commentary

                val chat = Chats.fetchChat(chatID)
                val chatMessages = UserChatMessages.getMessages(chatID)

                if (chat == null || chatMessages == null) {
                    call.respond(status = HttpStatusCode.BadRequest, "Chat doesn't exist")
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Chat doesn't exist"))
                    return@webSocket
                }

                chatMessages.forEach { message ->
                    thisConnection.session.send(Frame.Text(Gson().toJson(message)))
                }

                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue

                        val messageDTO = try {
                            Json.decodeFromString<MessageDTO>(frame.readText())
                        } catch (e: Exception) {
                            null
                        } ?: continue

                        val message = messageDTO.toMessage(chatID = chatID, fromID = currentUserId)

                        UserChatMessages.insertMessage(message) ?: run {
                            call.respond(status = HttpStatusCode.Conflict, message = "Failed DB")
                            close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Failed to save message in DB"))
                            return@webSocket
                        }

                        connections.forEach {
                            it.session.send(Frame.Text(Gson().toJson(message)))
                        }
                    }
                } finally {
                    connections.remove(thisConnection)
                }
            }
        }

        get("chats/get") {
            val userID = call.getAuthenticatedUserID() ?: run {
                call.respond(HttpStatusCode.Unauthorized)
                return@get
            }
            val chats = Chats.fetchChats(userId = userID) ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Some database error")
                return@get
            }
            call.respond(status = HttpStatusCode.OK, chats)
        }

    }


}

