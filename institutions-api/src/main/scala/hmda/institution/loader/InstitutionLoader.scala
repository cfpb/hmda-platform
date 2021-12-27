package hmda.institution.loader

import java.io.File

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.stream.Materializer
import akka.stream.scaladsl.{FileIO, Sink}
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import hmda.api.http.FlowUtils
import hmda.parser.institution.InstitutionCsvParser
import io.circe.syntax._
import org.slf4j.LoggerFactory
import scala.concurrent.duration._

import scala.concurrent.ExecutionContext
import scala.util.{ Failure, Success }
// $COVERAGE-OFF$
object InstitutionLoader extends App {

  val config          = ConfigFactory.load()
  var createdCount    = 0
  var acceptedCount   = 0
  var badRequestCount = 0
  var totalCount      = 0
  var notFoundCount   = 0
  var count           = 0

  implicit val system: ActorSystem        = ActorSystem()
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContext       = system.dispatcher

  def parallelism = config.getInt("hmda.loader.parallelism")
  val url         = config.getString("hmda.loader.institution.url")

  val bankFilter = ConfigFactory.load("application.conf").getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")

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

  def request(json: String) =
    postOrPut match {
      case "put" =>
        HttpRequest(uri = s"$url", method = HttpMethods.PUT)
          .withEntity(ContentTypes.`application/json`, ByteString(json))
      case _ =>
        HttpRequest(uri = s"$url", method = HttpMethods.POST)
          .withEntity(ContentTypes.`application/json`, ByteString(json))
    }

  source
    .via(FlowUtils.framing)
//    .drop(1)
    .map(line => line.utf8String)
    .map(x => InstitutionCsvParser(x))
    .filter { i =>
      count += 1
      !bankFilterList.contains(i.LEI)
    }
    .map(i => request(i.asJson.noSpaces))
    .mapAsync(parallelism)(req => Http().singleRequest(req))
    .map { res =>
      val response = res.entity.toStrict(100.seconds)
      res.status match {
        case StatusCodes.BadRequest =>
          response.map(_.data.utf8String).onComplete {
            case Success(data) => log.info(f"bad request response: [$data]")
            case Failure(e) => log.error("failed to get bad request response.", e)
          }
          badRequestCount += 1
        case StatusCodes.Created  => createdCount += 1
        case StatusCodes.Accepted => acceptedCount += 1
        case StatusCodes.NotFound =>
          log.info(res.toString())
          notFoundCount += 1
        case _ => log.info(res.toString())
      }
      totalCount += 1
    }
    .runWith(Sink.last)
    .onComplete { _ =>
      log.info(s"${totalCount} institutions attempts")
      if (totalCount != 0) {
        log.info(s"${createdCount} institutions created (${createdCount * 100 / totalCount}%)")
        log.info(s"${acceptedCount} institutions accepted (${acceptedCount * 100 / totalCount}%)")
        log.info(s"${badRequestCount} institutions failed (${badRequestCount * 100 / totalCount}%)")
        log.info(s"${notFoundCount} institutions not found (${notFoundCount * 100 / totalCount}%)")
        log.info(s"${count - totalCount} institutions filtered out (${(count - totalCount) * 100 / totalCount}%)")
      }
      system.terminate()
    }

}
// $COVERAGE-ON$