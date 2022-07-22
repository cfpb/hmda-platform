package hmda.institution.api.http

import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.CreateFilingAuthorization._
import hmda.api.http.model.ErrorResponse
import hmda.institution.api.http.model.{InstitutionNoteHistoryResponse, InstitutionsResponse}
import hmda.institution.query._
import hmda.model.institution.Institution
import hmda.utils.YearUtils._
import io.circe.generic.auto._
import slick.basic.DatabaseConfig
import slick.jdbc.JdbcProfile
import hmda.api.http.EmailUtils._
import hmda.query.ts.TransmittalSheetEntity
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object InstitutionQueryHttpApi {
  def create(config: Config)(implicit ec: ExecutionContext): Route =
    new InstitutionQueryHttpApi(config)(ec).institutionPublicRoutes
}
private class InstitutionQueryHttpApi(config: Config)(implicit ec: ExecutionContext) extends InstitutionEmailComponent with InstitutionNoteHistoryComponent{
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("institution_db")

  private val log = LoggerFactory.getLogger(getClass)

  implicit val institutionEmailsRepository: InstitutionEmailsRepository = new InstitutionEmailsRepository(dbConfig)
  implicit val institutionNoteHistoryRepository: InstitutionNoteHistoryRepository = new InstitutionNoteHistoryRepository(dbConfig)

  private val createSchema = config.getString("hmda.institution.createSchema").toBoolean
  if (createSchema) {
    institutionRepositories.values.foreach(_.createSchema())
    institutionEmailsRepository.createSchema()
  }

