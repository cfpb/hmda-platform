package hmda.institution.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.institution.query.{InstitutionComponent, InstitutionEntity}
import hmda.query.DbConfiguration._
import hmda.api.http.codec.institution.InstitutionCodec._
import hmda.institution.api.http.model.InstitutionsResponse
import io.circe.generic.auto._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait InstitutionQueryHttpApi
    extends HmdaTimeDirectives
    with InstitutionComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  implicit val institutionRepository = new InstitutionRepository(dbConfig)
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(
    dbConfig)

  val institutionByIdPath =
    path("institutions" / Segment) { lei =>
      timedGet { uri =>
        val fInstitution = institutionRepository.findById(lei)
        val fEmails = institutionEmailsRepository.findByLei(lei)
        val f = for {
          institution <- fInstitution
          emails <- fEmails
        } yield (institution, emails.map(_.emailDomain))

        onComplete(f) {
          case Success((institution, emails)) =>
            if (institution.isEmpty) {
              complete(
                ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
            } else {
              complete(
                ToResponseMarshallable(InstitutionConverter
                  .convert(institution.getOrElse(InstitutionEntity()), emails)))
            }
          case Failure(error) =>
            val errorResponse =
              ErrorResponse(500, error.getLocalizedMessage, uri.path)
            complete(
              ToResponseMarshallable(
                StatusCodes.InternalServerError -> errorResponse))
        }
      }
    }

  val institutionByDomainPath =
    path("institutions") {
      timedGet { uri =>
        parameter('domain.as[String]) { domain =>
          val f = findByEmail(domain)
          onComplete(f) {
            case Success(institutions) =>
              if (institutions.isEmpty) {
                complete(
                  ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
              } else {
                complete(
                  ToResponseMarshallable(InstitutionsResponse(institutions)))
              }
            case Failure(error) =>
              val errorResponse =
                ErrorResponse(500, error.getLocalizedMessage, uri.path)
              complete(
                ToResponseMarshallable(
                  StatusCodes.InternalServerError -> errorResponse))
          }
        }
      }
    }

  def institutionPublicRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionByIdPath ~ institutionByDomainPath
        }
      }
    }

}
