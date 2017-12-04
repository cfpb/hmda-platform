package hmda.loader.apor

import java.io.File

import akka.actor.{ActorPath, ActorRef, ActorSystem}
import akka.pattern.ask
import akka.cluster.client.{ClusterClient, ClusterClientSettings}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.FileIO
import akka.util.Timeout
import hmda.api.util.FlowUtils
import hmda.model.apor.{FixedRate, RateType, VariableRate}
import hmda.parser.apor.APORCsvParser
import hmda.persistence.HmdaSupervisor.FindAPORPersistence
import hmda.persistence.apor.HmdaAPORPersistence
import hmda.persistence.messages.commands.apor.APORCommands.CreateApor
import hmda.persistence.messages.events.apor.APOREvents.AporCreated
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object APORLoader extends FlowUtils {

  override implicit val system: ActorSystem = ActorSystem("hmda-cluster-client")
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = system.dispatcher

  val hmdaClusterName = config.getString("hmda.clusterName")
  val hmdaClusterIP = config.getString("hmda.lar.host")
  val hmdaClusterPort = config.getInt("hmda.lar.port")
  val actorTimeout = config.getInt("hmda.actorTimeout")

  implicit val timeout = Timeout(actorTimeout.seconds)

  val log = LoggerFactory.getLogger("hmda-apor-loader")

  val initialContacts = Set(
    ActorPath.fromString(s"akka.tcp://$hmdaClusterName@$hmdaClusterIP:$hmdaClusterPort/system/receptionist")
  )

  val settings = ClusterClientSettings(system)
    .withInitialContacts(initialContacts)

  val clusterClient = system.actorOf(ClusterClient.props(settings), "hmda-apor-loader")

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      exitSys(log, "Please provide file with APOR values and rate type (Fixed or Variable)", 1)
    }

    val file = new File(args(0))
    val rateTypeStr = args(1)

    val fAporPersistence = (clusterClient ? ClusterClient.Send(
      "/user/supervisor/singleton",
      FindAPORPersistence(HmdaAPORPersistence.name),
      localAffinity = true
    )).mapTo[ActorRef]

    val source = FileIO.fromPath(file.toPath)

    source
      .drop(1)
      .map(s => s.utf8String)
      .map(s => APORCsvParser(s))
      .mapAsync(parallelism) { apor =>
        for {
          a <- fAporPersistence
          created <- (a ? CreateApor(apor, rateType(rateTypeStr))).mapTo[AporCreated]
        } yield created
      }
  }

  private def rateType(rateTypeStr: String): RateType = {
    rateTypeStr match {
      case "Fixed" => FixedRate
      case "Variable" => VariableRate
      case _ => throw new RuntimeException("Invalid Rate Type")
    }
  }

}