  //institutions/<lei>/year/<year>
  private val institutionByIdPath =
    path("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      (extractUri & get) { uri =>
        isQuarterlyYearAllowed(year) {

          val defaultRepo = institutionRepositories(institutionConfig.getString("defaultYear"))
          val fInstitution = institutionRepositories.getOrElse(year.toString, defaultRepo).findById(lei)

          val fEmails = institutionEmailsRepository.findByLei(lei)
          val f = for {
            institution <- fInstitution
            emails      <- fEmails
          } yield (institution, emails.map(_.emailDomain))

          val entityMarshaller: PartialFunction[(Option[InstitutionEntity], Seq[String]), ToResponseMarshallable] = {
            case res: (Option[InstitutionEntity], Seq[String]) if res._1.nonEmpty =>
              val (institution, emails) = res
              ToResponseMarshallable(InstitutionConverter
                .convert(institution.getOrElse(InstitutionEntity()), emails))
          }

          completeFuture(f, uri, entityMarshaller)
        }
      }
    }

  private val institutionByDomainPath =
    path("institutions" / "year" / IntNumber) { year =>
      (extractUri & get) { uri =>
        isFilingAllowed(year, None) {
          parameter('domain.as[String]) { domain =>
            val f = findByEmail(domain, year.toString)
            completeInstitutionsFuture(f, uri)
          } ~
            parameters('domain.as[String], 'lei.as[String], 'respondentName.as[String], 'taxId.as[String])  {
              (domain, lei, respondentName, taxId) =>
                val f = findByFields(lei, respondentName, taxId, domain, year.toString)
                completeInstitutionsFuture(f, uri)
            }
        }
      }
    }

  private val institutionHistoryPath =
    path("institutions" / Segment / "year" / IntNumber  / "history") { (lei, year) =>
      (extractUri & get) { uri =>
        val f = institutionNoteHistoryRepository.findInstitutionHistory( year.toString,lei)
        completeInstitutionsNoteHistoryFuture(f, uri)
      }
    }

  def completeInstitutionsNoteHistoryFuture(f: Future[Seq[InstitutionNoteHistoryEntity]], uri: Uri): Route = {
    val entityMarshaller: PartialFunction[Seq[InstitutionNoteHistoryEntity], ToResponseMarshallable] = {
      case institutionNoteHistory: Seq[InstitutionNoteHistoryEntity] if institutionNoteHistory.nonEmpty =>
        ToResponseMarshallable(InstitutionNoteHistoryResponse(institutionNoteHistory))
    }
    completeFuture(f, uri, entityMarshaller)
  }

  private val institutionByDomainDefaultPath =
    path("institutions") {
      (extractUri & get) { uri =>
        parameter('domain.as[String]) { domain =>
          if (checkIfPublicDomain(domain)) {
            returnNotFoundError(uri)
          } else {
            val f = findByEmailAnyYear(domain)
            completeInstitutionsFuture(f, uri)
          }
        } ~
          parameters('domain.as[String], 'lei.as[String], 'respondentName.as[String], 'taxId.as[String]) {
            (domain, lei, respondentName, taxId) =>
              val f =
                findByFields(lei, respondentName, taxId, domain, currentYear)
              completeInstitutionsFuture(f, uri)
          }
      }
    }

  private val institutionLarCountPath =
    path("institutions" / Segment / "lars") { lei =>
      (extractUri & get) { uri =>

        val nonExistingTs: PartialFunction[Throwable, Option[TransmittalSheetEntity]] = {
          case err: Throwable =>
            log.debug("ts repo failure, most likely table not yet available for year, skipping...", err)
            None
        }

        val requestTransmittals = tsRepositories.values.map(_.findById(lei).recover(nonExistingTs)).toSeq
        val transmittalFuture = Future.sequence(requestTransmittals)

        val entityMarshaller: PartialFunction[Seq[Option[TransmittalSheetEntity]], ToResponseMarshallable] = {
          case transmittals: Seq[Option[TransmittalSheetEntity]] if transmittals.flatten.nonEmpty =>
            ToResponseMarshallable(transmittals.flatten.map(ts => InstitutionLarEntity(ts.year.toString, ts.totalLines)))
        }

        completeFuture(transmittalFuture, uri, entityMarshaller)
      }
    }

  private val quarterlyFilersLarCountsPath =
    path("institutions" / "quarterly" / IntNumber / "lars" / "past" / IntNumber) { (year, pastCount) =>
      (extractUri & get) { uri =>
        val fetchLarCounts = InstitutionTsRepo.fetchPastLarCountsForQuarterlies(year, pastCount)
        onComplete(fetchLarCounts) {
          case Success(data) => complete(ToResponseMarshallable(data))
          case Failure(error) =>
            log.debug("most likely tables out of range and doesn't exist.", error)
            returnNotFoundError(uri)
        }
      }
    }

  private def completeInstitutionsFuture(f: Future[Seq[Institution]], uri: Uri): Route = {
    val entityMarshaller: PartialFunction[Seq[Institution], ToResponseMarshallable] = {
      case institutions: Seq[Institution] if institutions.nonEmpty => ToResponseMarshallable(InstitutionsResponse(institutions))
    }
    completeFuture(f, uri, entityMarshaller)
  }

  private def completeFuture[TYPE](future: Future[TYPE], uri: Uri, entityMarshaller: PartialFunction[TYPE, ToResponseMarshallable]): Route =
    onComplete(future) {
      case Success(value) =>
        if (entityMarshaller.isDefinedAt(value)) {
          complete(entityMarshaller(value))
        } else {
          returnNotFoundError(uri)
        }
      case Failure(error) =>
        if (error.getLocalizedMessage.contains("filter predicate is not satisfied")) {
          returnNotFoundError(uri)
        } else {
          val errorResponse = ErrorResponse(500, error.getLocalizedMessage, uri.path)
          complete(ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
        }
    }

  private def returnNotFoundError(uri: Uri) = {
    val errorResponse = ErrorResponse(404, StatusCodes.NotFound.defaultMessage, uri.path)
    complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
  }

  def institutionPublicRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionByIdPath ~ institutionByDomainPath ~ institutionHistoryPath ~ institutionByDomainDefaultPath ~
            institutionLarCountPath ~ quarterlyFilersLarCountsPath
        }
      }
    }

}