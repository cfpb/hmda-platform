package hmda.query.projections.filing

import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import hmda.query.repository.filing.LoanApplicationRegisterCassandraRepository

class SubmissionCassandraProjection(sys: ActorSystem, mat: ActorMaterializer) extends LoanApplicationRegisterCassandraRepository {

  def startUp(): Unit = {
    createKeyspace()
    createTable()
  }

  override implicit def system: ActorSystem = sys

  override implicit def materializer: ActorMaterializer = mat
}
