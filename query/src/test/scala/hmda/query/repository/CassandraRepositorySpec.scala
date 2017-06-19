package hmda.query.repository

import akka.actor.{ ActorSystem, Scheduler }
import akka.stream.ActorMaterializer
import com.datastax.driver.core.{ Cluster, Session }
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext

trait CassandraRepositorySpec[A] extends WordSpec with MustMatchers with BeforeAndAfterAll with CassandraRepository[A] {

  EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L)
  val cluster: Cluster = EmbeddedCassandraServerHelper.getCluster
  override val session: Session = cluster.connect()

  override def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

  override implicit def materializer: ActorMaterializer = ActorMaterializer()

  override implicit val ec: ExecutionContext = system.dispatcher
  override implicit val scheduler: Scheduler = system.scheduler

  override implicit def system: ActorSystem = ActorSystem()
}
