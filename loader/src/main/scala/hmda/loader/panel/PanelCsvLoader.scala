package hmda.panel

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.model.HttpResponse
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{ FileIO, Framing, Sink }
import akka.util.ByteString
import hmda.loader.http.InstitutionFlowUtils
import org.slf4j.LoggerFactory
import hmda.api.util.FlowUtils
import scala.concurrent.duration._

object PanelCsvLoader extends FlowUtils with InstitutionFlowUtils {
  val httpTimeout = config.getInt("hmda.httpTimeout")
  val adminUrl = config.getString("hmda.adminUrl")
  val url = s"$adminUrl/institutions"
  val duration = httpTimeout.seconds
  override implicit val system: ActorSystem = ActorSystem("hmda-loader")
  override implicit val materializer = ActorMaterializer()
  override implicit val ec = system.dispatcher
  val log = LoggerFactory.getLogger("hmda")

  def main(args: Array[String]): Unit = {

    if (args.length < 1) {
      exitSys(log, "No file argument provided", 1)
    }

    val file = new File(args(0))
    if (!file.exists() || !file.isFile) {
      exitSys(log, "File does not exist", 2)
    }

    sendGetRequest("delete", url).map(_ => {
      val response = sendGetRequest("create", url)

      val source = FileIO.fromPath(file.toPath)

      response.onComplete(s =>
        if (s.getOrElse("").equals("InstitutionSchemaCreated()")) {
          log.info(s.get)
          val completedF = source
            .via(Framing.delimiter(ByteString("\n"), maximumFrameLength = 1024, allowTruncation = true))
            .drop(1)
            .via(byte2StringFlow)
            .via(institutionStringToHttpFlow(url))
            .via(singleConnectionFlow)
            .runWith(Sink.foreach[HttpResponse](elem => log.info(elem.entity.toString)))

          completedF.onComplete(result => {
            if (result.isSuccess)
              sys.exit(0)
            else {
              exitSys(log, "Error while processing institutions", 4)
            }
          })
        } else {
          exitSys(log, "Error creating institutions schema", 3)
        })
    })
  }

}

