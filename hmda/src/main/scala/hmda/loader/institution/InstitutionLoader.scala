package hmda.loader.institution

import java.io.File

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{FileIO, Framing, Sink, Source}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import hmda.api.http.FlowUtils
import hmda.parser.institution.InstitutionCsvParser
import org.slf4j.LoggerFactory

import scala.concurrent.ExecutionContext

object InstitutionLoader extends App with FlowUtils {

  val config = ConfigFactory.load()

  override implicit val system: ActorSystem = ActorSystem()
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override implicit val ec: ExecutionContext = system.dispatcher

  override def parallelism = config.getInt("hmda.loader.parallelism")

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

  val source = FileIO.fromPath(file.toPath)

  source
    .via(framing)
    .map(line => line.utf8String)
    .map(i => InstitutionCsvParser(i))
    .runWith(Sink.foreach(println))
    .onComplete(_ => system.terminate())

}
