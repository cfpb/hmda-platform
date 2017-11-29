package hmda.loader.panel

import java.io.File

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Sink, Tcp }
import org.slf4j.LoggerFactory
import hmda.api.util.FlowUtils

import scala.concurrent.duration._

object PanelCsvLoader extends FlowUtils {
  val httpTimeout = config.getInt("hmda.httpTimeout")
  val duration = httpTimeout.seconds
  override implicit val system: ActorSystem = ActorSystem("hmda-loader")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("hmda")

  val host = config.getString("hmda.panel.tcp.host")
  val port = config.getInt("hmda.panel.tcp.port")

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      exitSys(log, "No file argument provided", 1)
    }

    val file = new File(args(0))
    if (!file.exists() || !file.isFile) {
      exitSys(log, "File does not exist", 2)
    }

    val connectionFlow = Tcp().outgoingConnection(host, port)

    val source = FileIO.fromPath(file.toPath)

    source
      .via(connectionFlow)
      .runWith(Sink.last)
      .onComplete(_ => system.terminate())

  }
}

