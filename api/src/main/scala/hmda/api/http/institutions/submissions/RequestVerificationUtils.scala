package hmda.api.http.institutions.submissions

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
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

trait RequestVerificationUtils {

  implicit val system: ActorSystem
  implicit val timeout: Timeout

  /*
     If the request is legal (i.e. the institution, period, and filing exist), return None.
     If any of the objects does not exist, return Some(message), where message describes the issue.
   */
  def verifyRequest(institutionId: String, period: String, seqNr: Int)(implicit ec: ExecutionContext): Future[Option[String]] = {
    val submissionId = SubmissionId(institutionId, period, seqNr)

    val supervisor = system.actorSelection("/user/supervisor")
    val querySupervisor = system.actorSelection("/user/query-supervisor")

    val fInstitutionsActor = (querySupervisor ? FindActorByName(InstitutionView.name)).mapTo[ActorRef]
    val fFilingsActor = (supervisor ? FindFilings(FilingPersistence.name, institutionId)).mapTo[ActorRef]
    val fSubmissionsActor = (supervisor ? FindSubmissions(SubmissionPersistence.name, institutionId, period)).mapTo[ActorRef]

    for {
      ia <- fInstitutionsActor
      i <- (ia ? GetInstitutionById(institutionId)).mapTo[Institution]
      fa <- fFilingsActor
      f <- (fa ? GetFilingByPeriod(period)).mapTo[Filing]
      sa <- fSubmissionsActor
      s <- (sa ? GetSubmissionById(submissionId)).mapTo[Submission]
      msg <- getErrorMessage(i, f, s, institutionId, period, submissionId)
    } yield msg
  }

  private def getErrorMessage(i: Institution, f: Filing, s: Submission, iid: String, p: String, sid: SubmissionId)(implicit ec: ExecutionContext): Future[Option[String]] = Future {

    if (s.id == sid) None
    else if (f.period == p) Some(s"Submission ${sid.sequenceNumber} not found for $p filing")
    else if (i.id == iid) Some(s"$p filing not found for institution $iid")
    else Some(s"Institution $iid not found")
  }

}
