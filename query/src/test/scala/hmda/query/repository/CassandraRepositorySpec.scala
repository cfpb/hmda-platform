package hmda.query.repository

import com.datastax.driver.core.{ Cluster, Session }
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

trait CassandraRepositorySpec[A] extends WordSpec with MustMatchers with BeforeAndAfterAll with CassandraRepository[A] {

  EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L)
  val cluster: Cluster = EmbeddedCassandraServerHelper.getCluster
  override val session: Session = cluster.connect()

  override def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

}
