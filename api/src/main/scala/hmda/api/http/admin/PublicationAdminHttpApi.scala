package hmda.api.http.admin

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.model.fi.SubmissionId
import hmda.persistence.messages.commands.disclosure.DisclosureCommands.GenerateDisclosureReports

import scala.util.{ Failure, Success }

trait PublicationAdminHttpApi extends HmdaCustomDirectives with ApiErrorProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  val log: LoggingAdapter

  def disclosureGenerationPath(supervisor: ActorRef) =
    path("disclosure" / Segment / IntNumber / IntNumber) { (instId, year, subId) =>
      extractExecutionContext { executor =>
        implicit val ec = executor
        timedPost { uri =>
          val actorRef = system.actorSelection("user/disclosure-report-publisher").resolveOne()
          val submissionId = SubmissionId(instId, year.toString, subId)

          val message = for {
            a <- actorRef
          } yield a ! GenerateDisclosureReports(submissionId)

          onComplete(message) {
            case Success(created) =>
              complete(ToResponseMarshallable(StatusCodes.OK))

            case Failure(error) =>
              completeWithInternalError(uri, error)
          }
        }
      }
    }

  def publicationRoutes(supervisor: ActorRef) = disclosureGenerationPath(supervisor)

}
