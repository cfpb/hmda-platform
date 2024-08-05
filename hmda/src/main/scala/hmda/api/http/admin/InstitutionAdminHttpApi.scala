package hmda.api.http.admin

import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.model.ErrorResponse
import hmda.api.http.model.admin.InstitutionDeletedResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.institution.InstitutionCommands._
import hmda.messages.institution.InstitutionEvents._
import hmda.model.institution.{ Agency, Institution }
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.institution.InstitutionPersistence.selectInstitution
import hmda.util.http.FilingResponseUtils._
import hmda.api.http.EmailUtils._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

object InstitutionAdminHttpApi {
  def create(config: Config, sharding: ClusterSharding)(implicit ec: ExecutionContext, t: Timeout): OAuth2Authorization => Route =
    new InstitutionAdminHttpApi(config, sharding)(ec, t).institutionAdminRoutes _
}

private class InstitutionAdminHttpApi(config: Config, sharding: ClusterSharding)(implicit ec: ExecutionContext, t: Timeout) {
  val hmdaAdminRole   = config.getString("keycloak.hmda.admin.role")
  val checkLEI        = true
  val checkAgencyCode = false
  val rc: RequestReplicationClient = RequestReplicationClient.create(config, "hmda.institutions.edits.replication-address")
  def institutionAdminRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          rc.withRequestReplication {
            institutionWritePath(oAuth2Authorization)
          } ~ institutionReadPath(oAuth2Authorization)
        }
      }
    }

  private def institutionWritePath(oAuth2Authorization: OAuth2Authorization): Route =
    path("institutions") {
      oAuth2Authorization.authorizeTokenWithRole(hmdaAdminRole) { _ =>
        entity(as[Institution]) { institution =>
          (extractUri & post) { uri =>
            sanatizeInstitutionIdentifiers(institution, checkLEI, checkAgencyCode, uri, postInstitution)
          } ~
            (extractUri & put){uri => 
              sanatizeInstitutionIdentifiers(institution, checkLEI, checkAgencyCode, uri, putInstitution)
          } ~
            (extractUri & delete) { uri =>
              val institutionPersistence = InstitutionPersistence.selectInstitution(sharding, institution.LEI, institution.activityYear)
              val fDeleted: Future[InstitutionEvent] =
                institutionPersistence ? (ref => DeleteInstitution(institution.LEI, institution.activityYear, ref))

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

  // GET institutions/<lei>/year/<year>
  // GET institutions/<lei>/year/<year>/quarter/<quarter>
  private def institutionReadPath (oAuth2Authorization: OAuth2Authorization): Route = {
    path("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      oAuth2Authorization.authorizeTokenWithLeiOrRole(lei, hmdaAdminRole) { _ =>
        (extractUri & get) { uri =>
          getInstitution(lei, year, None, uri)
        }
      }
    } ~ path("institutions" / Segment / "year" / IntNumber / "quarter" / Quarter) { (lei, year, quarter) =>
      oAuth2Authorization.authorizeTokenWithLeiOrRole(lei, hmdaAdminRole) { _ =>
        (extractUri & get)(uri => getInstitution(lei, year, Option(quarter), uri))
      }
    } ~
      path("institutions" / Segment) { lei =>
        oAuth2Authorization.authorizeTokenWithLeiOrRole(lei, hmdaAdminRole) { _ =>
          (extractUri & get) { uri =>
            getAllInstitutions(lei, uri)
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
          val fCreated: Future[InstitutionEvent] = institutionPersistence ? (ref => CreateInstitution(institution, ref))
          onComplete(fCreated) {
            case Failure(error) =>
              failedResponse(StatusCodes.InternalServerError, uri, error)

            case Success(InstitutionCreated(i)) =>
              complete((StatusCodes.Created, i))

            case Success(InstitutionWithLou(i)) =>
              entityWithLou(StatusCodes.PreconditionFailed, uri, s"Institution LEI is an LOU: ${institution.LEI}")
          }
      }
    }
  }

  private def putInstitution(institution: Institution, uri: Uri): Route = {
    val institutionPersistence                    = InstitutionPersistence.selectInstitution(sharding, institution.LEI, institution.activityYear)
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

      case Success(InstitutionNotExists(lei)) => postInstitution(institution, uri)

      case Success(_) =>
        complete(StatusCodes.BadRequest)
    }
  }

  private def modifyCall(
                          incomingInstitution: Institution,
                          originalInstOpt: Option[Institution],
                          institutionPersistence: EntityRef[InstitutionCommand]
                        ): Future[InstitutionEvent] = {
    val originalFilerFlag      = originalInstOpt.getOrElse(Institution.empty).hmdaFiler
    val originalHasFiledQ1Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ1
    val originalHasFiledQ2Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ2
    val originalHasFiledQ3Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ3

    val iFilerFlagsSet = incomingInstitution.copy(
      hmdaFiler = originalFilerFlag,
      quarterlyFilerHasFiledQ1 = originalHasFiledQ1Flag,
      quarterlyFilerHasFiledQ2 = originalHasFiledQ2Flag,
      quarterlyFilerHasFiledQ3 = originalHasFiledQ3Flag
    )
    institutionPersistence ? (ref => ModifyInstitution(iFilerFlagsSet, ref))
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
  // $COVERAGE-OFF$
  private def getAllInstitutions(lei: String, uri: Uri): Route = {
    val years = config.getString("hmda.rules.yearly-filing.years-allowed").split(",").toList
    val institutionsF: List[Future[Option[Institution]]] = years.map(year => (selectInstitution(sharding, lei, year.toInt) ? GetInstitution))
    val fInstitutions = Future.sequence(institutionsF) 
    onComplete(fInstitutions) {
      case Success(i) =>
        val institutions = i.filter(_ != None)
        if (institutions.isEmpty) {
          complete(StatusCodes.NotFound)
        } else complete(institutions)
      case Failure(e) =>
        val errorResponse = ErrorResponse(500, e.getLocalizedMessage, uri.path)
        complete((StatusCodes.InternalServerError, errorResponse))
    }
  }
  // $COVERAGE-ON$

  private def validTaxIdFormat(taxIdOption: Option[String]): Boolean = {
    val taxId        = taxIdOption.getOrElse("")
    val taxIdPattern = "^[0-9]{2}\\-[0-9]{7}$".r
    taxId match {
      case taxIdPattern() => true
      case _              => false
    }
  }

  private def validLeiFormat(lei: String): Boolean = {
    val leiPattern = "^[A-Z0-9]{20}$".r
    lei match {
      case leiPattern() => true
      case _            => false
    }
  }

  private def validAgencyCodeFormat(agencyCode: Int): Boolean =
    Agency.values.contains(agencyCode)

  private def sanatizeInstitutionIdentifiers(
                                              institution: Institution,
                                              checkLei: Boolean,
                                              checkAgencyCode: Boolean,
                                              uri: Uri,
                                              route: (Institution, Uri) => Route
                                            ): Route =
    if (!validTaxIdFormat(institution.taxId)) {
      complete((StatusCodes.BadRequest, "Incorrect tax-id format"))
    } else if (checkLei && !validLeiFormat(institution.LEI)) {
      complete((StatusCodes.BadRequest, "Incorrect lei format"))
    } else if (checkAgencyCode && !validAgencyCodeFormat(institution.agency.code)) {
      complete((StatusCodes.BadRequest, "Incorrect agency code format"))
    } else if (checkListIfPublicDomain(institution.emailDomains)) {
      complete((StatusCodes.BadRequest, "Email domain is a public domain"))
    } else route(institution, uri)
}