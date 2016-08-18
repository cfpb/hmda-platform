package hmda.persistence.demo

import akka.actor.ActorSystem
import hmda.model.fi._
import hmda.model.institution.Agency.{ CFPB, FDIC, HUD, OCC }
import hmda.model.institution.ExternalIdType.{ FdicCertNo, FederalTaxId, OccCharterId, RssdId }
import hmda.model.institution.{ ExternalId, Institution }
import hmda.model.institution.InstitutionStatus.{ Active, Inactive }
import hmda.model.institution.InstitutionType.{ Bank, CreditUnion }
import hmda.persistence.institutions.FilingPersistence.CreateFiling
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.CommonMessages._
import hmda.persistence.institutions.SubmissionPersistence.CreateSubmission
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }

object DemoData {
  val institutions = DemoInstitutions.values

  val filings = {
    val f1 = Filing("2016", "0", Completed)
    val f2 = Filing("2017", "0", NotStarted)
    val f3 = Filing("2017", "1", Completed)
    val f4 = Filing("2016", "2", Completed)
    val f5 = Filing("2016", "3", Completed)
    val f6 = Filing("2017", "4", NotStarted)
    Seq(f1, f2, f3)
  }

  val institutionSummary = {
    val institution = institutions.head
    val f = filings.filter(x => x.institutionId == institution.id.toString)
    (institution.id, institution.name, f.reverse)
  }

  val newSubmissions = {
    val s1 = Submission(1, Created)
    val s2 = Submission(2, Created)
    val s3 = Submission(3, Created)
    Seq(s1, s2, s3)
  }

  def loadData(system: ActorSystem): Unit = {
    Thread.sleep(500)
    loadInstitutions(system)
    loadFilings(system)
    loadNewSubmissions(system)
  }

  def loadInstitutions(system: ActorSystem): Unit = {
    val institutionsActor = system.actorSelection("/user/institutions")
    institutions.foreach(i => institutionsActor ! CreateInstitution(i))
  }

  def loadFilings(system: ActorSystem): Unit = {
    filings.foreach { filing =>
      val filingActor = system.actorOf(FilingPersistence.props(filing.institutionId))
      filingActor ! CreateFiling(filing)
      Thread.sleep(100)
      filingActor ! Shutdown
    }
  }

  def loadNewSubmissions(system: ActorSystem): Unit = {
    newSubmissions.foreach { s =>
      val submissionsActor = system.actorOf(SubmissionPersistence.props("0", "2017"))
      submissionsActor ! CreateSubmission
      Thread.sleep(100)
      submissionsActor ! Shutdown
    }
  }
}
