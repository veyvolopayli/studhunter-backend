package com.example.email

import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.SimpleEmail

class EmailService(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val ssl: Boolean,
    private val senderEmail: String
) {
    private val email = SimpleEmail().apply {
        hostName = host
        setSmtpPort(port)
        setAuthenticator(DefaultAuthenticator(username, password))
        isSSLOnConnect = ssl
        setFrom(senderEmail)
    }

    fun sendConfirmationEmail(recipientEmail: String, confirmationCode: Int) {
        email.apply {
            addTo(recipientEmail)
            subject = "Код для подтверждения вашей почты, введите его в приложении:"
            setMsg(confirmationCode.toString())
        }.send()
    }
}
