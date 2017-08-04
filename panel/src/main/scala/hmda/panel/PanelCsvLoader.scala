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
import hmda.api.protocol.admin.WriteInstitutionProtocol

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
      exitSys("No file argument provided", 1)
    }

    val file = new File(args(0))
    if (!file.exists() || !file.isFile) {
      exitSys("File does not exist", 2)
    }

    sendRequest("delete").map(_ => {
      val response = sendRequest("create")

      val source = FileIO.fromPath(file.toPath)
      val connectionFlow = Http().outgoingConnection(host, port)

      response.onComplete(s =>
        if (s.getOrElse("").equals("InstitutionSchemaCreated()")) {
          log.info(s.get)
          val completedF = source
            .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1024, allowTruncation = true))
            .drop(1)
            .via(byte2StringFlow)
            .via(stringToHttpFlow)
            .via(connectionFlow)
            .runWith(Sink.foreach[HttpResponse](elem => log.info(elem.entity.toString)))

          completedF.onComplete(result => {
            if (result.isSuccess)
              sys.exit(0)
            else {
              exitSys("Error while processing institutions", 4)
            }
          })
        } else {
          exitSys("Error creating institutions schema", 3)
        })
    })
  }

  private def singleConnectionFlow: Flow[HttpRequest, HttpResponse, NotUsed] =
    Flow[HttpRequest]
    .map(r => {
      Http().singleRequest(r).
    })

  private def stringToHttpFlow: Flow[String, HttpRequest, NotUsed] =
    Flow[String]
      .map(x => {
        val payload = ByteString(InstitutionParser(x).toJson.toString)
        HttpRequest(
          HttpMethods.POST,
          entity = HttpEntity(MediaTypes.`application/json`, payload)
        )
      })

  private def byte2StringFlow: Flow[ByteString, String, NotUsed] =
    Flow[ByteString].map(bs => bs.utf8String)

  private def sendRequest(req: String) = {
    val request = HttpRequest(HttpMethods.GET, uri = s"http://$host:$port/institutions/$req")
    for {
      response <- Http().singleRequest(request)
      content <- Unmarshal(response.entity).to[String]
    } yield content
  }

  private def exitSys(errorMessage: String, code: Int) = {
    log.error(errorMessage)
    Thread.sleep(100)
    sys.exit(code)
  }
}
