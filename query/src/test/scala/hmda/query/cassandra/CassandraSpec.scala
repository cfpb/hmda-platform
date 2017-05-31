package hmda.query.cassandra

import com.datastax.driver.core.{ Cluster, Session }
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import org.cassandraunit.CQLDataLoader
import org.cassandraunit.dataset.cql.ClassPathCQLDataSet
import org.cassandraunit.utils.EmbeddedCassandraServerHelper

class CassandraSpec extends WordSpec with MustMatchers with BeforeAndAfterAll {

  var cluster: Cluster = _
  var session: Session = _

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L)
    cluster = EmbeddedCassandraServerHelper.getCluster()
    session = cluster.connect()

    val dataLoader = new CQLDataLoader(session)
    dataLoader.load(new ClassPathCQLDataSet("simple.cql", "mykeyspace"))
  }

  override def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

  "Cassandra" must {
    "Select from table" in {
      val resultSet = session.execute("select * from myTable where id = 'myKey01'")
      resultSet.iterator().next().getString("value") mustBe "myValue01"
    }
  }

}
