package hmda.publication.reports.aggregate

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.query.repository.filing.FilingCassandraRepository
import hmda.publication.reports.protocol.aggregate.A5XProtocol._

import scala.concurrent.Future
import scala.concurrent.duration._

import spray.json._

class AggregateReports(val sys: ActorSystem, val mat: ActorMaterializer) extends FilingCassandraRepository {

  override implicit def system: ActorSystem = sys
  override implicit def materializer: ActorMaterializer = mat
  val config = ConfigFactory.load()
  val duration = config.getInt("hmda.actor-lookup-timeout")
  implicit val timeout = Timeout(duration.seconds)

  val larSource = readData(1000)

  def generateReports(fipsCode: Int): Future[Unit] = {
    val a52F = A52.generate(larSource, fipsCode)
    a52F.map { a52 =>
      println(a52.toJson.prettyPrint)
    }

    // A53.generate(larSource, fipsCode)
  }
}
