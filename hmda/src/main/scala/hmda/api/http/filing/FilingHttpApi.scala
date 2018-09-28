package hmda.api.http.filing

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.messages.filing.FilingCommands.GetFilingDetails
import hmda.model.filing.FilingDetails
import hmda.persistence.filing.FilingPersistence
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import io.circe.generic.auto._

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
      val filingPersistence =
        sharding.entityRefFor(FilingPersistence.typeKey,
                              s"${FilingPersistence.name}-$lei-$period")

      timedGet { uri =>
        val fDetails: Future[FilingDetails] = filingPersistence ? (ref =>
          GetFilingDetails(ref))

        onComplete(fDetails) {
          case Success(filingDetails) =>
            complete(ToResponseMarshallable(filingDetails))
          case Failure(error) =>
            val errorResponse =
              ErrorResponse(500, error.getLocalizedMessage, uri.path)
            complete(
              ToResponseMarshallable(
                StatusCodes.InternalServerError -> errorResponse))
        }
      }
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
