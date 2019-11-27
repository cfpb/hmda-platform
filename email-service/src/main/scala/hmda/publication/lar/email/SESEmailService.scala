package hmda.publication.lar.email

import com.amazonaws.services.simpleemail.AmazonSimpleEmailService
import com.amazonaws.services.simpleemail.model._
import monix.eval.Task
import cats.implicits._

class SESEmailService(emailClient: AmazonSimpleEmailService, fromAddress: String) extends EmailService {
  private val UTF8 = "UTF-8"

  // Make sure your email addresses are verified by AWS SES otherwise you will get errors
  override def send(email: Email): Task[Either[Throwable, Unit]] = {
    import email._
    Task.eval {
      emailClient.sendEmail {
        new SendEmailRequest()
          .withMessage(
            new Message()
              .withSubject(new Content().withCharset(UTF8).withData(subject))
              .withBody(new Body().withHtml(new Content().withData(plainTextContent)))
          )
          .withDestination(new Destination().withToAddresses(recipientAddress))
          .withSource(fromAddress)
      }
    }.attempt.map(_.void)
  }
}