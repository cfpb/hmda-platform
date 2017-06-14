package hmda.panel

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.util.{ ByteString, Timeout }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Flow, Framing, Sink }
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ HttpEntity, _ }
import akka.http.scaladsl.unmarshalling.Unmarshal
import com.typesafe.config.ConfigFactory
import hmda.apiModel.protocol.admin.WriteInstitutionProtocol

import scala.concurrent.duration._
import hmda.parser.fi.InstitutionParser
import org.slf4j.LoggerFactory
import spray.json._

object PanelCsvLoader extends WriteInstitutionProtocol {
  implicit val system: ActorSystem = ActorSystem("hmda")
  implicit val materializer = ActorMaterializer()
  implicit val timeout: Timeout = Timeout(5.second)
  implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("hmda")

  val config = ConfigFactory.load()
  val host = config.getString("hmda.adminHost")
  val port = config.getInt("hmda.adminPort")

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      log.error("ERROR: Please provide institutions file")
      sys.exit(1)
    }

    log.info("Cleaning DB...")
    val deleteRequest = HttpRequest(HttpMethods.GET, uri = s"http://$host:$port/institutions/delete")
    val deleteResponse = for {
      response <- Http().singleRequest(deleteRequest)
      content <- Unmarshal(response.entity).to[String]
    } yield content

    val source = FileIO.fromPath(new File(args(0)).toPath)

    val connectionFlow = Http().outgoingConnection(host, port)

    deleteResponse.onComplete(s =>
      if(s.getOrElse("").equals("InstitutionSchemaDeleted()")) {
        source
          .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1024, allowTruncation = true))
          .drop(1)
          .via(byte2StringFlow)
          .via(stringToHttpFlow)
          .via(connectionFlow)
          .runWith(Sink.head)
      } else {
        log.error("Error deleting institutions schema")
        sys.exit(1)
      })
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
