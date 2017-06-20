package hmda.query.projections.filing

import akka.NotUsed
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.Source
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.persistence.messages.events.processing.CommonHmdaValidatorEvents.LarValidated
import hmda.query.repository.filing.FilingCassandraRepository
import hmda.persistence.processing.HmdaQuery._

class FIlingCassandraProjection extends FilingCassandraRepository {

  def startUp(): Unit = {
    createKeyspace()
    createTable()

    val source: Source[LoanApplicationRegister, NotUsed] = liveEvents("HmdaFiling-2017").map {
      case LarValidated(lar, _) => lar
    }
    val sink = CassandraSink[LoanApplicationRegister](parallelism = 2, preparedStatement, statementBinder)
    source.runWith(sink)
  }

}
