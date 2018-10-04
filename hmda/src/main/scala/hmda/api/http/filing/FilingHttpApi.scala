package hmda.api.http.filing

import java.time.Instant

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.messages.filing.FilingCommands.{CreateFiling, GetFilingDetails}
import hmda.model.filing.{Filing, FilingDetails, InProgress}
import hmda.persistence.filing.FilingPersistence
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import io.circe.generic.auto._
import hmda.api.http.codec.filing.FilingCodec._
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FilingHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<institutionId>/filings/<period>
  val filingReadPath =
    path("institutions" / Segment / "filings" / Segment) { (lei, period) =>
      val institutionPersistence =
        sharding.entityRefFor(InstitutionPersistence.typeKey,
                              s"${InstitutionPersistence.name}-$lei")

      val filingPersistence =
        sharding.entityRefFor(FilingPersistence.typeKey,
                              s"${FilingPersistence.name}-$lei-$period")

      val fInstitution: Future[Option[Institution]] = institutionPersistence ? (
          ref => GetInstitution(ref)
      )

      val fDetails: Future[Option[FilingDetails]] = filingPersistence ? (ref =>
        GetFilingDetails(ref))

      val filingDetailsF = for {
        i <- fInstitution
        d <- fDetails
      } yield (i, d)

      timedPost { uri =>
        onComplete(filingDetailsF) {
          case Success((None, _)) =>
            institutionNotFoundResponse(lei, uri)
          case Success((Some(_), Some(_))) =>
            val errorResponse =
              ErrorResponse(400,
                            s"Filing $lei-$period already exists",
                            uri.path)
            complete(
              ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
          case Success((Some(_), None)) =>
            val now = Instant.now().toEpochMilli
            val filing = Filing(
              period,
              lei,
              InProgress,
              true,
              now,
              0L
            )
            val fFiling: Future[FilingCreated] = filingPersistence ? (ref =>
              CreateFiling(filing, ref))
            onComplete(fFiling) {
              case Success(created) =>
                val filingDetails = FilingDetails(created.filing, Nil)
                complete(ToResponseMarshallable(filingDetails))
              case Failure(error) =>
                failedResponse(uri, error)
            }
          case Failure(error) =>
            failedResponse(uri, error)
        }
      } ~
        timedGet { uri =>
          onComplete(filingDetailsF) {
            case Success((Some(_), Some(filingDetails))) =>
              complete(ToResponseMarshallable(filingDetails))
            case Success((None, _)) =>
              institutionNotFoundResponse(lei, uri)
            case Success((Some(i), None)) =>
              val errorResponse = ErrorResponse(
                404,
                s"Filing for institution: ${i.LEI} and period: $period does not exist",
                uri.path)
              complete(
                ToResponseMarshallable(StatusCodes.NotFound -> errorResponse)
              )
            case Failure(error) =>
              failedResponse(uri, error)
          }
        }
    }

  private def failedResponse(uri: Uri, error: Throwable) = {
    val errorResponse =
      ErrorResponse(500, error.getLocalizedMessage, uri.path)
    complete(
      ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
  }

  private def institutionNotFoundResponse(lei: String, uri: Uri) = {
    val errorResponse =
      ErrorResponse(400, s"institution with LEI: $lei does not exist", uri.path)
    complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
  }

  def filingRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          filingReadPath
        }
      }
    }
  }
}
