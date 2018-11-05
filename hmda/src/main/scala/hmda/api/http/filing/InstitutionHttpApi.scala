package hmda.api.http.filing

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.codec.filing.FilingStatusCodec._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.filing.FilingResponseUtils.{
  entityNotPresentResponse,
  failedResponse
}
import hmda.messages.filing.FilingCommands.GetFilingDetails
import hmda.messages.institution.InstitutionCommands.{
  GetInstitution,
  GetInstitutionDetails
}
import hmda.model.filing.FilingDetails
import hmda.model.institution.Institution
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

trait InstitutionHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  //institutions/<lei>
  val institutionReadPath =
    path("institutions" / Segment) { (lei) =>
      val institutionPersistence =
        sharding.entityRefFor(InstitutionPersistence.typeKey,
                              s"${InstitutionPersistence.name}-$lei")

      val filingPersistence =
        sharding.entityRefFor(FilingPersistence.typeKey,
                              s"${FilingPersistence.name}-$lei")

      val fInstitution: Future[Option[Institution]] = institutionPersistence ? (
          ref => GetInstitution(ref)
      )

      val fDetails: Future[Option[FilingDetails]] = filingPersistence ? (ref =>
        GetFilingDetails(ref))

      val iDetails
        : Future[Option[InstitutionDetail]] = institutionPersistence ? (ref =>
        GetInstitutionDetails(ref))

      val filingDetailsF = for {
        i <- iDetails
      } yield (i)

      timedGet { uri =>
        onComplete(filingDetailsF) {
          case Success((Some(institutionDetails))) =>
            complete(ToResponseMarshallable(institutionDetails))
          case Success((None)) =>
            entityNotPresentResponse("institution", lei, uri)
          case Failure(error) =>
            failedResponse(uri, error)
        }

      }

    }

  def institutionRoutes: Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionReadPath
        }
      }
    }
  }
}
