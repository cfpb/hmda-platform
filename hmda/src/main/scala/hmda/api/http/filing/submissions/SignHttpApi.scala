package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.{encodeResponse, handleRejections}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import io.circe.generic.auto._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{
  cors,
  corsRejectionHandler
}
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.filing.submissions.{EditsSign, SignedResponse}
import hmda.auth.OAuth2Authorization
import hmda.messages.submission.SubmissionCommands.GetSubmission
import hmda.messages.submission.SubmissionProcessingCommands.SignSubmission
import hmda.messages.submission.SubmissionProcessingEvents.{
  SubmissionNotReadyToBeSigned,
  SubmissionSigned,
  SubmissionSignedEvent
}
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.persistence.submission.{HmdaValidationError, SubmissionPersistence}
import hmda.util.http.FilingResponseUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SignHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<lei>/filings/<period>/submissions/<submissionId>/sign
  def signPath(oAuth2Authorization: OAuth2Authorization): Route =
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / IntNumber / "sign") {
      (lei, period, seqNr) =>
        oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
          val submissionId = SubmissionId(lei, period, seqNr)
          timedGet { uri =>
            val submissionPersistence =
              sharding.entityRefFor(
                SubmissionPersistence.typeKey,
                s"${SubmissionPersistence.name}-$submissionId")

            val fSubmission
              : Future[Option[Submission]] = submissionPersistence ? (ref =>
              GetSubmission(ref))

            val fDetails = for {
              s <- fSubmission.map(s => s.getOrElse(Submission()))
            } yield s

            onComplete(fDetails) {
              case Success(submission) =>
                if (submission.isEmpty) {
                  submissionNotAvailable(submissionId, uri)
                } else {
                  val signed = SubmissionSigned(submissionId,
                                                submission.end,
                                                submission.status)
                  complete(ToResponseMarshallable(signed))
                }

              case Failure(e) =>
                failedResponse(StatusCodes.InternalServerError, uri, e)
            }
          } ~
            timedPost { uri =>
              entity(as[EditsSign]) { editsSign =>
                if (editsSign.signed) {
                  val submissionSignPersistence = sharding
                    .entityRefFor(HmdaValidationError.typeKey,
                                  s"${HmdaValidationError.name}-$submissionId")

                  val fSigned
                    : Future[SubmissionSignedEvent] = submissionSignPersistence ? (
                      ref => SignSubmission(submissionId, ref))

                  onComplete(fSigned) {
                    case Success(submissionSignedEvent) =>
                      submissionSignedEvent match {
                        case signed @ SubmissionSigned(_, _, status) =>
                          val signedResponse = SignedResponse(signed.timestamp,
                                                              signed.receipt,
                                                              status)
                          complete(ToResponseMarshallable(signedResponse))
                        case SubmissionNotReadyToBeSigned(id) =>
                          badRequest(
                            id,
                            uri,
                            s"Submission $id is not ready to be signed")
                      }
                    case Failure(e) =>
                      failedResponse(StatusCodes.InternalServerError, uri, e)
                  }
                } else {
                  badRequest(submissionId,
                             uri,
                             "Illegal argument: signed = false")
                }
              }
            }
        }
    }

  def signRoutes(oAuth2Authorization: OAuth2Authorization): Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          signPath(oAuth2Authorization)
        }
      }

    }
  }

}
