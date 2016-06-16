package hmda.api.demo

import java.io.File

import akka.actor.ActorSystem
import hmda.api.persistence.CommonMessages._
import hmda.api.persistence.FilingPersistence
import hmda.api.persistence.FilingPersistence.CreateFiling
import hmda.api.persistence.InstitutionPersistence.CreateInstitution
import hmda.api.protocol.processing.{ FilingProtocol, InstitutionProtocol }
import hmda.model.fi.{ Filing, Institution }
import spray.json._

import scala.io.Source

case class DemoData(file: File) extends InstitutionProtocol with FilingProtocol {
  val institutions = {
    val instFile = Source.fromFile(file)
    val json = instFile.getLines().toIterable.head.toString
    json.parseJson.convertTo[Set[Institution]]
  }

  val filings = {
    val instFile = Source.fromFile(file)
    val json = instFile.getLines().drop(1).toIterable.head.toString
    json.parseJson.convertTo[Seq[Filing]]
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
