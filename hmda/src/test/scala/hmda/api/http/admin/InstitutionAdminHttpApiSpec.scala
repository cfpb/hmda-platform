package hmda.api.http.admin

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.testkit._
import akka.util.Timeout
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.admin.InstitutionDeletedResponse
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.institution.InstitutionPersistence
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.duration._
import scala.util.Random

class InstitutionAdminHttpApiSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest {

  val duration = 10.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  implicit val typedSystem: ActorSystem[_] = system.toTyped
  val sharding: ClusterSharding            = ClusterSharding(typedSystem)
  val config: Config                       = typedSystem.settings.config
  implicit val timeout: Timeout            = Timeout(duration)
  val log: Logger                          = LoggerFactory.getLogger(getClass)
  val institutionAdminRoutes               = InstitutionAdminHttpApi.create(config,sharding)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
  }

  override def afterAll(): Unit = super.afterAll()

  val lei = Random.alphanumeric.take(20).mkString.toUpperCase

  val sampleInstitution =
    institutionGen.sample
      .getOrElse(Institution.empty)
      .copy(LEI = lei)
      .copy(taxId = Option("12-3456789"))
      .copy(activityYear = 2018)

  val sampleWrongLEIInstitution = sampleInstitution.copy(LEI = "Hello, world")

  val sampleWrongTaxInstitution = sampleInstitution.copy(taxId = Option(""))

  val sampleQuarterlyInstitution = sampleInstitution.copy(quarterlyFiler = true, quarterlyFilerHasFiledQ1 = true)

  val modified =
    sampleInstitution.copy(emailDomains = List("email@bank.com"))

  val filerFlagsNegated = modified.copy(
    hmdaFiler = !modified.hmdaFiler,
    quarterlyFilerHasFiledQ1 = !modified.quarterlyFilerHasFiledQ1,
    quarterlyFilerHasFiledQ2 = !modified.quarterlyFilerHasFiledQ2,
    quarterlyFilerHasFiledQ3 = !modified.quarterlyFilerHasFiledQ3
  )
  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  "Institutions HTTP Service" must {

    "Create an institution" in {
      Post("/institutions", sampleInstitution) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Created
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Return a 400 on a duplicate LEI" in {
      Post("/institutions", sampleInstitution) ~> Route.seal(institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }

    "Return a 404 on a wrong path to create an institution" in {
      Post("/wrongpath", sampleInstitution) ~> Route.seal(institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Return a 404 on a wrong path" in {
      Post("/wrongpath") ~> Route.seal(institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Return a 400 on an incorrect lei" in {
      Post("/institutions", sampleWrongLEIInstitution) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }

    "Return Bad Request for the wrong tax id format" in {
      Post("/institutions", sampleWrongTaxInstitution) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }

    "Get an institution" in {
      Get(s"/institutions/${sampleInstitution.LEI}/year/2018") ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Return a 404 on a wrong path to get an institution" in {
      Get(s"/wrongpath/${sampleInstitution.LEI}/year/2018") ~> Route.seal(institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Get an institution by quarter" in {
      Get(s"/institutions/${modified.LEI}/year/2018/quarter/Q1") ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Get all institutions for lei" in {
      Get(s"/institutions/${modified.LEI}") ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
      }
    }

    "Modify an institution" in {
      Put("/institutions", modified) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[Institution] mustBe modified
      }
    }

    "Return a 404 on a wrong path to modify an institution" in {
      Put("/wrongpath", modified) ~> Route.seal(institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Ignore filer flag for an institution with filer flag set" in {
      Put("/institutions", filerFlagsNegated) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[Institution] mustBe modified
      }
    }

    "Create institution when it doesn't exist for the lei" in {
      val sampleNewInstitution = sampleInstitution.copy(activityYear = 2019)
      Put("/institutions", sampleNewInstitution) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Created
        responseAs[Institution] mustBe sampleNewInstitution
      }
    }

    "Return a 400 when we try to modify for the incorrect lei" in {
      Put("/institutions", sampleWrongLEIInstitution) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }

    "Delete an institution" in {
      Delete("/institutions", modified) ~> institutionAdminRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[InstitutionDeletedResponse] mustBe InstitutionDeletedResponse(lei)
      }
    }

    "Return a 404 on a wrong path to delete an institution" in {
      Delete("/institutions", modified) ~> Route.seal(institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

  }

}