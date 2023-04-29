package com.example.email

import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.HtmlEmail
import org.apache.commons.mail.SimpleEmail

class EmailService(
    private val host: String,
    private val port: Int,
    private val username: String,
    private val password: String,
    private val ssl: Boolean,
    private val senderEmail: String
) {
    private val email = HtmlEmail().apply {
        hostName = host
        setSmtpPort(port)
        setAuthenticator(DefaultAuthenticator(username, password))
        isSSLOnConnect = ssl
        setFrom(senderEmail)
    }

    fun sendConfirmationEmail(recipientEmail: String, username: String, confirmationCode: Int) {
        email.apply {
            addTo(recipientEmail)
            subject = "<b>Код для подтверждения StudHunter</b>"
            setHtmlMsg("<p>Уважаемый $username, ваш код подтверждения для регистрации в приложении StudHunter:</p><h1><b>$confirmationCode</b></h1>")
            setCharset("UTF-8")
        }.send()
    }
}
