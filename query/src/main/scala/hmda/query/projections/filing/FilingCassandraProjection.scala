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
import hmda.query.model.filing.LoanApplicationRegisterQuery
import hmda.query.repository.filing.LarConverter._

class FilingCassandraProjection(sys: ActorSystem, mat: ActorMaterializer) extends FilingCassandraRepository {

  def startUp(): Unit = {
    createKeyspace()
    createTable()

    val source: Source[LoanApplicationRegister, NotUsed] = liveEvents("HmdaFiling-2017").map {
      case LarValidated(lar, _) => lar
    }
    val sink = CassandraSink[LoanApplicationRegisterQuery](parallelism = 2, preparedStatement, statementBinder)
    source
      .map { lar =>
        val queryLar = toLoanApplicationRegisterQuery(lar)
        queryLar.copy(institutionId = queryLar.id, period = "2017")
      }
      .to(sink).run()
  }

  override implicit def system: ActorSystem = sys

  override implicit def materializer: ActorMaterializer = mat
}
