package hmda.api.http.admin

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import hmda.model.institution.Institution
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.api.http.model.admin.InstitutionDeletedResponse
import hmda.persistence.institution.InstitutionPersistence
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.ConfigFactory
import hmda.auth.OAuth2Authorization
import hmda.messages.institution.InstitutionCommands.{
  CreateInstitution,
  DeleteInstitution,
  GetInstitution,
  InstitutionCommand,
  ModifyInstitution
}
import hmda.messages.institution.InstitutionEvents._
import hmda.util.http.FilingResponseUtils._
import hmda.api.http.PathMatchers._
import hmda.persistence.institution.InstitutionPersistence.selectInstitution

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait InstitutionAdminHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  val config        = ConfigFactory.load()
  val hmdaAdminRole = config.getString("keycloak.hmda.admin.role")

  def institutionWritePath(oAuth2Authorization: OAuth2Authorization): Route =
    oAuth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
      path("institutions") {
        entity(as[Institution]) { institution =>
          timedPost { uri =>
            sanatizeInstitutionIdentifiers(institution, true, uri, postInstitution)
          } ~
            timedPut { uri =>
              sanatizeInstitutionIdentifiers(institution, false, uri, putInstitution)
            } ~
            timedDelete { uri =>
              val institutionPersistence = InstitutionPersistence.selectInstitution(sharding, institution.LEI, institution.activityYear)
              val fDeleted: Future[InstitutionEvent] = institutionPersistence ? (
                ref => DeleteInstitution(institution.LEI, institution.activityYear, ref)
              )

              onComplete(fDeleted) {
                case Failure(error) =>
                  failedResponse(StatusCodes.InternalServerError, uri, error)

                case Success(InstitutionDeleted(lei, year)) =>
                  complete((StatusCodes.Accepted, InstitutionDeletedResponse(lei)))

                case Success(InstitutionNotExists(lei)) =>
                  complete((StatusCodes.NotFound, lei))

                case Success(_) =>
                  complete(StatusCodes.BadRequest)
              }
            }
        }
      }
    }

  private def postInstitution(institution: Institution, uri: Uri): Route = {
    val institutionPersistence = InstitutionPersistence.selectInstitution(sharding, institution.LEI, institution.activityYear)
    respondWithHeader(RawHeader("Cache-Control", "no-cache")) {
      val fInstitution: Future[Option[Institution]] = institutionPersistence ? GetInstitution
      onComplete(fInstitution) {
        case Failure(error) =>
          failedResponse(StatusCodes.InternalServerError, uri, error)

        case Success(Some(_)) =>
          entityAlreadyExists(StatusCodes.BadRequest, uri, s"Institution ${institution.LEI} already exists")

        case Success(None) =>
          val fCreated: Future[InstitutionCreated] = institutionPersistence ? (ref => CreateInstitution(institution, ref))
          onComplete(fCreated) {
            case Failure(error) =>
              failedResponse(StatusCodes.InternalServerError, uri, error)

            case Success(InstitutionCreated(i)) =>
              complete((StatusCodes.Created, i))
          }
      }
    }
  }

  private def putInstitution(institution: Institution, uri: Uri): Route = {
    val institutionPersistence = InstitutionPersistence.selectInstitution(sharding, institution.LEI, institution.activityYear)
    val originalInst: Future[Option[Institution]] = institutionPersistence ? GetInstitution
    val fModified = for {
      original <- originalInst
      m        <- modifyCall(institution, original, institutionPersistence)
    } yield m

    onComplete(fModified) {
      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)

      case Success(InstitutionModified(i)) =>
        complete((StatusCodes.Accepted, i))

      case Success(InstitutionNotExists(lei)) =>
        complete((StatusCodes.NotFound, lei))

      case Success(_) =>
        complete(StatusCodes.BadRequest)
    }
  }

  private def modifyCall(
    incomingInstitution: Institution,
    originalInstOpt: Option[Institution],
    institutionPersistence: EntityRef[InstitutionCommand]
  ): Future[InstitutionEvent] = {
    val originalFilerFlag = originalInstOpt.getOrElse(Institution.empty).hmdaFiler
    val iFilerFlagSet     = incomingInstitution.copy(hmdaFiler = originalFilerFlag)
    institutionPersistence ? (ref => ModifyInstitution(iFilerFlagSet, ref))
  }

  // GET institutions/<lei>/year/<year>
  // GET institutions/<lei>/year/<year>/quarter/<quarter>
  val institutionReadPath: Route =
    path("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      timedGet { uri =>
        getInstitution(lei, year, None, uri)
      } ~
        path("institutions" / Segment / "year" / IntNumber / "quarter" / Quarter) { (lei, year, quarter) =>
          timedGet { uri =>
            getInstitution(lei, year, Option(quarter), uri)
          }
        }
    }

  private def getInstitution(lei: String, year: Int, quarter: Option[String], uri: Uri): Route = {
    val institutionPersistence                    = selectInstitution(sharding, lei, year)
    val fInstitution: Future[Option[Institution]] = institutionPersistence ? GetInstitution
    onComplete(fInstitution) {
      case Failure(error) =>
        val errorResponse = ErrorResponse(500, error.getLocalizedMessage, uri.path)
        complete((StatusCodes.InternalServerError, errorResponse))

      case Success(Some(i)) =>
        complete(i)

      case Success(None) =>
        complete(StatusCodes.NotFound)
    }
  }

  private def validTaxIdFormat(taxIdOption: Option[String]): Boolean = {
    val taxId = taxIdOption.getOrElse("")
    val taxIdPattern ="[0-9]{2}\\-[0-9]{7}$".r
    taxId match {
      case taxIdPattern() => true
      case _ => false
    }
  }

  private def validLeiFormat(lei: String): Boolean = {
    val leiPattern = "[A-Z0-9]{20}$".r
    lei match {
      case leiPattern() => true
      case _ => false
    }
  }

  private def sanatizeInstitutionIdentifiers(institution: Institution, checkLei: Boolean, uri: Uri, route: (Institution, Uri) => Route): Route = {
    if (!validTaxIdFormat(institution.taxId)) {
      complete((StatusCodes.BadRequest, "Incorrect tax-id format"))
    } else if (checkLei && !validLeiFormat(institution.LEI)) {
      complete((StatusCodes.BadRequest, "Incorrect lei format"))
    } else route(institution, uri)
  }


  def institutionAdminRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionWritePath(oAuth2Authorization) ~ institutionReadPath
        }
      }
    }
}
