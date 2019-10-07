package hmda.institution.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{HttpResponse, StatusCodes, Uri}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.stream.ActorMaterializer
import akka.util.Timeout
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.api.http.model.ErrorResponse
import hmda.institution.query._
import hmda.query.DbConfiguration._
import hmda.institution.api.http.model.InstitutionsResponse
import hmda.model.institution.Institution
import hmda.utils.YearUtils._
import io.circe.generic.auto._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait InstitutionQueryHttpApi
    extends HmdaTimeDirectives
    with InstitutionEmailComponent {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout
  val log: LoggingAdapter

  implicit val institutionRepository2018 =
    new InstitutionRepository2018(dbConfig, "institutions2018")
  implicit val institutionRepository2019 =
    new InstitutionRepository2019(dbConfig, "institutions2019")
  implicit val institutionEmailsRepository = new InstitutionEmailsRepository(
    dbConfig)

  val currentYear = config.getString("hmda.filing.current")

  val institutionByIdPath =
    path("institutions" / Segment / "year" / Segment) { (lei, year) =>
      timedGet { uri =>
        if (!isValidYear(year.toInt)) {
          complete(
            ErrorResponse(500, s"Invalid Year Provided: $year", uri.path))
        } else {
          val fInstitution = if (year.toInt == 2018) {
            institutionRepository2018.findById(lei)
          } else {
            institutionRepository2019.findById(lei)
          }
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
                complete(ToResponseMarshallable(InstitutionConverter
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
    }

  val institutionByDomainPath =
    path("institutions" / "year" / Segment) { (year) =>
      timedGet { uri =>
        if (!isValidYear(year.toInt)) {
          complete(
            ErrorResponse(500, s"Invalid Year Provided: $year", uri.path))
        } else {
          parameter('domain.as[String]) { domain =>
            val f = findByEmail(domain, year)
            completeInstitutionsFuture(f, uri)
          } ~
            parameters('domain.as[String],
                       'lei.as[String],
                       'respondentName.as[String],
                       'taxId.as[String]) {
              (domain, lei, respondentName, taxId) =>
                val f = findByFields(lei, respondentName, taxId, domain, year)
                completeInstitutionsFuture(f, uri)
            }
        }
      }
    }

  val institutionByDomainDefaultPath =
    path("institutions") {
      timedGet { uri =>
        parameter('domain.as[String]) { domain =>
          val f = findByEmail(domain, currentYear)
          completeInstitutionsFuture(f, uri)
        } ~
          parameters('domain.as[String],
                     'lei.as[String],
                     'respondentName.as[String],
                     'taxId.as[String]) {
            (domain, lei, respondentName, taxId) =>
              val f =
                findByFields(lei, respondentName, taxId, domain, currentYear)
              completeInstitutionsFuture(f, uri)
          }
      }
    }

  private def completeInstitutionsFuture(f: Future[Seq[Institution]],
                                         uri: Uri): Route = {
    onComplete(f) {
      case Success(institutions) =>
        if (institutions.isEmpty) {
          complete(ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
        } else {
          complete(ToResponseMarshallable(InstitutionsResponse(institutions)))
        }
      case Failure(error) =>
        val errorResponse =
          ErrorResponse(500, error.getLocalizedMessage, uri.path)
        complete(
          ToResponseMarshallable(
            StatusCodes.InternalServerError -> errorResponse))
    }
  }

  def institutionPublicRoutes: Route =
    handleRejections(corsRejectionHandler) {
      cors() {
        encodeResponse {
          institutionByIdPath ~ institutionByDomainPath ~ institutionByDomainDefaultPath
        }
      }
    }

}
