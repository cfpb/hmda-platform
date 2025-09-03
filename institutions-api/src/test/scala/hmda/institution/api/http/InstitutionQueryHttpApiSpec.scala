package hmda.institution.api.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.institution.api.http.model.InstitutionsResponse
import hmda.institution.query.InstitutionSetup
import hmda.model.institution.Institution
import io.circe.generic.auto._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }

import scala.concurrent.ExecutionContext

class InstitutionQueryHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalatestRouteTest with InstitutionSetup {

  val ec: ExecutionContext               = system.dispatcher
  val config                             = ConfigFactory.load()
  implicit val timeout                   = Timeout(duration)
  val institutionPublicRoutes: Route     = InstitutionQueryHttpApi.create(config)

  override def beforeAll: Unit = {
    super.beforeAll()
    setup()
  }

  override def afterAll: Unit = {
    super.afterAll()
    tearDown()
  }

  "Institution Query HTTP API" must {
    "search by LEI" in {
      Get("/institutions/XXX/year/2018") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }

      Get("/institutions/XXX/year/2019") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }

      Get("/institutions/XXX/year/2020") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }

      Get(s"/institutions/AAA/year/2018") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe InstitutionConverter.convert(instA, Seq("aaa.com", "bbb.com"))
        responseAs[Institution].emailDomains mustBe Seq("aaa.com", "bbb.com")
      }
    }

    "search by email domain" in {
      Get("/institutions/year/2018?domain=xxx.com") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
      Get("/institutions/year/2018?domain=bbb.com") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionsResponse].institutions.size mustBe 2
      }
    }

    "search by fields values" in {
      Get("/institutions/year/2018?domain=aaa.com&lei=AAA&respondentName=RespA&taxId=taxIdA") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        val institutions = responseAs[InstitutionsResponse].institutions
        institutions.size mustBe 1
        institutions.head.LEI mustBe "AAA"
        institutions.head.taxId mustBe Some("taxIdA")
        institutions.head.respondent.name mustBe Some("RespA")
        institutions.head.emailDomains mustBe List("aaa.com", "bbb.com")
      }

      Get("/institutions/year/2018?domain=xxx.com&lei=XXX&respondentName=RespX&taxId=taxIdX") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "search by institution" in {
      Get("/institutions?domain=aaa.com") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
      }

      Get("/institutions?domain=aaa.com&lei=AAA&respondentName=RespA&taxId=taxIdA") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }

}