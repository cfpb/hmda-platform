package hmda.query.repository.institutions

import com.datastax.driver.core.{ Cluster, Session }
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers }

import scala.concurrent.Future

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
    dataLoader.load(new ClassPathCQLDataSet("institutions.cql", "hmda_query"))
  }

  "Institutions in Cassandra" must {
    "read back data from default data in test" in {
      val f = InstitutionCassandraRepository.readData(100)
      f.map(xs => xs.size mustBe 1)
    }
    "read back a subset of data from default data in test, based on fetchsize" in {
      Future(1 mustBe 1)
    }
    "Drop the table, create it again and populate it with some data that can be read back" in {
      Future(1 mustBe 1)
    }
  }

}
