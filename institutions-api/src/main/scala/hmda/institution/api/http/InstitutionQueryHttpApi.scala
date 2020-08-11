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

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

object InstitutionQueryHttpApi {
  def create(config: Config)(implicit ec: ExecutionContext): Route =
    new InstitutionQueryHttpApi(config)(ec).institutionPublicRoutes
}
private class InstitutionQueryHttpApi(config: Config)(implicit ec: ExecutionContext) extends InstitutionEmailComponent with InstitutionNoteHistoryComponent{
  val dbConfig = DatabaseConfig.forConfig[JdbcProfile]("institution_db")

  implicit val institutionRepository2018   = new InstitutionRepository2018(dbConfig, "institutions2018")
  implicit val institutionRepository2019   = new InstitutionRepository2019(dbConfig, "institutions2019")
  implicit val institutionRepository2020   = new InstitutionRepository2020(dbConfig, "institutions2020")
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(dbConfig)
  implicit val institutionNoteHistoryRepository = new InstitutionNoteHistoryRepository(dbConfig)


  val createSchema = config.getString("hmda.institution.createSchema").toBoolean
  if (createSchema) {
    institutionRepository2018.createSchema()
    institutionRepository2019.createSchema()
    institutionEmailsRepository.createSchema()
  }

  //institutions/<lei>/year/<year>
  private val institutionByIdPath =
    path("institutions" / Segment / "year" / IntNumber) { (lei, year) =>
      (extractUri & get) { uri =>
        isFilingAllowed(year, None) {

          val fInstitution = year match {
            case 2018 => institutionRepository2018.findById(lei)
            case 2020 => institutionRepository2020.findById(lei)
            case _    => institutionRepository2019.findById(lei)
          }

          val fEmails = institutionEmailsRepository.findByLei(lei)
          val f = for {
            institution <- fInstitution
            emails      <- fEmails
          } yield (institution, emails.map(_.emailDomain))

          onComplete(f) {
            case Success((institution, emails)) =>
              if (institution.isEmpty) {
                returnNotFoundError(uri)
              } else {
                complete(
                  ToResponseMarshallable(
                    InstitutionConverter
                      .convert(institution.getOrElse(InstitutionEntity()), emails)
                  )
                )
              }
            case Failure(error) =>
              val errorResponse =
                ErrorResponse(500, error.getLocalizedMessage, uri.path)
              complete(ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
          }
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
            parameters(('domain.as[String], 'lei.as[String], 'respondentName.as[String], 'taxId.as[String])) {
              (domain, lei, respondentName, taxId) =>
                val f = findByFields(lei, respondentName, taxId, domain, year.toString)
                completeInstitutionsFuture(f, uri)
            }
        }
      }
    }


  def completeInstitutionsNoteHistoryFuture(f: Future[Seq[InstitutionNoteHistoryEntity]], uri: Uri): Route =
    onComplete(f) {
      case Success(institutionNoteHistory) =>
        if (institutionNoteHistory.isEmpty) {
          returnNotFoundError(uri)
        } else {
          complete(ToResponseMarshallable(InstitutionNoteHistoryResponse(institutionNoteHistory)))
        }
      case Failure(error) =>
        if (error.getLocalizedMessage.contains("filter predicate is not satisfied")) {
          returnNotFoundError(uri)
        } else {
          val errorResponse = ErrorResponse(500, error.getLocalizedMessage, uri.path)
          complete(ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
        }
    }
  private val getInstitutionHistory =
    path("institutions" / Segment / "year" / IntNumber  / "history") { (lei, year) =>
      (extractUri & get) { uri =>
        isFilingAllowed(year, None) {
            val f = institutionNoteHistoryRepository.findInstitutionHistory( year.toString,lei)
            completeInstitutionsNoteHistoryFuture(f, uri)
        }
      }
    }

  private val institutionByDomainDefaultPath =
  path("institutions") {
    (extractUri & get) { uri =>
      parameter('domain.as[String]) { domain =>
        val f = findByEmailAnyYear(domain)
        completeInstitutionsFuture(f, uri)
      } ~
        parameters(('domain.as[String], 'lei.as[String], 'respondentName.as[String], 'taxId.as[String])) {
          (domain, lei, respondentName, taxId) =>
            val f =
              findByFields(lei, respondentName, taxId, domain, currentYear)
            completeInstitutionsFuture(f, uri)
        }
    }
  }

  private def completeInstitutionsFuture(f: Future[Seq[Institution]], uri: Uri): Route =
    onComplete(f) {
      case Success(institutions) =>
        if (institutions.isEmpty) {
          returnNotFoundError(uri)
        } else {
          complete(ToResponseMarshallable(InstitutionsResponse(institutions)))
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
          institutionByIdPath ~ institutionByDomainPath ~ institutionByDomainDefaultPath /* ~ institutionLoaderCSVByYear*/
        }
      }
    }

}