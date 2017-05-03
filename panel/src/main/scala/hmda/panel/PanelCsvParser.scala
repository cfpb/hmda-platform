package hmda.panel

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.util.{ ByteString, Timeout }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Flow, Framing, Sink }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, _ }
import com.typesafe.config.ConfigFactory
import hmda.api.protocol.admin.WriteInstitutionProtocol

import scala.concurrent.duration._
import hmda.parser.fi.InstitutionParser
import hmda.query.repository.institutions.InstitutionComponent
import org.slf4j.LoggerFactory

import scala.concurrent.Await
import spray.json._

object PanelCsvParser extends InstitutionComponent with WriteInstitutionProtocol {
  implicit val system: ActorSystem = ActorSystem("hmda")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5.second)
  implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("hmda")

  val repository = new InstitutionRepository(hmda.query.DbConfiguration.config)

  val config = ConfigFactory.load()
  val host = config.getString("hmda.http.adminHost")
  val port = config.getInt("hmda.http.adminPort")

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      println("ERROR: Please provide institutions file")
      sys.exit(1)
    }

    println("Cleaning DB...")
    Await.result(repository.dropSchema(), 5.seconds)
    println("Creating new schema...")
    Await.result(repository.createSchema(), 5.seconds)

    val source = FileIO.fromPath(new File(args(0)).toPath)

    val connectionFlow = Http().outgoingConnection(host, port)

    source
      .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1024, allowTruncation = true))
      .drop(1)
      .via(byte2StringFlow)
      .via(stringToHttpFlow)
      .via(connectionFlow)
      .runWith(Sink.head)
  }

  private def stringToHttpFlow: Flow[String, HttpRequest, NotUsed] =
    Flow[String]
      .map(x => {
        val payload = ByteString(InstitutionParser(x).toJson.toString)
        HttpRequest(
          HttpMethods.POST,
          uri = "/institutions",
          entity = HttpEntity(MediaTypes.`application/json`, payload)
        )
      })

  private def byte2StringFlow: Flow[ByteString, String, NotUsed] =
    Flow[ByteString].map(bs => bs.utf8String)
}
