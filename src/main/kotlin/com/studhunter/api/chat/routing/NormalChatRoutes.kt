package com.studhunter.api.chat.routing

import com.studhunter.api.chat.model.*
import com.studhunter.api.chat.tables.Chats
import com.studhunter.api.chat.tables.Tasks
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
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import java.util.*

private const val INCOMING_TYPE_MESSAGE = "message"
private const val INCOMING_TYPE_DEAL_REQUEST = "deal_request"
private const val INCOMING_TYPE_DEAL_RESPONSE = "deal_response"

fun Route.normalChatRoutes() {
    val connections = Collections.synchronizedMap<String, MutableSet<Connection>>(LinkedHashMap())
    val offerRequests = Collections.synchronizedMap<String, OfferRequest>(LinkedHashMap())

    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(DataTransfer::class) {
                subclass(OfferRequest::class)
                subclass(OfferRequestDTO::class)
                subclass(OfferResponse::class)
                subclass(OfferResponseDTO::class)
                subclass(Message::class)
                subclass(MessageDTO::class)
            }
        }
    }

    authenticate {
        webSocket("/chat") {
            val chatIdParam = call.parameters["chatID"]
            val pubIdParam = call.parameters["pubID"]

            if (chatIdParam != null && pubIdParam != null) {
                call.respond(HttpStatusCode.BadRequest)
                return@webSocket
            }

            val currentUserID = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "Invalid JWT")
                return@webSocket
            }

            chatIdParam?.let { chatID ->
                // Fetching chat by id
                val chat = Chats.fetchChat(chatID = chatID) ?: run {
                    call.respond(status = HttpStatusCode.BadRequest, "Chat doesn't exist")
                    return@webSocket
                }

                val thisConnection = Connection(currentUserID, this)

                val chatConnections = connections.getOrPut(chatID) {
                    mutableSetOf()
                }
                connections[chatID] = chatConnections.also {
                    it.add(thisConnection)
                }

                // Fetching messages that exists in chat that was found
//                UserChatMessages.getMessages(chatID)?.forEach { message ->
//                    send(Frame.Text(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_MESSAGE, data = message))))
//                }

                // Listen session incoming frames
                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val textFrame = frame.readText()

                        val incomingTextFrame = json.decodeFromString<IncomingTextFrame>(textFrame)
                        when(incomingTextFrame.type) {
                            INCOMING_TYPE_MESSAGE -> {
                                try {
                                    val messageDto = incomingTextFrame.data as? MessageDTO ?: continue
                                    println(messageDto)
                                    val message = messageDto.toMessage(chatID = chatID, fromID = currentUserID)
                                    UserChatMessages.insertMessage(message) ?: continue
                                    connections[chatID]?.forEach { connection ->
                                        connection.session.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_MESSAGE, data = message)))
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'message'")
                                }
                            }
                            INCOMING_TYPE_DEAL_REQUEST -> {
                                try {
                                    val dealRequestDto = incomingTextFrame.data as? OfferRequestDTO ?: continue
                                    println("DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL DEAL ")
                                    val dealRequest = dealRequestDto.toOfferRequest(chatID = chatID)
                                    offerRequests[chatID] = dealRequest
                                    connections[chatID]?.find { it.userID == chat.sellerId }?.session?.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_DEAL_REQUEST, data = dealRequest)))
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'deal request'")
                                }
                            }
                            INCOMING_TYPE_DEAL_RESPONSE -> {
                                try {
                                    val dealResponseDto = incomingTextFrame.data as? OfferResponseDTO ?: continue
                                    offerRequests[chatID]?.let { request ->
                                        val dealResponse = dealResponseDto.toOfferResponse(chatID = chatID, requestID = request.id)
                                        if (currentUserID == chat.customerId) {
                                            connections[chatID]?.forEach { connection ->
                                                if (connection.userID == chat.sellerId) {
                                                    if (dealResponse.positive) {
                                                        val task = Task(
                                                            executorID = chat.sellerId,
                                                            customerID = chat.customerId,
                                                            publicationID = chat.publicationId,
                                                            chatID = chatID,
                                                            deadlineTimestamp = request.jobDeadline
                                                        )
                                                        Tasks.insertTask(task)
                                                    } else {
                                                        offerRequests.remove(chatID)
                                                    }
                                                    connection.session.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_DEAL_RESPONSE, data = dealResponse)))
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'deal response'")
                                }
                            }
                        }
                    }
                } finally {
                    connections[chatID]?.remove(thisConnection)
                    close()
                }

            } ?: pubIdParam?.let { pubID ->
                //todo Тут мы получаем чат с помощью функции getChatOrCreate: Chat?

                val chat = Chats.getChatOrCreate(userId = currentUserID, publicationId = pubID) ?: run {
                    close()
                    call.respond(status = HttpStatusCode.Conflict, "Terrible error was occurred")
                    return@webSocket
                }

                val thisConnection = Connection(currentUserID, this)

                val chatConnections = connections.getOrPut(chat.id) {
                    mutableSetOf()
                }
                connections[chat.id] = chatConnections.also {
                    it.add(thisConnection)
                }

//                val publication = Publications.getPublication(pubID) ?: run {
//                    call.respond(status = HttpStatusCode.BadRequest, message = "Publication not found")
//                    return@webSocket
//                }
//
//                val thisConnection = Connection(currentUserID, this)
//
//                val chat = Chats.fetchChat(publicationID = pubID, userID = currentUserID) ?: run {
//                    val chat = Chat(
//                        publicationId = pubID,
//                        customerId = currentUserID,
//                        sellerId = publication.userId,
//                        lastMessage = ""
//                    )
//                    Chats.insertChat(chat)
//                    chat
//                }
//
//                val chatConnections = connections.getOrPut(chat.id) {
//                    mutableSetOf()
//                }
//                connections[chat.id] = chatConnections.also {
//                    it.add(thisConnection)
//                }

                /*UserChatMessages.getMessages(chat.id)?.forEach { message ->
                    send(Frame.Text(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_MESSAGE, data = message))))
                }*/

                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val textFrame = frame.readText()

                        val incomingTextFrame = json.decodeFromString<IncomingTextFrame>(textFrame)

                        when(incomingTextFrame.type) {
                            INCOMING_TYPE_MESSAGE -> {
                                try {
                                    val messageDto = incomingTextFrame.data as? MessageDTO ?: continue
                                    println(messageDto)
                                    val message = messageDto.toMessage(chatID = chat.id, fromID = currentUserID)
                                    UserChatMessages.insertMessage(message) ?: continue
                                    connections[chat.id]?.forEach { connection ->
                                        connection.session.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_MESSAGE, data = message)))
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'message'")
                                }
                            }
                            INCOMING_TYPE_DEAL_REQUEST -> {
                                try {
                                    val dealRequestDto = incomingTextFrame.data as? OfferRequestDTO ?: continue
                                    val dealRequest = dealRequestDto.toOfferRequest(chatID = chat.id)
                                    offerRequests[chat.id] = dealRequest
                                    if (currentUserID == chat.sellerId) {
                                        connections[chat.id]?.forEach { connection ->
                                            if (connection.userID == chat.customerId) {
                                                connection.session.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_DEAL_REQUEST, data = dealRequest)))
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'deal request'")
                                }
                            }
                            INCOMING_TYPE_DEAL_RESPONSE -> {
                                try {
                                    val dealResponseDto = incomingTextFrame.data as? OfferResponseDTO ?: continue
                                    offerRequests[chat.id]?.let { request ->
                                        val dealResponse = dealResponseDto.toOfferResponse(chatID = chat.id, requestID = request.id)
                                        if (currentUserID == chat.customerId) {
                                            connections[chat.id]?.forEach { connection ->
                                                if (connection.userID == chat.sellerId) {
                                                    if (dealResponse.positive) {
                                                        val task = Task(
                                                            executorID = chat.sellerId,
                                                            customerID = chat.customerId,
                                                            publicationID = chat.publicationId,
                                                            chatID = chat.id,
                                                            deadlineTimestamp = request.jobDeadline
                                                        )
                                                        Tasks.insertTask(task)
                                                    } else {
                                                        offerRequests.remove(chat.id)
                                                    }
                                                    connection.session.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_DEAL_RESPONSE, data = dealResponse)))
                                                }
                                            }
                                        }
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'deal response'")
                                }
                            }
                        }
                    }
                } finally {
                    connections[chat.id]?.remove(thisConnection)
                    close()
                }

            } ?: call.respond(status = HttpStatusCode.BadRequest, message = "Chat ID or publication ID required")
        }

        webSocket {

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

        get("chat/by-chat_id/{chatID}/messages") {
            val chatID = call.parameters["chatID"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val messages = UserChatMessages.getMessages(chatID) ?: run {
                call.respond(status = HttpStatusCode.Conflict, "Failed to fetch messages")
                return@get
            }
            call.respond(status = HttpStatusCode.OK, message = messages)
        }

        get("chat/by-publication_id/{pubID}/messages") {
            val pubID = call.parameters["pubID"] ?: run {
                call.respond(HttpStatusCode.BadRequest)
                return@get
            }
            val currentUserId = call.getAuthenticatedUserID() ?: run {
                call.respond(status = HttpStatusCode.Conflict, message = "JWT exception was occurred")
                return@get
            }
            val messages = UserChatMessages.getMessages(userId = currentUserId, publicationId = pubID) ?: run {
                call.respond(status = HttpStatusCode.Conflict, "Failed to fetch messages")
                return@get
            }
            call.respond(status = HttpStatusCode.OK, message = messages)
        }
    }
}