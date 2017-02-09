package hmda.api.http.public

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.RequestHeaderUtils
import hmda.model.fi.lar.LarGenerators
import org.scalatest.{ BeforeAndAfterAll, BeforeAndAfterEach, MustMatchers, WordSpec }
import akka.util.Timeout

import scala.concurrent.duration._
import scala.concurrent.Await
import hmda.query.repository.filing.LarConverter._

class PublicHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterEach with BeforeAndAfterAll
    with ScalatestRouteTest with RequestHeaderUtils with PublicHttpApi with LarGenerators {

  override val log: LoggingAdapter = NoLogging
  implicit val ec = system.dispatcher
  val repository = new LarRepository(config)
  val totalRepository = new LarTotalRepository(config)

  val duration = 10.seconds
  override implicit val timeout = Timeout(duration)

  val lar1 = larGen.sample.get.copy(respondentId = "0")
  val lar2 = larGen.sample.get.copy(respondentId = "0")
  val p = "2017"
  val l1 = toLoanApplicationRegisterQuery(lar1).copy(period = p)
  val l2 = toLoanApplicationRegisterQuery(lar2).copy(period = p)

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.result(repository.createSchema(), duration)
    Await.result(modifiedLarRepository.createSchema(), duration)
    loadData()
  }

  override def afterEach(): Unit = {
    super.afterEach()
    Await.result(modifiedLarRepository.dropSchema(), duration)
    Await.result(repository.dropSchema(), duration)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    repository.config.db.close()
  }

  private def loadData(): Unit = {
    Await.result(repository.insertOrUpdate(l1), duration)
    Await.result(repository.insertOrUpdate(l2), duration)
  }

  "Modified LAR Http API" must {
    "return list of modified LARs in proper format" in {
      Get("/institutions/0/filings/2017/lar") ~> publicHttpRoutes ~> check {
        status mustBe StatusCodes.OK
        contentType mustBe ContentTypes.`text/csv(UTF-8)`
        responseAs[String].split("\n") must have size 2
      }
    }
  }

}
