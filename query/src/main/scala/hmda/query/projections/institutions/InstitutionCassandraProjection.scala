package hmda.query.projections.institutions

import akka.NotUsed
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import akka.stream.alpakka.cassandra.scaladsl.CassandraSink
import akka.stream.scaladsl.Source
import hmda.persistence.messages.events.institutions.InstitutionEvents.{ InstitutionCreated, InstitutionModified }
import hmda.persistence.processing.HmdaQuery._
import hmda.query.model.institutions.InstitutionQuery
import hmda.query.repository.institutions.InstitutionCassandraRepository
import hmda.query.repository.institutions.InstitutionConverter._

class InstitutionCassandraProjection(sys: ActorSystem, mat: ActorMaterializer) extends InstitutionCassandraRepository {

  def startUp(): Unit = {
    createKeyspace()
    createTable()

    val source: Source[InstitutionQuery, NotUsed] = liveEvents("institutions").map {
      case InstitutionCreated(i) => i
      case InstitutionModified(i) => i
    }
    val sink = CassandraSink[InstitutionQuery](parallelism = 2, preparedStatement, statementBinder)
    source.to(sink).run()
  }

  override implicit def system: ActorSystem = sys

  override implicit def materializer: ActorMaterializer = mat
}
