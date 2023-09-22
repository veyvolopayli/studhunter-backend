package com.studhunter.api.chat.routing

import com.studhunter.api.chat.model.*
import com.studhunter.api.chat.tables.Chats
import com.studhunter.api.chat.tables.Tasks
import com.studhunter.api.chat.tables.UserChatMessages
import com.studhunter.api.features.getAuthenticatedUserID
import io.ktor.http.*
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

private const val TRANSFERRING_TYPE_MESSAGE = "message"
private const val TRANSFERRING_TYPE_DEAL_REQUEST = "deal_request"
private const val TRANSFERRING_TYPE_TASK = "task"

fun Route.veryNormalChatRoutes() {
    val connections = Collections.synchronizedMap<String, MutableSet<Connection>>(LinkedHashMap())
    val dealRequests = Collections.synchronizedMap<String, DealRequest>(LinkedHashMap())

    val json = Json {
        serializersModule = SerializersModule {
            polymorphic(DataTransfer::class) {
                subclass(DealRequest::class)
                subclass(Message::class)
                subclass(MessageDTO::class)
                subclass(Task::class)
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

                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val textFrame = frame.readText()

                        val incomingTextFrame = json.decodeFromString<IncomingTextFrame>(textFrame)
                        when(incomingTextFrame.type) {
                            TRANSFERRING_TYPE_MESSAGE -> {
                                try {
                                    val messageDto = incomingTextFrame.data as? MessageDTO ?: continue
                                    println(messageDto)
                                    val message = messageDto.toMessage(chatID = chatID, fromID = currentUserID)
                                    UserChatMessages.insertMessage(message) ?: continue
                                    connections[chatID]?.forEach { connection ->
                                        connection.session.send(json.encodeToString(IncomingTextFrame(type = TRANSFERRING_TYPE_MESSAGE, data = message)))
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'message'")
                                }
                            }
                            TRANSFERRING_TYPE_DEAL_REQUEST -> {
                                try {
                                    val dealRequest = incomingTextFrame.data as? DealRequest ?: continue
                                    dealRequests[chatID] = dealRequest

                                    // todo Как я понимаю чел может подключиться только к своей сессии. То есть она не
                                    //  общая на сервере. Значит, что при получении currentUserId, например, при
                                    //  перехвате dealRequest у меня есть полная уверенность в том, что он присвоится правильно.

                                    val newTask = Task(
                                        customerId = currentUserID,
                                        executorId = chat.sellerId,
                                        publicationId = chat.publicationId,
                                        chatId = chatID
                                    )

                                    connections[chatID]?.forEach { connection ->
                                        connection.session.send(Frame.Text(json.encodeToString(IncomingTextFrame(type = TRANSFERRING_TYPE_TASK, data = newTask))))
                                    }

//                                    connections[chatID]?.find { it.userID == chat.sellerId }?.session?.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_DEAL_REQUEST, data = dealRequest)))
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'deal request'")
                                }
                            }
                            TRANSFERRING_TYPE_TASK -> {
                                val task = incomingTextFrame.data as? Task ?: continue

                                Tasks.insertTask(task) ?: run {
                                    close()
                                    call.respond(status = HttpStatusCode.Conflict, message = "Database exception")
                                    return@webSocket
                                }

                                try {
                                    connections[chatID]?.forEach { connection ->
                                        connection.session.send(Frame.Text(json.encodeToString(task)))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
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

                try {
                    for (frame in incoming) {
                        frame as? Frame.Text ?: continue
                        val textFrame = frame.readText()

                        val incomingTextFrame = json.decodeFromString<IncomingTextFrame>(textFrame)

                        when(incomingTextFrame.type) {
                            TRANSFERRING_TYPE_MESSAGE -> {
                                try {
                                    val messageDto = incomingTextFrame.data as? MessageDTO ?: continue
                                    println(messageDto)
                                    val message = messageDto.toMessage(chatID = chat.id, fromID = currentUserID)
                                    UserChatMessages.insertMessage(message) ?: continue
                                    connections[chat.id]?.forEach { connection ->
                                        connection.session.send(json.encodeToString(IncomingTextFrame(type = TRANSFERRING_TYPE_MESSAGE, data = message)))
                                    }
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'message'")
                                }
                            }
                            TRANSFERRING_TYPE_DEAL_REQUEST -> {
                                try {
                                    val dealRequest = incomingTextFrame.data as? DealRequest ?: continue
                                    dealRequests[chat.id] = dealRequest

                                    val newTask = Task(
                                        customerId = currentUserID,
                                        executorId = chat.sellerId,
                                        publicationId = chat.publicationId,
                                        chatId = chat.id
                                    )

                                    connections[chat.id]?.forEach { connection ->
                                        connection.session.send(Frame.Text(json.encodeToString(IncomingTextFrame(type = TRANSFERRING_TYPE_TASK, data = newTask))))
                                    }

//                                    connections[chatID]?.find { it.userID == chat.sellerId }?.session?.send(json.encodeToString(IncomingTextFrame(type = INCOMING_TYPE_DEAL_REQUEST, data = dealRequest)))
                                } catch (e: Exception) {
                                    call.respond(status = HttpStatusCode.Conflict, "Wrong data structure for type 'deal request'")
                                }
                            }
                            TRANSFERRING_TYPE_TASK -> {
                                val task = incomingTextFrame.data as? Task ?: continue

                                Tasks.insertTask(task) ?: run {
                                    close()
                                    call.respond(status = HttpStatusCode.Conflict, message = "Database exception")
                                    return@webSocket
                                }

                                try {
                                    connections[chat.id]?.forEach { connection ->
                                        connection.session.send(Frame.Text(json.encodeToString(task)))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
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
    }
}