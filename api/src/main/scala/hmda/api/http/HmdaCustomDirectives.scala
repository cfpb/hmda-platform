package hmda.api.http

import akka.actor.{ Actor, ActorSystem }
import akka.http.scaladsl.server.Directive0
import akka.http.scaladsl.server.Directives._
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.{ HttpRequest, HttpResponse, StatusCodes }
import akka.stream.ActorMaterializer
import hmda.api.model.ErrorResponse
import hmda.model.fi.{ Created, Submission }
import akka.pattern.ask
import akka.util.Timeout
import hmda.api.protocol.processing.ApiErrorProtocol
import hmda.persistence.institutions.SubmissionPersistence
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.CommonMessages._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._

import scala.util.{ Failure, Success }

trait HmdaCustomDirectives extends ApiErrorProtocol {

  val log: LoggingAdapter
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  implicit val timeout: Timeout

  def time: Directive0 = {
    val startTime = System.currentTimeMillis()

    mapResponse { response =>
      val endTime = System.currentTimeMillis()
      val responseTime = endTime - startTime
      log.debug(s"Request took $responseTime ms")
      response
    }

  }

  def preventSubmissionOverwrite(fid: String, filingId: String, submissionId: Int, path: String) =
    mapInnerRoute { route =>
      val submissionsActor = system.actorOf(SubmissionPersistence.props(fid, filingId))
      val submission = (submissionsActor ? GetSubmissionById(submissionId)).mapTo[Submission]
      onComplete(submission) {
        case Success(submission) =>
          submissionsActor ! Shutdown
          if (submission.submissionStatus == Created) route
          else {
            val errorResponse = ErrorResponse(400, "Submission already exists", path)
            complete(ToResponseMarshallable(StatusCodes.BadRequest -> errorResponse))
          }
        case Failure(error) =>
          submissionsActor ! Shutdown
          val errorResponse = ErrorResponse(500, "Internal server error", path)
          complete(ToResponseMarshallable(StatusCodes.InternalServerError -> errorResponse))
      }
    }

}
