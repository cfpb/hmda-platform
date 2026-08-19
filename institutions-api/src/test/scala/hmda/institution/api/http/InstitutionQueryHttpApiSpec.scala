package hmda.institution.api.http

import org.apache.pekko.http.scaladsl.model.StatusCodes
import org.apache.pekko.http.scaladsl.server.Route
import org.apache.pekko.http.scaladsl.testkit.ScalatestRouteTest
import org.apache.pekko.util.Timeout
import com.typesafe.config.ConfigFactory
import com.github.pjfanning.pekkohttpcirce.FailFastCirceSupport._
import hmda.institution.api.http.model.InstitutionsResponse
import hmda.institution.query.InstitutionSetup
import hmda.model.institution.Institution
import io.circe.generic.auto._
import org.scalatest.{ BeforeAndAfterAll, MustMatchers, WordSpec }
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext

class InstitutionQueryHttpApiSpec extends WordSpec with MustMatchers with BeforeAndAfterAll with ScalatestRouteTest with InstitutionSetup {

  val ec: ExecutionContext               = system.dispatcher
  val config                             = ConfigFactory.load()
  implicit val timeout                   = Timeout(duration)
  val log: Logger                          = LoggerFactory.getLogger(getClass)

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier
  )

  val institutionPublicRoutes     = InstitutionQueryHttpApi.create(config)

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
      Get("/institutions/XXX/year/2018") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
      }

      Get("/institutions/XXX/year/2019") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
      }

      Get("/institutions/XXX/year/2020") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
      }

      Get(s"/institutions/AAA/year/2018") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe InstitutionConverter.convert(instA, Seq("aaa.com", "bbb.com"))
        responseAs[Institution].emailDomains mustBe Seq("aaa.com", "bbb.com")
      }
    }

    "search by email domain" in {
      Get("/institutions?domain=xxx.com") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
      }
      Get("/institutions?domain=bbb.com") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[InstitutionsResponse].institutions.size mustBe 2
      }
    }

    "search by institution" in {
      Get("/institutions?domain=aaa.com") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
      }

      Get("/institutions?domain=aaa.com&lei=AAA&respondentName=RespA&taxId=taxIdA") ~> institutionPublicRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
      }
    }
  }

}