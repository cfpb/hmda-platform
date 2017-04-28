package hmda.api.http.public

import akka.event.{ LoggingAdapter, NoLogging }
import akka.http.scaladsl.model.{ ContentTypes, StatusCodes }
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.api.RequestHeaderUtils
import hmda.model.fi.lar.LarGenerators
import hmda.query.repository.filing.LarConverter._
import hmda.query.DbConfiguration._

import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class PublicHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll
    with ScalatestRouteTest with RequestHeaderUtils with PublicHttpApi with LarGenerators {

  override val log: LoggingAdapter = NoLogging
  implicit val ec = system.dispatcher
  val repository = new LarRepository(config)
  val larTotalMsaRepository = new LarTotalMsaRepository(config)

  val duration = 10.seconds
  override implicit val timeout = Timeout(duration)

  import repository.config.profile.api._

  val p = "2017"
  val l1 = toLoanApplicationRegisterQuery(sampleLar).copy(period = p, institutionId = "0")
  val l2 = toLoanApplicationRegisterQuery(sampleLar).copy(period = p, institutionId = "0")

  override def beforeAll(): Unit = {
    super.beforeAll()
    val setup = for {
      a <- dropAllObjects
      b <- repository.createSchema()
      c <- modifiedLarRepository.createSchema()
      d <- repository.insertOrUpdate(l1)
      e <- repository.insertOrUpdate(l2)
    } yield (d, e)
    Await.result(setup, duration)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    dropAllObjects
  }

  private def dropAllObjects: Future[Int] = {
    val db = repository.config.db
    val dropAll = sqlu"""DROP ALL OBJECTS"""
    db.run(dropAll)
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
