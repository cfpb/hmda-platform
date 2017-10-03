package hmda.api.http.institutions.submissions

import java.time.{ ZoneOffset, ZonedDateTime }

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
import hmda.persistence.HmdaSupervisor.{ FindProcessingActor, FindSubmissions }
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.processing.SubmissionManager
import spray.json.{ JsBoolean, JsFalse, JsObject, JsTrue }

import scala.util.{ Failure, Success }
import scala.concurrent.{ ExecutionContext, Future }
import javax.mail._
import javax.mail.internet.{ InternetAddress, MimeMessage }

import com.typesafe.config.ConfigFactory
import hmda.model.institution.Institution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.repository.KeyCloakRepository
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.institutions.InstitutionView.GetInstitutionById

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
          completeWithSubmissionReceipt(supervisor, submissionId, uri, signed = false)
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
                    case Success(Some(_)) => completeWithSubmissionReceipt(supervisor, submissionId, uri, signed = true)
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

  private def completeWithSubmissionReceipt(supervisor: ActorRef, subId: SubmissionId, uri: Uri, signed: Boolean)(implicit ec: ExecutionContext) = {
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, subId.institutionId, subId.period)).mapTo[ActorRef]
    val fSubmission = for {
      a <- fSubmissionsActor
      s <- (a ? GetSubmissionById(subId)).mapTo[Submission]
    } yield s

    onComplete(fSubmission) {
      case Success(sub) =>
        if (signed) {
          emailSignature(supervisor, sub)
        }
        complete(ToResponseMarshallable(Receipt(sub.end, sub.receipt, sub.status)))
      case Failure(error) => completeWithInternalError(uri, error)
    }
  }

  private def emailSignature(supervisor: ActorRef, submission: Submission)(implicit ec: ExecutionContext) = {
    val emails = findEmailsById(submission.id.institutionId)
    val querySupervisor = system.actorSelection("/user/query-supervisor/singleton")
    val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    val fName = for {
      a <- fInstitutionsActor
      i <- (a ? GetInstitutionById(submission.id.institutionId)).mapTo[Institution]
      e <- emails
    } yield (i.respondent.name, e)

    fName.onComplete({
      case Success((instName, emailSeq)) =>
        emailSeq.foreach(t => {
          val username = t._1 + " " + t._2
          sendMail(t._3, username, submission, instName)
        })
      case Failure(error) => log.error(error, s"An error has occured retrieving the institution name for ID ${submission.id.institutionId}")
    })
  }

  private def sendMail(address: String, username: String, submission: Submission, instName: String) = {
    val config = ConfigFactory.load()
    val host = config.getString("hmda.mail.host")
    val port = config.getString("hmda.mail.port")
    val senderAddress = config.getString("hmda.mail.senderAddress")

    val properties = System.getProperties
    properties.put("mail.smtp.host", host)
    properties.put("mail.smtp.port", port)

    val session = Session.getDefaultInstance(properties)
    val message = new MimeMessage(session)

    val date = getFormattedDate

    val text = s"$username,\n\nCongratulations, you've completed filing your HMDA data for $instName for filing period ${submission.id.period}.\n" +
      s"We received your filing on: $date\n" +
      s"Your receipt is: ${submission.receipt}"
    message.setFrom(new InternetAddress(senderAddress))
    message.setRecipients(Message.RecipientType.TO, address)
    message.setSubject("HMDA Filing Successful")
    message.setText(text)

    log.info(s"Sending message to $address with the message \n$text")
    Transport.send(message)
  }

  private def getFormattedDate: String = {
    val offset = ZoneOffset.ofHours(-5)
    val zonedTime = ZonedDateTime.now(offset)

    val day = zonedTime.getDayOfMonth
    val month = zonedTime.getMonthValue
    val year = zonedTime.getYear

    s"$month/$day/$year"
  }
}
