package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.pattern.ask
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.{ HmdaCustomDirectives, ValidationErrorConverter }
import hmda.api.model._
import hmda.api.protocol.processing.{ ApiErrorProtocol, EditResultsProtocol, InstitutionProtocol, SubmissionProtocol }
import hmda.model.fi.{ Submission, SubmissionId }
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.InstitutionPersistence.GetInstitution
import hmda.persistence.institutions.{ InstitutionPersistence, SubmissionPersistence }
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.persistence.processing.SubmissionManager
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }

import scala.util.{ Failure, Success }
import scala.concurrent.{ ExecutionContext, Future }
import javax.mail._
import javax.mail.internet.{ InternetAddress, MimeMessage }

import com.typesafe.config.ConfigFactory
import hmda.query.repository.KeyCloakRepository
/*
Questions
- Which email library to use (Javax)?
- Which email/port to send from (no-reply)?
- Send all emails using CC/BCC, or separate (separate)?
 */

trait SubmissionSignPaths
    extends InstitutionProtocol
    with SubmissionProtocol
    with ApiErrorProtocol
    with EditResultsProtocol
    with HmdaCustomDirectives
    with RequestVerificationUtils
    with ValidationErrorConverter
    with KeyCloakRepository {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>/submissions/<submissionId>/sign
  def submissionSignPath(supervisor: ActorRef, querySupervisor: ActorRef, institutionId: String)(implicit ec: ExecutionContext) =
    path("filings" / Segment / "submissions" / IntNumber / "sign") { (period, id) =>
      val submissionId = SubmissionId(institutionId, period, id)
      timedGet { uri =>
        completeVerified(supervisor, querySupervisor, institutionId, period, id, uri) {
          completeWithSubmissionReceipt(supervisor, submissionId, uri)
        }
      } ~
        timedPost { uri =>
          completeVerified(supervisor, querySupervisor, institutionId, period, id, uri) {
            entity(as[JsObject]) { json =>
              val verified = json.fields("signed").asInstanceOf[JsBoolean]
              verified match {
                case JsTrue =>
                  val fProcessingActor: Future[ActorRef] = (supervisor ? FindProcessingActor(SubmissionManager.name, submissionId)).mapTo[ActorRef]
                  val fSign = for {
                    actor <- fProcessingActor
                    s <- actor ? hmda.persistence.processing.ProcessingMessages.Signed
                  } yield s
                  onComplete(fSign) {
                    case Success(Some(_)) => completeWithSubmissionReceipt(supervisor, submissionId, uri)
                    case Success(_) =>
                      val errorResponse = ErrorResponse(400, "Illegal State: Submission must be Validated or ValidatedWithErrors to sign", uri.path)
                      complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
                    case Failure(error) => completeWithInternalError(uri, error)
                  }

                case JsFalse =>
                  val errorResponse = ErrorResponse(400, "Illegal Argument: signed = false", uri.path)
                  complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
              }

            }
          }
        }
    }

  private def completeWithSubmissionReceipt(supervisor: ActorRef, subId: SubmissionId, uri: Uri)(implicit ec: ExecutionContext) = {
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, subId.institutionId, subId.period)).mapTo[ActorRef]
    val fSubmission = for {
      a <- fSubmissionsActor
      s <- (a ? GetSubmissionById(subId)).mapTo[Submission]
    } yield s

    onComplete(fSubmission) {
      case Success(sub) =>
        if (sub.status.code == 10) {
          emailSignature(supervisor, sub)
        }
        complete(ToResponseMarshallable(Receipt(sub.end, sub.receipt, sub.status)))
      case Failure(error) => completeWithInternalError(uri, error)
    }
  }

  private def emailSignature(supervisor: ActorRef, submission: Submission)(implicit ec: ExecutionContext) = {
    val emails = findEmailsById(submission.id.institutionId)
    emails.map(emailSeq => {
      emailSeq.foreach(t => {
        val username = t._1 + " " + t._2
        sendMail(t._3, username, submission)
      })
    })
  }

  private def sendMail(address: String, username: String, submission: Submission) = {
    val config = ConfigFactory.load()
    val host = config.getString("hmda.mail.host")
    val port = config.getString("hmda.mail.port")

    val properties = System.getProperties
    properties.put("mail.smtp.host", host)
    properties.put("mail.smtp.port", port)

    val session = Session.getDefaultInstance(properties)
    val message = new MimeMessage(session)

    val text = s"$username,\nCongratulations, you've completed filing your HMDA data for filing period ${submission.id.period}.\n" +
      s"We received your filing on: ${submission.end}\n" +
      s"Your receipt is:${submission.receipt}"
    message.setFrom(new InternetAddress("no-reply@test.com"))
    message.setRecipients(Message.RecipientType.TO, address)
    message.setSubject("HMDA Filing Successful")
    message.setText(text)

    log.info(s"Sending message to $address with the message \n$text")
    Transport.send(message)
  }
}
