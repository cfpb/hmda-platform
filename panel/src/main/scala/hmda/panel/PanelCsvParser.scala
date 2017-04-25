package hmda.panel

import java.io.File

import akka.NotUsed
import akka.actor.{ ActorRef, ActorSystem }
import akka.util.{ ByteString, Timeout }
import akka.pattern.ask
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ BroadcastHub, FileIO, Flow, Framing, Keep, Sink, Source }
import hmda.model.fi.{ Filing, NotStarted }
import hmda.model.institution.Institution
import hmda.persistence.messages.CommonMessages._

import scala.concurrent.duration._
import hmda.parser.fi.InstitutionParser
import hmda.persistence.HmdaSupervisor._
import hmda.persistence.institutions.FilingPersistence.CreateFiling
import hmda.persistence.institutions.{ FilingPersistence, InstitutionPersistence }
import hmda.persistence.institutions.InstitutionPersistence.CreateInstitution
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.DbConfiguration._
import hmda.query.repository.institutions.InstitutionComponent
import org.slf4j.LoggerFactory

import scala.concurrent.Await

object PanelCsvParser extends InstitutionComponent {
  implicit val system: ActorSystem = ActorSystem("hmda")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5.second)
  implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("hmda")

  val supervisor = createSupervisor(system)
  val institutionPersistenceF = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]

  val repository = new InstitutionRepository(config)

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      println("ERROR: Please provide institutions file")
      sys.exit(1)
    }

    println("Cleaning DB...")
    Await.result(repository.dropSchema(), 5.seconds)
    println("Creating new schema...")
    Await.result(repository.createSchema(), 5.seconds)
    val institutionPersistence = Await.result(institutionPersistenceF, 5.seconds)

    val source = FileIO.fromPath(new File(args(0)).toPath)

    val graph = source
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1024, allowTruncation = true))
      .drop(1)
      .via(byte2StringFlow)
      .via(parseInstitutions)
      .toMat(BroadcastHub.sink(bufferSize = 1024))(Keep.right)

    val fromGraph: Source[Institution, NotUsed] = graph.run()

    println("Reading file...")

    fromGraph
      .map(i => CreateInstitution(i))
      .runWith(Sink.actorRef(institutionPersistence, Shutdown))

    fromGraph
      .runForeach(i => {
        val f = Filing(i.activityYear.toString, i.id)
        for {
          s <- (supervisor ? FindFilings(FilingPersistence.name, i.id)).mapTo[ActorRef]
        } yield s ! CreateFiling(f)
      })
  }

  private def parseInstitutions: Flow[String, Institution, NotUsed] =
    Flow[String]
      .map(x => InstitutionParser(x))

  private def byte2StringFlow: Flow[ByteString, String, NotUsed] =
    Flow[ByteString].map(bs => bs.utf8String)

}
