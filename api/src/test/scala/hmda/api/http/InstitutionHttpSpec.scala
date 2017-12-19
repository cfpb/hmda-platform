package hmda.api.http

import java.io.File

import akka.actor.ActorRef
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import hmda.api.RequestHeaderUtils
import hmda.persistence.HmdaSupervisor
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.HmdaQuerySupervisor
import org.iq80.leveldb.util.FileUtils
import org.scalatest._

import scala.concurrent.duration._
import akka.pattern.ask
import hmda.validation.stats.ValidationStats

import scala.concurrent._

trait InstitutionHttpSpec extends MustMatchers with BeforeAndAfterAll with RequestHeaderUtils with InstitutionsHttpApi with FileUploadUtils with ScalatestRouteTest { suite: Suite =>
  val configuration: Config = ConfigFactory.load()

  val validationStats = ValidationStats.createValidationStats(system)
  val supervisor = HmdaSupervisor.createSupervisor(system, validationStats)

  val querySupervisor = HmdaQuerySupervisor.createQuerySupervisor(system)

  val duration = 10.seconds
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(duration)

  implicit val flowParallelism: Int = configuration.getInt("hmda.actor-flow-parallelism")

  implicit val ec = system.dispatcher

  override def beforeAll(): Unit = {
    super.beforeAll()
    val institutionsActorF = (supervisor ? FindActorByName(InstitutionPersistence.name)).mapTo[ActorRef]
    val institutionsActor = Await.result(institutionsActorF, duration)
    DemoData.loadTestData(system, institutionsActor)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
    val config = ConfigFactory.load()
    val snapshotStore = new File(config.getString("akka.persistence.snapshot-store.local.dir"))
    FileUtils.deleteRecursively(snapshotStore)
  }

}
