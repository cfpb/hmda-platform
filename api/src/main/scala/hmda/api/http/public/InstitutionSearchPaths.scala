package hmda.api.http.public

import akka.actor.{ ActorRef, ActorSystem }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.ErrorResponse
import hmda.api.model.public.{ InstitutionSearch, InstitutionSearchResults }
import hmda.api.protocol.public.InstitutionSearchProtocol
import hmda.model.institution.Institution
import hmda.query.view.institutions.InstitutionView.FindInstitutionByPeriodAndDomain

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait InstitutionSearchPaths extends InstitutionSearchProtocol with HmdaCustomDirectives {

  implicit val timeout: Timeout
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val ec: ExecutionContext

  // institutions?domain=<domain>
  def institutionSearchPath(institutionViewF: Future[ActorRef]) = {
    path("institutions") {
      encodeResponse {
        timedGet { uri =>
          parameter('domain.as[String]) { domain =>
            val institutionsF = for {
              v <- institutionViewF
              institutions <- (v ? FindInstitutionByPeriodAndDomain(domain)).mapTo[Set[Institution]]
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
