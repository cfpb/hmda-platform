package hmda.query.repository.institutions

import akka.stream.scaladsl.Source
import com.datastax.driver.core.{ Cluster, Session }
import hmda.model.institution.{ Agency, InstitutionGenerators }
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import hmda.query.repository.institutions.InstitutionConverter._
import scala.concurrent.duration._
import scala.concurrent.Await

class InstitutionCassandraRepositorySpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  var cluster: Cluster = _
  var session: Session = _

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L)
    cluster = EmbeddedCassandraServerHelper.getCluster()
    session = cluster.connect()
    InstitutionCassandraRepository.createKeyspace()
  }

  override def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

  "Institutions in Cassandra" must {
    "Drop the table if it exists, create it again and populate it with some data that can be read back" in {
      InstitutionCassandraRepository.dropTable()
      InstitutionCassandraRepository.createTable()
      val institutions = List(
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB)),
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB)),
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB))
      )
      val source = Source.fromIterator(() => institutions.toIterator)
      val insertedF = InstitutionCassandraRepository.insertData(source)
      Await.result(insertedF, 5.seconds)
      val read = InstitutionCassandraRepository.readData(20)
      read.map { r =>
        r.map(x => x.getInt("agency") mustBe Agency.CFPB.value)
        r.seq.size mustBe 3
      }
    }
  }

}
