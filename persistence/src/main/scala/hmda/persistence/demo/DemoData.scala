package hmda.persistence.demo

import akka.actor.ActorSystem
import hmda.model.fi._
import hmda.model.institution.Agency.{ CFPB, FDIC }
import hmda.model.institution.ExternalIdType.{ FdicCertNo, FederalTaxId, RssdId }
import hmda.model.institution.{ ExternalId, Institution }
import hmda.model.institution.InstitutionStatus.{ Active, Inactive }
import hmda.model.institution.InstitutionType.{ Bank, CreditUnion }
import hmda.persistence.institutions.FilingPersistence.CreateFiling
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.CommonMessages._
import hmda.persistence.institutions.SubmissionPersistence.CreateSubmission
import hmda.persistence.institutions.{ FilingPersistence, SubmissionPersistence }

object DemoData {
  val externalId1 = ExternalId("externalTest1", FdicCertNo)
  val externalId2 = ExternalId("externalTest2", RssdId)
  val institutions = {
    val i1 = Institution(12345, "First Bank", Set(externalId1), FDIC, Bank, Active)
    val i2 = Institution(123456, "Second Bank", Set(externalId2), CFPB, CreditUnion, Inactive)
    Set(i1, i2)
  }

  val filings = {
    val f1 = Filing("2016", "12345", Completed)
    val f2 = Filing("2017", "12345", NotStarted)
    val f3 = Filing("2017", "123456", InProgress)
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
      val submissionsActor = system.actorOf(SubmissionPersistence.props("12345", "2017"))
      submissionsActor ! CreateSubmission
      Thread.sleep(100)
      submissionsActor ! Shutdown
    }
  }
}
