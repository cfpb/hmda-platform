package hmda.api.http

import java.io.File

import akka.actor.ActorSystem
import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, Multipart }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.{ Config, ConfigFactory }
import hmda.api.RequestHeaderUtils
import hmda.persistence.HmdaSupervisor
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import hmda.query.HmdaQuerySupervisor
import hmda.query.view.institutions.InstitutionView
import hmda.validation.ValidationStats
import org.iq80.leveldb.util.FileUtils
import org.scalatest._

import scala.concurrent.duration._

trait InstitutionHttpSpec extends MustMatchers with BeforeAndAfterAll with RequestHeaderUtils with InstitutionsHttpApi with ScalatestRouteTest { suite: Suite =>
  val configuration: Config = ConfigFactory.load()

  val supervisor = system.actorSelection("/user/supervisor")

  val validationStats = system.actorOf(ValidationStats.props(), "validation-stats")

  val duration = 10.seconds
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(duration)

  implicit val flowParallelism: Int = configuration.getInt("hmda.actor-flow-parallelism")

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    super.beforeAll()
    val supervisor = HmdaSupervisor.createSupervisor(system)
    supervisor ! FindActorByName(InstitutionPersistence.name)
    val querySupervisor = HmdaQuerySupervisor.createQuerySupervisor(system)
    querySupervisor ! FindActorByName(InstitutionView.name)
    DemoData.loadTestData(system)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    system.terminate()
    val config = ConfigFactory.load()
    val snapshotStore = new File(config.getString("akka.persistence.snapshot-store.local.dir"))
    FileUtils.deleteRecursively(snapshotStore)
  }

  def multiPartFile(contents: String, fileName: String) =
    Multipart.FormData(Multipart.FormData.BodyPart.Strict(
      "file",
      HttpEntity(ContentTypes.`text/plain(UTF-8)`, contents),
      Map("filename" -> fileName)
    ))

}
