package com.studhunter.api.email.service

import org.apache.commons.mail.DefaultAuthenticator
import org.apache.commons.mail.EmailException
import org.apache.commons.mail.HtmlEmail

class EmailService(
    private val host: String = "smtp.yandex.ru",
    private val port: Int = 465,
    private val username: String = "studhunterapp@yandex.ru",
    private val password: String = "yxwgeczvkdweitya",
    private val ssl: Boolean = true,
    private val senderEmail: String
) {

    private fun createHtmlEmail(): HtmlEmail {
        return HtmlEmail().apply {
            hostName = host
            setSmtpPort(port)
            setAuthenticator(DefaultAuthenticator(username, password))
            isSSLOnConnect = ssl
            setFrom(senderEmail)
        }
    }

    fun sendConfirmationEmail(recipientEmail: String, username: String, confirmationCode: Int): Boolean {
        val email = createHtmlEmail().apply {
            addTo(recipientEmail)
            subject = "Код для подтверждения StudHunter"
            setHtmlMsg("<p>Уважаемый $username, ваш код подтверждения для регистрации в приложении StudHunter:</p><h1><b>$confirmationCode</b></h1>")
            setCharset("UTF-8")
        }

        return try {
            email.send()
            true
        } catch (e: EmailException) {
            e.printStackTrace()
            false
        }
    }
}
