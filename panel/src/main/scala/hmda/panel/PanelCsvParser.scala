package hmda.panel

import akka.actor.{ ActorRef, ActorSystem }
import akka.util.Timeout
import akka.pattern.ask
import com.typesafe.config.ConfigFactory
import hmda.future.util.FutureRetry._

import scala.concurrent.duration._
import hmda.parser.fi.InstitutionParser
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.messages.events.institutions.InstitutionEvents.InstitutionSchemaCreated
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.HmdaQuerySupervisor._
import hmda.query.projections.institutions.InstitutionDBProjection.CreateSchema
import hmda.query.view.institutions.InstitutionView
import hmda.query.view.messages.CommonViewMessages.GetProjectionActorRef
import org.slf4j.LoggerFactory

import scala.io.Source
import scala.util.Try

object PanelCsvParser extends App {
  val config = ConfigFactory.load()

  val system: ActorSystem = ActorSystem("hmda")
  implicit val timeout: Timeout = Timeout(5.second)
  implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("hmda")

  val supervisor = createSupervisor(system)
  val querySupervisor = createQuerySupervisor(system)
  val institutionPersistenceF = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
  val institutionViewF = (querySupervisor ? FindActorByName(InstitutionView.name))
    .mapTo[ActorRef]

  var file: Source = _
  if (Try(Source.fromFile(args(0))).isSuccess) {
    file = Source.fromFile(args(0))
  } else {
    println("\nWARNING: Unable to read file input.")
    System.exit(1)
  }
  val lines = file.getLines().toList.tail

  implicit val scheduler = system.scheduler
  val retries = List(200.millis, 200.millis, 500.millis, 1.seconds, 2.seconds)
  for {
    i <- institutionViewF
    q <- retry((i ? GetProjectionActorRef).mapTo[ActorRef], retries, 10, 300.millis)
    s <- (q ? CreateSchema).mapTo[InstitutionSchemaCreated]
    p <- institutionPersistenceF
  } yield {
    for (line <- lines) {
      p ! CreateInstitution(InstitutionParser(line))
    }
  }
}
