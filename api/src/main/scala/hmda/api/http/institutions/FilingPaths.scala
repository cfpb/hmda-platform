package hmda.api.http.institutions

import akka.actor.{ ActorRef, ActorSystem }
import akka.event.LoggingAdapter
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.api.EC
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.{ ErrorResponse, FilingDetail }
import hmda.api.protocol.processing.{ ApiErrorProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Submission }
import hmda.persistence.messages.CommonMessages.GetState
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait FilingPaths extends InstitutionProtocol with ApiErrorProtocol with HmdaCustomDirectives {
  implicit val system: ActorSystem
  implicit val materializer: ActorMaterializer
  val log: LoggingAdapter

  implicit val timeout: Timeout

  // institutions/<institutionId>/filings/<period>
  def filingByPeriodPath[_: EC](institutionId: String) =
    path("filings" / Segment) { period =>
      timedGet { uri =>
        val supervisor = system.actorSelection("/user/supervisor")
        val fFilings = (supervisor ? FindFilings(FilingPersistence.name, institutionId)).mapTo[ActorRef]
        val fSubmissions = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]

        val fDetails = for {
          f <- fFilings
          s <- fSubmissions
          d <- filingDetailsByPeriod(period, f, s)
        } yield d

        onComplete(fDetails) {
          case Success(filingDetails) =>
            val filing = filingDetails.filing
            if (filing.institutionId == institutionId && filing.period == period)
              complete(ToResponseMarshallable(filingDetails))
            else if (filing.institutionId == institutionId && filing.period != period) {
              val errorResponse = ErrorResponse(404, s"$period filing not found for institution $institutionId", uri.path)
              complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
            } else {
              val errorResponse = ErrorResponse(404, s"Institution $institutionId not found", uri.path)
              complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
            }
          case Failure(error) =>
            completeWithInternalError(uri, error)
        }
      }
    }

  private def filingDetailsByPeriod[_: EC](period: String, filingsActor: ActorRef, submissionActor: ActorRef): Future[FilingDetail] = {
    val fFiling = (filingsActor ? GetFilingByPeriod(period)).mapTo[Filing]
    for {
      filing <- fFiling
      submissions <- (submissionActor ? GetState).mapTo[Seq[Submission]]
    } yield FilingDetail(filing, submissions)
  }
}
