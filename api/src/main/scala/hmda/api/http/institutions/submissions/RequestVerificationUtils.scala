package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
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
import hmda.persistence.messages.commands.filing.FilingCommands._
import hmda.persistence.institutions.SubmissionPersistence.GetSubmissionById
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence, SubmissionPersistence }
import hmda.persistence.messages.commands.institutions.InstitutionCommands.GetInstitutionById
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName

import scala.concurrent.{ ExecutionContext, Future }
import scala.util.{ Failure, Success }

trait RequestVerificationUtils extends HmdaCustomDirectives {

  implicit val system: ActorSystem
  implicit val timeout: Timeout

  /*
     If the institution, filing, and submission exist, complete the request.
     If any one does not exist, respond with 404 error, including an appropriate message.
   */
  def completeVerified(supervisor: ActorRef, institutionId: String, period: String, seqNr: Int, uri: Uri)(completeRequest: Route)(implicit ec: ExecutionContext): Route = {
    onComplete(verifyRequest(supervisor, institutionId, period, seqNr)) {
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
  private def verifyRequest(supervisor: ActorRef, institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val submissionId = SubmissionId(institutionId, period, seqNr)

    val inst = fInstitution(supervisor, submissionId)
    val fil = fFiling(supervisor, submissionId)
    val sub = fSubmission(supervisor, submissionId)

    for {
      i <- inst
      f <- fil
      s <- sub
      msg <- getErrorMessage(i, f, s, institutionId, period, submissionId)
    } yield msg
  }

  private def getErrorMessage(i: Option[Institution], f: Filing, s: Submission, iid: String, p: String, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Option[String]] = Future {
    val submissionFound: Boolean = s.id == sid
    val filingFound: Boolean = f.period == p
    val institutionFound: Boolean = i.getOrElse(Institution.empty).id == iid

    if (submissionFound && filingFound && institutionFound) None
    else if (filingFound && institutionFound) Some(s"Submission ${sid.sequenceNumber} not found for $p filing period")
    else if (institutionFound) Some(s"$p filing period not found for institution $iid")
    else Some(s"Institution $iid not found")
  }

  private def fInstitution(supervisor: ActorRef, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Option[Institution]] = {
    val fInstitutionsActor = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    fInstitutionsActor.flatMap(ia => ia ? GetInstitutionById(sid.institutionId)).mapTo[Option[Institution]]
  }

  private def fFiling(supervisor: ActorRef, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Filing] = {
    val fFilingsActor = (supervisor ? FindFilings(FilingPersistence.name, sid.institutionId)).mapTo[ActorRef]
    fFilingsActor.flatMap(fa => fa ? GetFilingByPeriod(sid.period)).mapTo[Filing]
  }

  private def fSubmission(supervisor: ActorRef, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Submission] = {
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, sid.institutionId, sid.period)).mapTo[ActorRef]
    fSubmissionsActor.flatMap(sa => sa ? GetSubmissionById(sid)).mapTo[Submission]
  }

}
