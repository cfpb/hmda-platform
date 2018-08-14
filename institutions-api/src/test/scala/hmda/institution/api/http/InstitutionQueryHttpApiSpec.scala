package hmda.institution.api.http

import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.institution.query.InstitutionSetup
import hmda.model.institution.Institution
import org.scalatest.{BeforeAndAfterAll, MustMatchers, WordSpec}
import hmda.query.DbConfiguration._

import scala.concurrent.ExecutionContext
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.codec.institution.InstitutionCodec._
import hmda.institution.api.http.model.InstitutionsResponse
import io.circe.generic.auto._

class InstitutionQueryHttpApiSpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with InstitutionQueryHttpApi
    with InstitutionSetup {

  override val institutionRepository = new InstitutionRepository(config)

  override val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = NoLogging
  implicit val timeout = Timeout(duration)

  override def beforeAll = {
    super.beforeAll()
    setup()
  }

  override def afterAll = {
    super.afterAll()
    tearDown()
  }

  "Institution Query HTTP API" must {
    "search by LEI" in {
      Get("/institutions/XXX") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
      Get(s"/institutions/AAA") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe InstitutionConverter.convert(
          instA,
          Seq("aaa.com", "bbb.com"))
        responseAs[Institution].emailDomains mustBe Seq("aaa.com", "bbb.com")
      }
    }

    "search by email domain" in {
      Get("/institutions?domain=xxx.com") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
      Get("/institutions?domain=bbb.com") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionsResponse].institutions.size mustBe 2
      }
    }
  }

}
