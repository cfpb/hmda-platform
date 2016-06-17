package hmda.api.util

import java.io.File

import akka.actor.ActorSystem
import hmda.api.model.processing.Institutions
import hmda.api.processing.submission.InstitutionsFiling.CreateInstitution
import hmda.api.protocol.processing.ProcessingProtocol
import spray.json._

import scala.io.Source

object TestData extends ProcessingProtocol {

  val institutions = {
    val file = new File("api/src/test/resources/institutions.json")
    val instFile = Source.fromFile(file)
    val json = instFile.getLines().toIterable.head.toString
    json.parseJson.convertTo[Institutions].institutions
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
