package hmda.institution.loader

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import hmda.api.http.FlowUtils
import hmda.parser.institution.InstitutionCsvParser
import io.circe.syntax._
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object InstitutionLoader extends App with FlowUtils {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem             = ActorSystem()
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext            = system.dispatcher

  override def parallelism = config.getInt("hmda.loader.parallelism")
  val url                  = config.getString("hmda.loader.institution.url")

  println("URL: " + url)

  val log = LoggerFactory.getLogger("institutions-loader")

  if (args.length < 1) {
    log.error("No file argument provided")
    sys.exit(1)
  }

  val file = new File(args(0))
  if (!file.exists() || !file.isFile) {
    log.error("File does not exist")
    sys.exit(2)
  }

  val postOrPut = args(1)
  log.info(s"Running in $postOrPut mode")
  val source = FileIO.fromPath(file.toPath)

  def request(json: String) = {
    var r = HttpRequest()
    postOrPut match {
      case "put" =>
        r = HttpRequest(uri = s"$url", method = HttpMethods.PUT)
          .withEntity(ContentTypes.`application/json`, ByteString(json))
      case _ =>
        r = HttpRequest(uri = s"$url", method = HttpMethods.POST)
          .withEntity(ContentTypes.`application/json`, ByteString(json))
    }
    Http().singleRequest(r)
      .onComplete {
        case Success(res) => if(res.status == StatusCodes.BadRequest) log.info(res.toString())
        case Failure(_)   => sys.error("something wrong")
      }
    r
  }

  source
    .via(framing)
    .drop(1)
    .map(line => line.utf8String)
    .map(x => InstitutionCsvParser(x))
    .map(i => request(i.asJson.noSpaces))
    .mapAsync(parallelism) { req =>
      Http().singleRequest(req)
    }
    .runWith(Sink.last)
    .onComplete(_ => system.terminate())

}
