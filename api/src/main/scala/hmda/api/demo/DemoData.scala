package hmda.api.demo

import java.io.File

import akka.actor.ActorSystem
import hmda.api.persistence.InstitutionPersistence.CreateInstitution
import hmda.api.protocol.processing.InstitutionProtocol
import hmda.model.fi.Institution
import spray.json._

import scala.io.Source

case class DemoData(file: File) extends InstitutionProtocol {
  val institutions = {
    val instFile = Source.fromFile(file)
    val json = instFile.getLines().toIterable.head.toString
    json.parseJson.convertTo[Set[Institution]]
  }

  def loadData(system: ActorSystem): Unit = {
    Thread.sleep(500)
    loadInstitutions(system)
  }

  def loadInstitutions(system: ActorSystem): Unit = {
    val institutionsActor = system.actorSelection("/user/institutions")
    institutions.foreach(i => institutionsActor ! CreateInstitution(i))
  }
}
