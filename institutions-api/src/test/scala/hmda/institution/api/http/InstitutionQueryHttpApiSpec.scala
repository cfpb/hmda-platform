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
import hmda.institution.api.http.model.InstitutionsResponse
import io.circe.generic.auto._

class InstitutionQueryHttpApiSpec
    extends WordSpec
    with MustMatchers
    with BeforeAndAfterAll
    with ScalatestRouteTest
    with InstitutionQueryHttpApi
    with InstitutionSetup {

  override val institutionRepository2018 = new InstitutionRepository2018(
    dbConfig)
  override val institutionRepository2019 = new InstitutionRepository2019(
    dbConfig)

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
      Get("/institutions/XXX/year/2018") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
      Get(s"/institutions/AAA/year/2018") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe InstitutionConverter.convert(
          instA,
          Seq("aaa.com", "bbb.com"))
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
      Get(
        "/institutions/year/2018?domain=aaa.com&lei=AAA&respondentName=RespA&taxId=taxIdA") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.OK
        val institutions = responseAs[InstitutionsResponse].institutions
        institutions.size mustBe 1
        institutions.head.LEI mustBe "AAA"
        institutions.head.taxId mustBe Some("taxIdA")
        institutions.head.respondent.name mustBe Some("RespA")
        institutions.head.emailDomains mustBe List("aaa.com", "bbb.com")
      }
      Get(
        "/institutions/year/2018?domain=xxx.com&lei=XXX&respondentName=RespX&taxId=taxIdX") ~> institutionPublicRoutes ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
  }

}
