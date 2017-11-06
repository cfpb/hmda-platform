package hmda.query.projections.filing

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.query.repository.filing.FilingCassandraRepository
import hmda.persistence.processing.HmdaQuery._

class SubmissionCassandraProjection(sys: ActorSystem, mat: ActorMaterializer) extends FilingCassandraRepository {

  def startUp(): Unit = {
    createKeyspace()
    createTable()
    //projectLarData()
  }

  def projectLarData(): Unit = {
    val source: Source[LoanApplicationRegister, NotUsed] = liveEvents("HmdaFiling-2017").map {
      case LarValidated(lar, _) => lar
    }
    val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)
    source
      .map { e => println(e); e }
      .to(sink).run()
  }

  override implicit def system: ActorSystem = sys

  override implicit def materializer: ActorMaterializer = mat
}
