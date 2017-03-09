package hmda.api.http.public

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import hmda.api.RequestHeaderUtils
import hmda.model.fi.lar.LarGenerators
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import akka.util.Timeout
import hmda.query.DbConfiguration._
import scala.concurrent.duration._
import scala.concurrent.Await
import hmda.query.repository.filing.LarConverter._

class PublicHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with RequestHeaderUtils with PublicHttpApi with LarGenerators {

  override val log: LoggingAdapter = NoLogging
  implicit val ec = system.dispatcher
  val repository = new LarRepository(config)
  val larTotalRepository = new LarTotalRepository(config)

  val duration = 10.seconds
  override implicit val timeout = Timeout(duration)

  import repository.config.profile.api._

  val lar1 = sampleLar.copy(respondentId = "0")
  val lar2 = sampleLar.copy(respondentId = "0")
  val p = "2017"
  val l1 = toLoanApplicationRegisterQuery(lar1).copy(period = p)
  val l2 = toLoanApplicationRegisterQuery(lar2).copy(period = p)

  override def beforeAll(): Unit = {
    super.beforeAll()
    dropAllObjects()
    Await.result(repository.createSchema(), duration)
    Await.result(modifiedLarRepository.createSchema(), duration)
    loadData()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects()
    repository.config.db.close()
  }

  private def dropAllObjects() = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    Await.result(db.run(dropAll), duration)
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
