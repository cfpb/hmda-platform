package hmda.api.http

import akka.actor.ActorSystem
import akka.event.LoggingAdapter
import akka.stream.ActorMaterializer
import hmda.api.protocol.processing.SubmissionProtocol
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ HttpResponse, StatusCodes }
import akka.util.Timeout
import akka.pattern.ask
import hmda.api.model.Submissions
import hmda.api.persistence.CommonMessages.GetState
import hmda.api.persistence.SubmissionPersistence
import hmda.model.fi.Submission
import hmda.api.persistence.CommonMessages._

import scala.util.{ Failure, Success }

trait SubmissionsHttpApi extends SubmissionProtocol {

  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  val submissionPath =
    path("institutions" / Segment / "filings" / Segment / "submissions") { (fid, period) =>
      val submissionsActor = system.actorOf(SubmissionPersistence.props(fid, period))
      get {
        val fSubmissions = (submissionsActor ? GetState).mapTo[Seq[Submission]]
        onComplete(fSubmissions) {
          case Success(submissions) =>
            submissionsActor ! Shutdown
            complete(ToResponseMarshallable(Submissions(submissions)))
          case Failure(error) =>
            submissionsActor ! Shutdown
            complete(HttpResponse(StatusCodes.InternalServerError))
        }
      }
    }

  val submissionRoutes = submissionPath

}
