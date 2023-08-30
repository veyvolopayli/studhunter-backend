package com.studhunter.api.chat.routing

import com.studhunter.api.chat.features.TextFrameType
import com.studhunter.api.chat.features.getTextFrameType
import com.studhunter.api.chat.model.*
import com.studhunter.api.chat.tables.Chats
import com.studhunter.api.chat.tables.UserChatMessages
import com.studhunter.api.features.getAuthenticatedUserID
import com.studhunter.api.publications.tables.Publications
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.util.*

fun Route.chatRoutes() {
    val connections = Collections.synchronizedMap<String, MutableSet<Connection>>(LinkedHashMap())

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

            chatIdParam?.let { chatID ->
                val thisConnection = Connection(userID = currentUserId, session = this)
                val chatConnections = connections.getOrPut(chatID) { mutableSetOf() }
                chatConnections.add(thisConnection)
                connections[chatID] = chatConnections

                val chat = Chats.fetchChat(chatID)
                val chatMessages = UserChatMessages.getMessages(chatID)

                if (chat == null || chatMessages == null) {
                    call.respond(status = HttpStatusCode.BadRequest, "Chat doesn't exist")
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Chat doesn't exist"))
                    return@webSocket
                }

                chatMessages.forEach { message ->
                    thisConnection.session.send(Frame.Text(Json.encodeToString(message)))
                }

                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue

                        when(val frameType = getTextFrameType(frame)) {
                            is TextFrameType.TMessage -> {
                                val messageDTO = frameType.data as? MessageDTO ?: continue
                                val message = messageDTO.toMessage(chatID = chatID, fromID = currentUserId)

                                UserChatMessages.insertMessage(message) ?: run {
                                    call.respond(status = HttpStatusCode.Conflict, message = "Failed DB")
                                    close(CloseReason(CloseReason.Codes.PROTOCOL_ERROR, "Failed to save message in DB"))
                                    return@webSocket
                                }

                                connections[chatID]?.forEach { connection ->
                                    connection.session.send(Frame.Text(Json.encodeToString(message)))
                                }
                            }
                            is TextFrameType.TOffer -> {
                                val offerRequest = frameType.data as? OfferRequest ?: continue
                                if (offerRequest.userID == chat.sellerId) {
                                    connections[chatID]?.forEach { connection ->
                                        connection.session.send(Frame.Text(Json.encodeToString(offerRequest)))
                                    }
                                }
                            }
                            is TextFrameType.TOfferResponse -> {
                                val offerResponse = frameType.data as? OfferResponse ?: continue
                                if (offerResponse.userID == chat.customerId) {
                                    connections[chatID]?.forEach { connection ->
                                        connection.session.send(Frame.Text(Json.encodeToString(offerResponse)))
                                    }
                                }
                            }
                            is TextFrameType.TOther -> {
                                continue
                            }
                        }
                    }
                } finally {
                    connections[chatID]?.remove(thisConnection)
                }
            }

            pubIdParam?.let { pubID ->
                val publication = Publications.getPublication(pubID) ?: run {
                    call.respond(status = HttpStatusCode.BadRequest, message = "No publication with this ID")
                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "No publication with this ID"))
                    return@webSocket
                }

                val chat = Chats.fetchChat(publicationID = pubID, userID = currentUserId)
                val chatID = chat?.id

                if (chatID != null) {
                    val thisConnection = Connection(userID = currentUserId, session = this)
                    val chatConnections = connections.getOrPut(chatID) { mutableSetOf() }
                    chatConnections.add(thisConnection)
                    connections[chatID] = chatConnections

                    val messages = UserChatMessages.getMessages(chatID)
                    connections[chatID]?.forEach { connection ->
                        messages?.forEach { message ->
                            connection.session.send(Frame.Text(Json.encodeToString(message)))
                        }
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

                            UserChatMessages.insertMessage(message)

                            connections[chatID]?.forEach { connection ->
                                connection.session.send(Frame.Text(Json.encodeToString(message)))
                            }
                        }
                    } finally {
                        connections[chatID]?.remove(thisConnection)
                    }
                } else {
                    val newChatID = UUID.randomUUID().toString()
                    var newChat: Chat? = null

                    val thisConnection = Connection(userID = currentUserId, session = this)
                    val chatConnections = connections.getOrPut(newChatID) { mutableSetOf() }
                    chatConnections.add(thisConnection)
                    connections[newChatID] = chatConnections

                    try {
                        for (frame in incoming) {
                            frame as? Frame.Text ?: continue

                            val messageDTO = try {
                                Json.decodeFromString<MessageDTO>(frame.readText())
                            } catch (e: Exception) {
                                null
                            } ?: continue

                            val message = messageDTO.toMessage(chatID = newChatID, fromID = currentUserId)

                            connections[newChatID]?.forEach { connection ->
                                connection.session.send(Frame.Text(Json.encodeToString(message)))
                            }

                            UserChatMessages.insertMessage(message) ?: run {
                                close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Save message to db failed"))
                                return@webSocket
                            }

                            if (newChat == null) {
                                newChat = Chat(
                                    id = newChatID,
                                    publicationId = pubID,
                                    customerId = currentUserId,
                                    sellerId = publication.userId,
                                    lastMessage = message.messageBody
                                )
                                Chats.insertChat(newChat) ?: run {
                                    close(CloseReason(CloseReason.Codes.VIOLATED_POLICY, "Fail of creating chat"))
                                    return@webSocket
                                }
                            }
                        }
                    } finally {
                        connections[newChatID]?.remove(thisConnection)
                    }
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

