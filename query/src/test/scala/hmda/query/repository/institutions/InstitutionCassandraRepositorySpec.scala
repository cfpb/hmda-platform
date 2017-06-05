package hmda.query.repository.institutions

import akka.stream.scaladsl.Source
import com.datastax.driver.core.{ Cluster, Session }
import hmda.model.institution.{ Agency, InstitutionGenerators }
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }
import hmda.query.repository.institutions.InstitutionConverter._

class InstitutionCassandraRepositorySpec extends AsyncWordSpec with MustMatchers with BeforeAndAfterAll {

  var cluster: Cluster = _
  var session: Session = _

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L)
    cluster = EmbeddedCassandraServerHelper.getCluster()
    session = cluster.connect()
    loadData()
  }

  override def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

  def loadData(): Unit = {
    val dataLoader = new CQLDataLoader(session)
    dataLoader.load(new ClassPathCQLDataSet("simple.cql", "hmda_query"))
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
      val inserted = InstitutionCassandraRepository.insertData(source)
      val read = InstitutionCassandraRepository.readData(20)
      for {
        _ <- inserted
        r <- read
      } yield {
        r.map(x => x.getInt("agency") mustBe Agency.CFPB.value)
        r.size mustBe 3
      }
    }
  }

}
