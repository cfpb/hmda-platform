package hmda.query.projections.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.query.repository.filing.TransmittalSheetCassandraRepository

class TransmittalSheetCassandraProjection(sys: ActorSystem, mat: ActorMaterializer) extends TransmittalSheetCassandraRepository {


  def startup(): Unit = {
    createKeyspace()
    createTable()
  }

  override implicit def system: ActorSystem = sys

  override implicit def materializer: ActorMaterializer = mat
}
