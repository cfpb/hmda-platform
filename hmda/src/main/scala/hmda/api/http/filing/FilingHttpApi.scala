package hmda.api.http.filing

import java.time.Instant

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.model.StatusCodes
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.headers.RawHeader
import hmda.api.http.directives.{CreateFilingAuthorization, HmdaTimeDirectives, QuarterlyFilingAuthorization}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.messages.filing.FilingCommands.{CreateFiling, GetFilingDetails}
import hmda.model.filing.{Filing, FilingDetails, InProgress}
import hmda.persistence.filing.FilingPersistence._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence._
import hmda.util.http.FilingResponseUtils._
import hmda.api.http.PathMatchers._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait FilingHttpApi extends HmdaTimeDirectives with QuarterlyFilingAuthorization with CreateFilingAuthorization {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  def filingRoutes(oAuth2Authorization: OAuth2Authorization): Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          filingReadPath(oAuth2Authorization)
        }
      }
    }

  def filingReadPath(oAuth2Authorization: OAuth2Authorization): Route =
    respondWithDefaultHeader(RawHeader("Cache-Control", "no-cache")) {
      path("institutions" / Segment / "filings" / IntNumber) { (lei, year) =>
        oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
          pathEndOrSingleSlash {
            // POST/institutions/<lei>/filings/<year>
            timedPost { uri =>
              createFilingForInstitution(lei, year, None, uri)
            } ~
              // GET/institutions/<lei>/filings/<year>
              timedGet { uri =>
                getFilingForInstitution(lei, year, None, uri)
              }
          }
        }
      } ~ path("institutions" / Segment / "filings" / IntNumber / "quarter" / Quarter) { (lei, period, quarter) =>
        oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
          pathEndOrSingleSlash {
            quarterlyFilingAllowed(lei, period) {
              // POST/institutions/<lei>/filings/<year>/quarters/<quarter>
              timedPost { uri =>
                createFilingForInstitution(lei, period, Option(quarter), uri)
              } ~
                // GET /institutions/<lei>/filings/<year>/quarters/<quarter>
                timedGet { uri =>
                  getFilingForInstitution(lei, period, Option(quarter), uri)
                }
            }
          }
        }
      }
    }

  def obtainFilingDetails(
                           lei: String,
                           period: Int,
                           quarter: Option[String]
  ): Future[(Option[Institution], Option[FilingDetails])] = {
    val ins = selectInstitution(sharding, lei, period)
    val fil = selectFiling(sharding, lei, period, quarter)

    val fInstitution: Future[Option[Institution]] = ins ? (ref => GetInstitution(ref))
    val fEnriched: Future[Option[FilingDetails]] = fil ? (ref => GetFilingDetails(ref))

    for {
      i: Option[Institution]           <- fInstitution
      d: Option[FilingDetails] <- fEnriched
    } yield (i, d)
  }

  def createFilingForInstitution(lei: String, year: Int, quarter: Option[String], uri: Uri): Route =
    isFilingAllowed(year, quarter) {
      onComplete(obtainFilingDetails(lei, year, quarter)) {
        case Failure(error) =>
          log.error(error, s"Unable to obtain filing details for an institution for (lei: $lei, year: $year, quarter: $quarter)")
          failedResponse(StatusCodes.InternalServerError, uri, error)

        case Success((None, _)) =>
          entityNotPresentResponse("institution", lei, uri)

        case Success((Some(_), Some(_))) =>
          val errorResponse = ErrorResponse(400, s"Filing $lei-$year${quarter.fold("")(q => s"-$q")} already exists", uri.path)
          complete((StatusCodes.BadRequest, errorResponse))

        case Success((Some(_), None)) =>
          val now = Instant.now().toEpochMilli
          // NOTE: We use the period field in Filing to represent both the year and the quarter (optional)
          val period = quarter.fold(ifEmpty = s"$year")(quarter => s"$year-$quarter")
          val filing = Filing(
            period = period,
            lei = lei,
            status = InProgress,
            filingRequired = true,
            start = now,
            end = 0L
          )
          val fFiling: Future[FilingCreated] = selectFiling(sharding, lei, year, quarter) ? (ref => CreateFiling(filing, ref))
          onComplete(fFiling) {
            case Success(created) =>
              val filingDetails = FilingDetails(created.filing, Nil)
              complete((StatusCodes.Created, filingDetails))

            case Failure(error) =>
              log.error(error, s"Unable to create a filing for an institution for (lei: $lei, period: $period, quarter: $quarter)")
              failedResponse(StatusCodes.InternalServerError, uri, error)
          }
      }
    }

  def getFilingForInstitution(lei: String, period: Int, quarter: Option[String], uri: Uri): Route =
    onComplete(obtainFilingDetails(lei, period, quarter)) {
      case Success((Some(_), Some(filingDetails))) =>
        complete(filingDetails)

      case Success((None, _)) =>
        entityNotPresentResponse("institution", lei, uri)

      case Success((Some(i), None)) =>
        val errorResponse = ErrorResponse(404, s"Filing for institution: ${i.LEI} and period: $period does not exist", uri.path)
        complete((StatusCodes.NotFound, errorResponse))

      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }

}