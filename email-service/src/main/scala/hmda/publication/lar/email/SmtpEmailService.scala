package hmda.publication.lar.email

import monix.eval.Task
import jakarta.mail._
import jakarta.mail.internet._
import cats.implicits._
import hmda.publication.lar.config.Settings

import java.util.Properties

class SmtpEmailService(config: Settings) extends EmailService {
  override def send(email: Email): Task[Either[Throwable, Unit]] = {
    import email._
    Task.eval {
      val props = new Properties() {
        {
          put("mail.smtp.host", config.client.smtpHost)
          put("mail.smtp.port", config.client.smtpPort)
          put("mail.smtp.auth", "true")
          put("mail.smtp.starttls.enable", "true")
        }
      }

      val passwordAuthenticator = new Authenticator() {
        override def getPasswordAuthentication = new PasswordAuthentication(config.client.smtpUsername, config.client.smtpPassword)
      }

      val session = Session.getInstance(props, passwordAuthenticator)
      val msg = new MimeMessage(session)
      msg.setRecipients(Message.RecipientType.TO, recipientAddress)
      msg.setFrom("no-reply@cfpb.gov")
      msg.setSubject(subject)
      msg.setText(plainTextContent)

      Transport.send(msg)

    }.attempt.map(_.void)
  }
}
