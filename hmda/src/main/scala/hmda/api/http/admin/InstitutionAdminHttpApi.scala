package hmda.api.http.admin

import akka.actor.{ActorSystem, Scheduler}
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import akka.util.Timeout
import akka.http.scaladsl.server.Directives._
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.server.Route
import hmda.model.institution.Institution
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.directives.HmdaTimeDirectives
import hmda.persistence.institution.InstitutionPersistence._
import hmda.api.http.codec.institution.InstitutionCodec._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

trait InstitutionAdminHttpApi extends HmdaTimeDirectives {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter
  implicit val ec: ExecutionContext
  implicit val timeout: Timeout

  val institutionsWritePath =
    path("institutions") {
      entity(as[Institution]) { institution =>
        require(
          institution.LEI.isDefined && institution.LEI.getOrElse("") != "")
        val typedSystem = system.toTyped
        implicit val scheduler: Scheduler = typedSystem.scheduler

        val institutionPersistence =
          createShardedInstitution(typedSystem, institution.LEI.getOrElse(""))

        timedPost { uri =>
          val fCreated: Future[InstitutionCreated] = institutionPersistence ? (
              ref => CreateInstitution(institution, ref))

          onComplete(fCreated) {
            case Success(InstitutionCreated(i)) =>
              complete(ToResponseMarshallable(i))
            case Failure(error) => complete(error.getLocalizedMessage)
          }
        }

      }
    }

  def institutionAdminRoutes: Route = encodeResponse {
    institutionsWritePath
  }

}
