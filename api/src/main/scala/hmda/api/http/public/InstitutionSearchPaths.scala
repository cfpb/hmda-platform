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
import hmda.api.model.public.InstitutionSearch
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
                  val institutionSearch = institutions.map(i => institutiontoInstitutionSearch(i))
                  complete(ToResponseMarshallable(institutionSearch))
                } else {
                  complete(HttpResponse(StatusCodes.NotFound))
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
