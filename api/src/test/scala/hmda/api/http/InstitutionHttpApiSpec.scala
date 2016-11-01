package hmda.api.http

import java.io.File

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.{ ContentTypes, HttpEntity, Multipart }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.api.RequestHeaderUtils
import hmda.persistence.HmdaSupervisor
import hmda.persistence.demo.DemoData
import hmda.persistence.institutions.InstitutionPersistence
import hmda.persistence.model.HmdaSupervisorActor.FindActorByName
import org.iq80.leveldb.util.FileUtils
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.duration._

trait InstitutionHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with RequestHeaderUtils with InstitutionsHttpApi {

  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(10.seconds)

  val ec = system.dispatcher

  override def beforeAll(): Unit = {
    val supervisor = HmdaSupervisor.createSupervisor(system)
    supervisor ! FindActorByName(InstitutionPersistence.name)
    DemoData.loadTestData(system)
    super.beforeAll()
  }

  override def afterAll(): Unit = {
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
