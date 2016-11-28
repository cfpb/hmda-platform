package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSelection, ActorSystem }
import akka.http.scaladsl.marshalling.ToResponseMarshallable
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import akka.http.scaladsl.model.{ StatusCodes, Uri }
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.server.Directives._
import akka.pattern.ask
import akka.util.Timeout
import hmda.api.http.HmdaCustomDirectives
import hmda.api.model.ErrorResponse
import hmda.model.fi.{ Filing, Submission, SubmissionId }
import hmda.model.institution.Institution
import hmda.persistence.HmdaSupervisor.{ FindFilings, FindSubmissions }
import hmda.persistence.institutions.FilingPersistence.GetFilingByPeriod
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.projections.institutions.InstitutionView
import hmda.query.projections.institutions.InstitutionView.GetInstitutionById

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait RequestVerificationUtils extends HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val timeout: Timeout

  /*
     If the institution, filing, and submission exist, complete the request.
     If any one does not exist, respond with 404 error, including an appropriate message.
   */
  def completeVerified(institutionId: String, period: String, seqNr: Int, uri: Uri)(completeRequest: Route)(implicit ec: ExecutionContext): Route = {
    onComplete(verifyRequest(institutionId, period, seqNr)) {
      case Success(None) => completeRequest
      case Success(Some(message)) =>
        val errorResponse = ErrorResponse(404, message, uri.path)
        complete(ToResponseMarshallable(StatusCodes.NotFound -> errorResponse))
      case Failure(error) => completeWithInternalError(uri, error)
    }
  }

  /*
     If the request is legal (i.e. the institution, period, and filing exist), return None.
     If any of the objects does not exist, return Some(message), where message describes the issue.
   */
  private def verifyRequest(institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val submissionId = SubmissionId(institutionId, period, seqNr)

    for {
      i <- fInstitution(submissionId)
      f <- fFiling(submissionId)
      s <- fSubmission(submissionId)
      msg <- getErrorMessage(i, f, s, institutionId, period, submissionId)
    } yield msg
  }

  private def getErrorMessage(i: Institution, f: Filing, s: Submission, iid: String, p: String, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Option[String]] = Future {
    val submissionFound: Boolean = s.id == sid
    val filingFound: Boolean = f.period == p
    val institutionFound: Boolean = i.id == iid

    if (submissionFound && filingFound && institutionFound) None
    else if (filingFound && institutionFound) Some(s"Submission ${sid.sequenceNumber} not found for $p filing")
    else if (institutionFound) Some(s"$p filing not found for institution $iid")
    else Some(s"Institution $iid not found")
  }

  private def fInstitution(sid: SubmissionId)(implicit ec: ExecutionContext): Future[Institution] = {
    val querySupervisor = system.actorSelection("/user/query-supervisor")
    val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    fInstitutionsActor.flatMap(ia => ia ? GetInstitutionById(sid.institutionId)).mapTo[Institution]
  }

  private def fFiling(sid: SubmissionId)(implicit ec: ExecutionContext): Future[Filing] = {
    val fFilingsActor = (supervisor ? FindFilings(FilingPersistence.name, sid.institutionId)).mapTo[ActorRef]
    fFilingsActor.flatMap(fa => fa ? GetFilingByPeriod(sid.period)).mapTo[Filing]
  }

  private def fSubmission(sid: SubmissionId)(implicit ec: ExecutionContext): Future[Submission] = {
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, sid.institutionId, sid.period)).mapTo[ActorRef]
    fSubmissionsActor.flatMap(sa => sa ? GetSubmissionById(sid)).mapTo[Submission]
  }

  private def supervisor: ActorSelection = system.actorSelection("/user/supervisor")

}
