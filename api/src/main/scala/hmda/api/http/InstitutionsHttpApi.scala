package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.pattern.ask
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.util.Timeout
import hmda.api.model.Institutions
import hmda.api.persistence.CommonMessages.GetState
import hmda.api.persistence.InstitutionPersistence.GetInstitutionById
import hmda.api.protocol.processing.InstitutionProtocol
import hmda.model.fi.Institution

import scala.util.{ Failure, Success }

trait InstitutionsHttpApi extends InstitutionProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val institutionsPath =
    path("institutions") {
      val institutionsActor = system.actorSelection("/user/institutions")
      get {
        val fInstitutions = (institutionsActor ? GetState).mapTo[Set[Institution]]
        onComplete(fInstitutions) {
          case Success(institutions) =>
            complete(ToResponseMarshallable(Institutions(institutions)))
          case Failure(error) =>
            log.error(error.getLocalizedMessage)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val institutionByIdPath =
    path("institutions" / Segment) { fid =>
      val institutionsActor = system.actorSelection("/user/institutions")
      get {
        val fInstitutions = (institutionsActor ? GetInstitutionById(fid)).mapTo[Institution]
        onComplete(fInstitutions) {
          case Success(institution) =>
            complete(ToResponseMarshallable(institution))
          case Failure(error) =>
            log.error(error.getLocalizedMessage)
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val institutionsRoutes = institutionsPath ~ institutionByIdPath
}
