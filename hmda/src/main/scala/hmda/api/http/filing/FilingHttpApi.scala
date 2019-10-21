package hmda.api.http.filing

import java.time.Instant

import akka.actor.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.event.LoggingAdapter
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.{ PathMatcher1, Route }
import akka.http.scaladsl.model.headers.RawHeader
import hmda.api.http.directives.HmdaTimeDirectives
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import hmda.messages.filing.FilingCommands.{ CreateFiling, GetFilingDetails }
import hmda.model.filing.{ Filing, FilingDetails, InProgress }
import hmda.persistence.filing.FilingPersistence
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.auth.OAuth2Authorization
import hmda.messages.filing.FilingCommands
import hmda.messages.filing.FilingEvents.FilingCreated
import hmda.messages.institution.InstitutionCommands
import hmda.messages.institution.InstitutionCommands.GetInstitution
import hmda.model.institution.Institution
import hmda.persistence.institution.InstitutionPersistence
import hmda.util.http.FilingResponseUtils._
import hmda.utils.YearUtils._

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait FilingHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val sharding: ClusterSharding

  val Quarter: PathMatcher1[String] = Segment.flatMap(Option(_).filter(isValidQuarter))
  val Year: PathMatcher1[Int]       = IntNumber.flatMap(Option(_).filter(isValidYear))

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
      path("institutions" / Segment / "filings" / Year) { (lei, year) =>
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
      } ~ path("institutions" / Segment / "filings" / Year / "quarters" / Quarter) { (lei, year, quarter) =>
        pathEndOrSingleSlash {
          // POST/institutions/<lei>/filings/<year>/quarters/<quarter>
          timedPost { uri =>
            createFilingForInstitution(lei, year, Option(quarter), uri)
          } ~
            // GET /institutions/<lei>/filings/<year>/quarters/<quarter>
            timedGet { uri =>
              getFilingForInstitution(lei, year, Option(quarter), uri)
            }
        }
      }
    }

  def selectInstitution(lei: String, year: Int, quarter: Option[String]): EntityRef[InstitutionCommands.InstitutionCommand] =
    if (year == 2018) sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$lei")
    else {
      val coordinates = quarter.fold(ifEmpty = s"$lei-$year")(q => s"$lei-$year-$q")
      sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-$coordinates")
    }

  def selectFiling(lei: String, year: Int, quarter: Option[String]): EntityRef[FilingCommands.FilingCommand] = {
    val coordinates = quarter.fold(ifEmpty = s"$lei-$year")(q => s"$lei-$year-$q")
    sharding.entityRefFor(FilingPersistence.typeKey, s"${FilingPersistence.name}-$coordinates")
  }

  def obtainFilingDetails(lei: String, period: Int, quarter: Option[String]): Future[(Option[Institution], Option[FilingDetails])] = {
    val ins = selectInstitution(lei, period, quarter)
    val fil = selectFiling(lei, period, quarter)

    val fInstitution: Future[Option[Institution]] = ins ? (ref => GetInstitution(ref))
    val fDetails: Future[Option[FilingDetails]]   = fil ? (ref => GetFilingDetails(ref))

    for {
      i <- fInstitution
      d <- fDetails
    } yield (i, d)
  }

  def getFilingForInstitution(lei: String, period: Int, quarter: Option[String], uri: Uri): Route =
    onComplete(obtainFilingDetails(lei, period, quarter)) {
      case Success((Some(_), Some(filingDetails))) =>
        complete(filingDetails)

      case Success((None, _)) =>
        entityNotPresentResponse("institution", lei, uri)

      case Success((Some(i), None)) =>
        val errorResponse = ErrorResponse(404, s"Filing for institution: ${i.LEI} and period: $period does not exist", uri.path)
        complete(StatusCodes.NotFound, errorResponse)

      case Failure(error) =>
        failedResponse(StatusCodes.InternalServerError, uri, error)
    }

  def createFilingForInstitution(lei: String, year: Int, quarter: Option[String], uri: Uri): Route =
    onComplete(obtainFilingDetails(lei, year, quarter)) {
      case Failure(error) =>
        log.error(error, s"Unable to obtain filing details for an institution for (lei: $lei, year: $year, quarter: $quarter)")
        failedResponse(StatusCodes.InternalServerError, uri, error)

      case Success((None, _)) =>
        entityNotPresentResponse("institution", lei, uri)

      case Success((Some(_), Some(_))) =>
        val errorResponse = ErrorResponse(400, s"Filing $lei-$year${quarter.fold("")(q => s"-$q")} already exists", uri.path)
        complete(StatusCodes.BadRequest, errorResponse)

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
        val fFiling: Future[FilingCreated] = selectFiling(lei, year, quarter) ? (ref => CreateFiling(filing, ref))
        onComplete(fFiling) {
          case Success(created) =>
            val filingDetails = FilingDetails(created.filing, Nil)
            complete(StatusCodes.Created, filingDetails)

          case Failure(error) =>
            log.error(error, "Unable to create a filing for an institution for (lei: $lei, period: $period, quarter: $quarter)")
            failedResponse(StatusCodes.InternalServerError, uri, error)
        }
    }
}
