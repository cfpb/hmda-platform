package hmda.query.repository.institutions

import akka.Done
import akka.actor.{ ActorSystem, Scheduler }
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Source
import com.datastax.driver.core.{ Cluster, Session }
import hmda.model.institution.{ Agency, InstitutionGenerators }
import hmda.persistence.model.AsyncActorSpec
import org.cassandraunit.utils.EmbeddedCassandraServerHelper
import org.scalatest.{ AsyncWordSpec, BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.query.repository.institutions.InstitutionConverter._

import scala.concurrent.duration._
import scala.concurrent.{ Await, ExecutionContext, Future }

class InstitutionCassandraRepositorySpec extends WordSpec with MustMatchers with BeforeAndAfterAll with InstitutionCassandraRepository {

  var cluster: Cluster = _
  var session: Session = _

  override def beforeAll(): Unit = {
    EmbeddedCassandraServerHelper.startEmbeddedCassandra(20000L)
    cluster = EmbeddedCassandraServerHelper.getCluster()
    session = cluster.connect()
    createKeyspace()
  }

  override def afterAll(): Unit = {
    EmbeddedCassandraServerHelper.cleanEmbeddedCassandra()
  }

  "Institutions in Cassandra" must {
    "Drop the table if it exists, create it again and populate it with some data that can be read back" in {
      dropTable()
      createTable()

      val institutions = List(
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB)),
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB)),
        toInstitutionQuery(InstitutionGenerators.sampleInstitution.copy(agency = Agency.CFPB))
      )

      val source = Source.fromIterator(() => institutions.toIterator)
      insertData(source)
      val read = readData(20)
      read.map { r =>
        r.map(x => x.getInt("agency") mustBe Agency.CFPB.value)
        r.seq.size mustBe 3
      }
    }
  }

  override implicit def materializer: ActorMaterializer = ActorMaterializer()

  override implicit val ec: ExecutionContext = system.dispatcher
  override implicit val scheduler: Scheduler = system.scheduler

  override implicit def system: ActorSystem = ActorSystem()
}
