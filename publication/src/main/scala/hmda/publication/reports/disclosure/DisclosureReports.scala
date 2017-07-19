package hmda.publication.reports.disclosure

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.publication.reports.protocol.disclosure.D51Protocol._
import hmda.query.repository.filing.FilingCassandraRepository

import scala.concurrent.Future
import spray.json._

class DisclosureReports(val sys: ActorSystem, val mat: ActorMaterializer) extends FilingCassandraRepository {

  override implicit def system: ActorSystem = sys
  override implicit def materializer: ActorMaterializer = mat

  val larSource = readData(1000)

  def generateReports(fipsCode: Int, respId: String): Future[Unit] = {
    val d51F = D51.generate(larSource, fipsCode, respId)
    d51F.map { d51 =>
      println(d51.toJson.prettyPrint)
    }
  }

}
