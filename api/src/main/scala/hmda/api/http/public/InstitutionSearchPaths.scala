package hmda.api.http.public

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.ErrorResponse
import hmda.api.model.public.{ InstitutionSearch, InstitutionSearchResults }
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.api.protocol.public.InstitutionSearchProtocol
import hmda.model.institution.Institution
import hmda.persistence.messages.commands.institutions.InstitutionCommands.{ FindInstitutionByDomain, GetInstitutionById }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait InstitutionSearchPaths extends InstitutionSearchProtocol with HmdaCustomDirectives with ApiErrorProtocol {

  implicit val timeout: Timeout
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  // institutions?domain=<domain>
  def institutionSearchPath(institutionPersistenceF: Future[ActorRef]) = {
    path("institutions") {
      encodeResponse {
        timedGet { uri =>
          parameter('domain.as[String]) { domain =>
            val institutionsF = for {
              v <- institutionPersistenceF
              institutions <- (v ? FindInstitutionByDomain(domain)).mapTo[Set[Institution]]
            } yield institutions
            onComplete(institutionsF) {
              case Success(institutions) =>
                if (institutions.nonEmpty) {
                  val xs = institutions.map(i => institutiontoInstitutionSearch(i))
                  val institutionSearchResults = InstitutionSearchResults(xs)
                  complete(ToResponseMarshallable(institutionSearchResults))
                } else {
                  val errorResponse = ErrorResponse(404, s"email domain $domain not found", uri.path)
                  complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
                }
              case Failure(error) =>
                completeWithInternalError(uri, error)
            }
          }
        }
      }
    } ~
      path("institutions" / Segment) { institutionId =>
        encodeResponse {
          timedGet { uri =>
            val institutionF = for {
              a <- institutionPersistenceF
              o <- (a ? GetInstitutionById(institutionId)).mapTo[Option[Institution]]
              i = o.getOrElse(Institution.empty)
            } yield i

            onComplete(institutionF) {
              case Success(i) =>
                if (i.isEmpty) {
                  complete(ToResponseMarshallable(HttpResponse(StatusCodes.NotFound)))
                } else {
                  complete(ToResponseMarshallable(i))
                }
              case Failure(e) =>
                complete(ToResponseMarshallable(HttpResponse(StatusCodes.InternalServerError)))
            }
          }
        }
      }
  }

  protected def institutiontoInstitutionSearch(i: Institution): InstitutionSearch = {
    InstitutionSearch(
      i.id,
      i.respondent.name,
      i.emailDomains,
      i.externalIds
    )
  }

}
