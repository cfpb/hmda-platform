package hmda.panel

import akka.actor.{ ActorRef, ActorSystem }
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import hmda.parser.fi.InstitutionParser
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import scala.concurrent.ExecutionContext.Implicits.global

import scala.io.Source
import scala.util.Try

object PanelCsvParser extends App {
  val system: ActorSystem = ActorSystem("parser")
  implicit val timeout: Timeout = Timeout(5.second)

  val institutionPersistence = InstitutionPersistence.createInstitutions(system)

  var file: Source = _
  if (Try(Source.fromFile(args(0))).isSuccess) {
    file = Source.fromFile(args(0))
  } else {
    println("\nWARNING: Unable to read file input.")
    System.exit(1)
  }

  val lines = file.getLines().toList.tail

  for (line <- lines) {
    institutionPersistence ! CreateInstitution(InstitutionParser(line))
  }
}
