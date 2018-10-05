package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.messages.filing.FilingCommands.GetLatestSubmission
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents.SubmissionCreated
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.submission.SubmissionPersistence
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait SubmissionHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<lei>/filings/<period>/submissions
  val submissionCreatePath: Route =
    path("institutions" / Segment / "filings" / Segment / "submissions") {
      (lei, period) =>
        timedPost { uri =>
          val filingPersistence =
            sharding.entityRefFor(FilingPersistence.typeKey,
                                  s"${FilingPersistence.name}-$lei-$period")

          val latestSubmissionF
            : Future[Option[Submission]] = filingPersistence ? (ref =>
            GetLatestSubmission(ref))

          onComplete(latestSubmissionF) {
            case Failure(error) =>
              failedResponse(uri, error)
            case Success(maybeLatest) =>
              maybeLatest match {
                case None =>
                  val submissionId = SubmissionId(lei, period, 0)
                  createSubmission(uri, submissionId)
                case Some(submission) =>
                  val submissionId =
                    SubmissionId(lei, period, submission.id.sequenceNumber + 1)
                  createSubmission(uri, submissionId)
              }
          }

        }
    }

//  val submissionLatestPath: Route = ???
//
//  val submissionByIdPath: Route = ???

  def submissionRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          submissionCreatePath
        }
      }
    }
  }

  private def createSubmission(uri: Uri, submissionId: SubmissionId): Route = {
    val submissionPersistence = sharding.entityRefFor(
      SubmissionPersistence.typeKey,
      s"${SubmissionPersistence.name}-${submissionId.toString}")

    val createdF: Future[SubmissionCreated] = submissionPersistence ? (ref =>
      CreateSubmission(submissionId, ref))

    onComplete(createdF) {
      case Success(created) =>
        complete(ToResponseMarshallable(created.submission))
      case Failure(error) =>
        failedResponse(uri, error)
    }
  }

  private def failedResponse(uri: Uri, error: Throwable) = {
    val errorResponse =
      ErrorResponse(500, error.getLocalizedMessage, uri.path)
    complete(
      ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
  }

}
