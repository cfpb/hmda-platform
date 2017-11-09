package hmda.query.projections.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.query.repository.filing.FilingCassandraRepository

class SubmissionCassandraProjection(sys: ActorSystem, mat: ActorMaterializer) extends FilingCassandraRepository {

  def startUp(): Unit = {
    createKeyspace()
    createTable()
  }

  override implicit def system: ActorSystem = sys

  override implicit def materializer: ActorMaterializer = mat
}
