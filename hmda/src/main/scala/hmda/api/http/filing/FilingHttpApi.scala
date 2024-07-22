package hmda.api.http.filing

import java.time.Instant

import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives.{cors, corsRejectionHandler}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.PathMatchers._
import hmda.api.http.directives.CreateFilingAuthorization._
import hmda.api.http.directives.QuarterlyFilingAuthorization.quarterlyFilingAllowed
import hmda.api.http.model.ErrorResponse
import hmda.api.http.model.filing.submissions.FilingDetailsSummary
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands.{CreateFiling, GetFilingDetails}
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.filing.{Filing, FilingDetails, InProgress}
import hmda.model.institution.Institution
import hmda.persistence.filing.FilingPersistence._
import hmda.persistence.institution.InstitutionPersistence._
import hmda.util.http.FilingResponseUtils._
import org.slf4j.Logger

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object FilingHttpApi {
  def create(log: Logger, sharding: ClusterSharding)(implicit ec: ExecutionContext, t: Timeout): OAuth2Authorization => Route =
    new FilingHttpApi(log, sharding)(ec, t).filingRoutes _
}

private class FilingHttpApi(log: Logger, sharding: ClusterSharding)(implicit val ec: ExecutionContext, t: Timeout) {
  private val quarterlyFiler = quarterlyFilingAllowed(log, sharding) _

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
            (post & extractUri) { uri =>
              createFilingForInstitution(lei, year, None, uri)
            } ~
              // GET/institutions/<lei>/filings/<year>
              (get & extractUri) { uri =>
                parameter('page.as[Int] ? 1)(pageNumber => getFilingForInstitution(lei, year, None, uri, pageNumber))
              }
          }
        }
      } ~ path("institutions" / Segment / "filings" / IntNumber / "quarter" / Quarter) { (lei, period, quarter) =>
        oAuth2Authorization.authorizeTokenWithLei(lei) { _ =>
          pathEndOrSingleSlash {
            quarterlyFiler(lei, period) {
              // POST/institutions/<lei>/filings/<year>/quarters/<quarter>
              (post & extractUri) { uri =>
                createFilingForInstitution(lei, period, Option(quarter), uri)
              } ~
                // GET /institutions/<lei>/filings/<year>/quarters/<quarter>
                (get & extractUri) { uri =>
                  parameter('page.as[Int] ? 1)(pageNumber => getFilingForInstitution(lei, period, Option(quarter), uri, pageNumber))
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
    val fEnriched: Future[Option[FilingDetails]]  = fil ? (ref => GetFilingDetails(ref))

    for {
      i: Option[Institution]   <- fInstitution
      d: Option[FilingDetails] <- fEnriched
    } yield (i, d)
  }

  def createFilingForInstitution(lei: String, year: Int, quarter: Option[String], uri: Uri): Route =
    isFilingAllowed(year, quarter) {
      onComplete(obtainFilingDetails(lei, year, quarter)) {
        case Failure(error) =>
          log.error(s"Unable to obtain filing details for an institution for (lei: $lei, year: $year, quarter: $quarter)", error)
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
              log.error(s"Unable to create a filing for an institution for (lei: $lei, period: $period, quarter: $quarter)", error)
              failedResponse(StatusCodes.InternalServerError, uri, error)
          }
      }
    }

  def getFilingForInstitution(lei: String, period: Int, quarter: Option[String], uri: Uri, pageNumber: Int): Route =
    onComplete(obtainFilingDetails(lei, period, quarter)) {
      case Success((Some(_), Some(filingDetails))) =>
        val sortedDetails = filingDetails.copy(submissions = filingDetails.submissions.sortBy(- _.id.sequenceNumber))
        // get all filings 
        if (pageNumber == 0)
          complete(sortedDetails)
        else {
          val summary =
            FilingDetailsSummary(
              filing = sortedDetails.filing,
              submissions = Nil,
              sortedDetails.submissions.length,
              pageNumber,
              uri.path.toString()
            )
          // Note that the current Pagination structure uses the pageNumber and configuration
          // in order to compute the fromIndex (how many entries to skip)
          // and count (how many entries to take)
          // So we first put in empty data but the correct page number and total length
          // in order to compute the correct fromIndex and correct count
          // then we put the actual adjusted data in
          val result = summary.copy(
            submissions = sortedDetails.submissions
              .drop(summary.fromIndex)
              .take(summary.count)
          )
          complete(result)
        }

      case Success((None, _)) =>
        entityNotPresentResponse("institution", lei, uri)

      case Success((Some(i), None)) =>
        val errorResponse = ErrorResponse(404, s"Filing for institution: ${i.LEI} and period: $period does not exist", uri.path)
        complete((StatusCodes.NotFound, errorResponse))

      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }
}