package hmda.api.http.filing.submissions

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.directives.HmdaTimeDirectives
import akka.http.scaladsl.server.Directives._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.messages.filing.FilingCommands.{GetFiling, GetLatestSubmission}
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents.SubmissionCreated
import hmda.model.filing.Filing
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.model.institution.Institution
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.SubmissionPersistence
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import io.circe.generic.auto._
import hmda.api.http.filing.FilingResponseUtils._

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
          val institutionPersistence =
            sharding.entityRefFor(InstitutionPersistence.typeKey,
                                  s"${InstitutionPersistence.name}-$lei")

          val fInstitution
            : Future[Option[Institution]] = institutionPersistence ? (
              ref => GetInstitution(ref)
          )

          val filingPersistence =
            sharding.entityRefFor(FilingPersistence.typeKey,
                                  s"${FilingPersistence.name}-$lei-$period")

          val filingF: Future[Option[Filing]] = filingPersistence ? (ref =>
            GetFiling(ref))

          val latestSubmissionF
            : Future[Option[Submission]] = filingPersistence ? (ref =>
            GetLatestSubmission(ref))

          val fCheck = for {
            i <- fInstitution
            f <- filingF
            l <- latestSubmissionF
          } yield (i, f, l)

          onComplete(fCheck) {
            case Success(check) =>
              check match {
                case (None, _, _) =>
                  entityNotPresentResponse("institution", lei, uri)
                case (_, None, _) =>
                  entityNotPresentResponse("filing", s"$lei-$period", uri)
                case (_, _, maybeLatest) =>
                  maybeLatest match {
                    case None =>
                      val submissionId = SubmissionId(lei, period, 1)
                      createSubmission(uri, submissionId)
                    case Some(submission) =>
                      val submissionId =
                        SubmissionId(lei,
                                     period,
                                     submission.id.sequenceNumber + 1)
                      createSubmission(uri, submissionId)
                  }
              }

            case Failure(error) => failedResponse(uri, error)
          }
        }
    }

  //institutions/<lei>/filings/<period>/submissions/latest
  val submissionLatestPath: Route =
    path(
      "institutions" / Segment / "filings" / Segment / "submissions" / "latest") {
      (lei, period) =>
        timedGet { uri =>
          val filingPersistence =
            sharding.entityRefFor(FilingPersistence.typeKey,
                                  s"${FilingPersistence.name}-$lei-$period")

          val fLatest: Future[Option[Submission]] = filingPersistence ? (ref =>
            GetLatestSubmission(ref))

          onComplete(fLatest) {
            case Success(maybeLatest) =>
              maybeLatest match {
                case Some(latest) => complete(ToResponseMarshallable(latest))
                case None         => complete(HttpResponse(StatusCodes.NotFound))
              }
            case Failure(error) => failedResponse(uri, error)
          }
        }
    }

  def submissionRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          submissionCreatePath ~ submissionLatestPath
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
        complete(
          ToResponseMarshallable(StatusCodes.Created -> created.submission))
      case Failure(error) =>
        failedResponse(uri, error)
    }
  }

}
