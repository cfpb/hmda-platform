package hmda.api.http.admin

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import hmda.model.institution.Institution
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.codec.institution.InstitutionCodec._
import hmda.api.http.model.ErrorResponse
import hmda.api.http.codec.ErrorResponseCodec._
import hmda.api.http.model.admin.InstitutionDeletedResponse
import hmda.persistence.institution.InstitutionPersistence
import io.circe.generic.auto._
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.ConfigFactory
import hmda.auth.OAuth2Authorization
import hmda.messages.institution.InstitutionCommands.{
  CreateInstitution,
  DeleteInstitution,
  GetInstitution,
  ModifyInstitution
}
import hmda.messages.institution.InstitutionEvents._
import hmda.util.http.FilingResponseUtils._
import hmda.utils.YearUtils._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait InstitutionAdminHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  val config = ConfigFactory.load()
  val hmdaAdminRole = config.getString("keycloak.hmda.admin.role")

  def institutionWritePath(oAuth2Authorization: OAuth2Authorization) =
    oAuth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
      path("institutions") {
        entity(as[Institution]) { institution =>
          val institutionPersistence = {
            if (institution.activityYear == 2018) {
              sharding.entityRefFor(
                InstitutionPersistence.typeKey,
                s"${InstitutionPersistence.name}-${institution.LEI}")
            } else {
              sharding.entityRefFor(
                InstitutionPersistence.typeKey,
                s"${InstitutionPersistence.name}-${institution.LEI}-${institution.activityYear}")
            }
          }

          timedPost { uri =>
            respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
              val fInstitution
                : Future[Option[Institution]] = institutionPersistence ? (
                  ref => GetInstitution(ref)
              )
              onComplete(fInstitution) {
                case Success(Some(_)) =>
                  entityAlreadyExists(
                    StatusCodes.BadRequest,
                    uri,
                    s"Institution ${institution.LEI} already exists")
                case Success(None) =>
                  val fCreated
                    : Future[InstitutionCreated] = institutionPersistence ? (
                      ref => CreateInstitution(institution, ref))
                  onComplete(fCreated) {
                    case Success(InstitutionCreated(i)) =>
                      complete(ToResponseMarshallable(StatusCodes.Created -> i))
                    case Failure(error) =>
                      failedResponse(StatusCodes.InternalServerError,
                                     uri,
                                     error)
                  }
                case Failure(error) =>
                  failedResponse(StatusCodes.InternalServerError, uri, error)
              }
            }
          } ~
            timedPut { uri =>
              val originalInst
                : Future[Option[Institution]] = institutionPersistence ? (
                  ref => GetInstitution(ref)
              )

              def modifyCall(originalInstOpt: Option[Institution])
                : Future[InstitutionEvent] = {
                val originalFilerFlag =
                  originalInstOpt.getOrElse(Institution.empty).hmdaFiler
                val iFilerFlagSet =
                  institution.copy(hmdaFiler = originalFilerFlag)
                institutionPersistence ? (
                    ref => ModifyInstitution(iFilerFlagSet, ref)
                )
              }

              val fModified = for {
                i <- originalInst
                m <- modifyCall(i)
              } yield m

              onComplete(fModified) {
                case Success(InstitutionModified(i)) =>
                  complete(ToResponseMarshallable(StatusCodes.Accepted -> i))
                case Success(InstitutionNotExists(lei)) =>
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> lei))
                case Success(_) =>
                  complete(ToResponseMarshallable(
                    HttpResponse(StatusCodes.BadRequest)))
                case Failure(error) =>
                  failedResponse(StatusCodes.InternalServerError, uri, error)
              }
            } ~
            timedDelete { uri =>
              val fDeleted
                : Future[InstitutionEvent] = institutionPersistence ? (
                  ref =>
                    DeleteInstitution(institution.LEI,
                                      institution.activityYear,
                                      ref)
              )

              onComplete(fDeleted) {
                case Success(InstitutionDeleted(lei, year)) =>
                  complete(ToResponseMarshallable(
                    StatusCodes.Accepted -> InstitutionDeletedResponse(lei)))
                case Success(InstitutionNotExists(lei)) =>
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> lei))
                case Success(_) =>
                  complete(ToResponseMarshallable(
                    HttpResponse(StatusCodes.BadRequest)))
                case Failure(error) =>
                  failedResponse(StatusCodes.InternalServerError, uri, error)
              }
            }
        }
      }
    }

  val institutionReadPath =
    path("institutions" / Segment / "year" / Segment) { (lei, period) =>
      val institutionPersistence = {
        if (period == "2018") {
          sharding.entityRefFor(InstitutionPersistence.typeKey,
                                s"${InstitutionPersistence.name}-$lei")
        } else {
          sharding.entityRefFor(InstitutionPersistence.typeKey,
                                s"${InstitutionPersistence.name}-$lei-$period")
        }
      }

      timedGet { uri =>
        if (!isValidYear(period.toInt)) {
          complete(
            ErrorResponse(500, s"Invalid Year Provided: $period", uri.path))
        } else {
          val fInstitution
            : Future[Option[Institution]] = institutionPersistence ? (
              ref => GetInstitution(ref)
          )

          onComplete(fInstitution) {
            case Success(Some(i)) =>
              complete(ToResponseMarshallable(i))
            case Success(None) =>
              complete(
                ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
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

  def institutionAdminRoutes(
      oAuth2Authorization: OAuth2Authorization): Route = {
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionWritePath(oAuth2Authorization) ~ institutionReadPath
        }
      }
    }
  }

}
