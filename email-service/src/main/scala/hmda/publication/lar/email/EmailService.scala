package hmda.publication.lar.email

import monix.eval.Task

case class Email(recipientAddress: String, subject: String, plainTextContent: String)

trait EmailService {
  def send(email: Email): Task[Either[Throwable, Unit]]
}
