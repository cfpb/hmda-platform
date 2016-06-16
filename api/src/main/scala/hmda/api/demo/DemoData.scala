package hmda.api.demo

import akka.actor.ActorSystem
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.FilingPersistence
import hmda.api.persistence.FilingPersistence.CreateFiling
import hmda.api.persistence.InstitutionPersistence.CreateInstitution
import hmda.model.fi._

object DemoData {
  val institutions = {
    val i1 = Institution("12345", "First Bank", Active)
    val i2 = Institution("123456", "Second Bank", Inactive)
    Set(i1, i2)
  }

  val filings = {
    val f1 = Filing("2016", "12345", Completed)
    val f2 = Filing("2017", "12345", NotStarted)
    val f3 = Filing("2017", "123456", InProgress)
    Seq(f1, f2, f3)
  }

  def loadData(system: ActorSystem): Unit = {
    Thread.sleep(500)
    loadInstitutions(system)
    loadFilings(system)
  }

  def loadInstitutions(system: ActorSystem): Unit = {
    val institutionsActor = system.actorSelection("/user/institutions")
    institutions.foreach(i => institutionsActor ! CreateInstitution(i))
  }

  def loadFilings(system: ActorSystem): Unit = {
    filings.foreach { filing =>
      val filingActor = system.actorOf(FilingPersistence.props(filing.fid))
      filingActor ! CreateFiling(filing)
      filingActor ! Shutdown
    }
  }
}
