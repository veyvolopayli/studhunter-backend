package com.studhunter.api.auth.routes

import com.studhunter.api.users.model.User
import com.studhunter.api.auth.requests.SignInRequest
import com.studhunter.api.auth.requests.SignUpRequest
import com.studhunter.api.auth.responses.AuthResponse
import com.studhunter.api.email.service.EmailService
import com.studhunter.api.users.tables.UserData
import com.studhunter.api.users.tables.Users
import com.studhunter.security.hashing.HashingService
import com.studhunter.security.hashing.SaltedHash
import com.studhunter.security.token.TokenClaim
import com.studhunter.security.token.TokenConfig
import com.studhunter.security.token.TokenService
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.exceptions.ExposedSQLException

fun Route.signUp(
    hashingService: HashingService,
    emailService: EmailService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {

    post("signup") {

        val request = call.receiveNullable<SignUpRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 6
        if (areFieldsBlank || isPwTooShort) {
            call.respond(status = HttpStatusCode.Conflict, message = "Username or password is too short or empty")
            return@post
        }

        val user = Users.getUserByUsername(request.username)

        if (user != null) {
            call.respond(status = HttpStatusCode.Conflict, message = "User already exists")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)

        val newUser = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            email = request.email,
            name = request.name,
            surname = request.surname,
            university = request.university
        )

        try {
            Users.insertUser(newUser)
        } catch (e: ExposedSQLException) {
            call.respond(status = HttpStatusCode.Conflict, message = "User already exists")
        }

        val userDataModel = com.studhunter.api.users.model.UserDataModel(userId = newUser.id)

        UserData.insertUserData(userDataModel) ?: kotlin.run {
            call.respond(status = HttpStatusCode.Conflict, message = "Fail to insert user data")
            return@post
        }

        emailService.sendConfirmationEmail(newUser.email, newUser.username, userDataModel.confirmationCode)

        val token = tokenService.generate(config = tokenConfig, TokenClaim(name = "userId", value = newUser.id))

        call.respond(status = HttpStatusCode.OK, message = AuthResponse(token = token))

        /*val areFieldsBlank = request.username.isBlank() || request.password.isBlank()
        val isPwTooShort = request.password.length < 6
        if (areFieldsBlank || isPwTooShort) {
            call.respond(status = HttpStatusCode.Conflict, message = "Password is too short or empty")
            return@post
        }

        val userExist = try {
            userDataSource.getUserByUsername(request.username) != null
        } catch (e: Exception) {
            false
        }

        if (userExist) {
            call.respond(status = HttpStatusCode.Conflict, message = "User already exists")
            return@post
        }

        val saltedHash = hashingService.generateSaltedHash(request.password)
        val user = User(
            username = request.username,
            password = saltedHash.hash,
            salt = saltedHash.salt,
            email = request.email,
            fullName = "${request.name} ${request.surname}"
        )
        val wasAcknowledged = userDataSource.insertUser(user)
//        val wasAcknowledged = userDataSource.insertUser(user)
        if (!wasAcknowledged) {
            call.respond(HttpStatusCode.Conflict)
            return@post
        }

        call.respond(HttpStatusCode.OK)*/
    }
}

fun Route.signIn(
    hashingService: HashingService,
    tokenService: TokenService,
    tokenConfig: TokenConfig
) {
    post("signin") {

        val request = call.receiveNullable<SignInRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = Users.getUserDetailed(request.username) ?: kotlin.run {
            call.respond(status = HttpStatusCode.BadRequest, message = "User does not exist")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(config = tokenConfig, TokenClaim(name = "userId", value = user.id))

        call.respond(status = HttpStatusCode.OK, message = AuthResponse(token = token))

        /*val request = call.receiveNullable<SignInRequest>() ?: kotlin.run {
            call.respond(HttpStatusCode.BadRequest)
            return@post
        }

        val user = userDataSource.getUserByUsername(request.username)

        if (user == null) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val isValidPassword = hashingService.verify(
            value = request.password,
            saltedHash = SaltedHash(
                hash = user.password,
                salt = user.salt
            )
        )

        if (!isValidPassword) {
            call.respond(HttpStatusCode.Conflict, "Incorrect username or password")
            return@post
        }

        val token = tokenService.generate(config = tokenConfig, TokenClaim(name = "userId", value = user.id))

        call.respond(status = HttpStatusCode.OK, message = AuthResponse(token = token))*/
    }
}

fun Route.authenticate() {
    authenticate {
        get("authenticate") {
            call.respond(HttpStatusCode.OK)
        }
    }
}

fun Route.getUserId() {
    authenticate {
        get("userid") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "$userId")
        }
    }
}

fun Route.getSecretInfo() {
    authenticate {
        get("secret") {
            val principal = call.principal<JWTPrincipal>()
            val userId = principal?.getClaim("userId", String::class)
            call.respond(HttpStatusCode.OK, "Your userId is $userId")
        }
    }
}