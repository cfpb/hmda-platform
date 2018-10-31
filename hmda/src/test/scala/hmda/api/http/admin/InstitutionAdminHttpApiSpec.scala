package hmda.api.http.admin

import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.{LoggingAdapter, NoLogging}
import akka.stream.ActorMaterializer
import akka.util.Timeout
import hmda.persistence.AkkaCassandraPersistenceSpec
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import hmda.model.institution.Institution
import org.scalatest.MustMatchers
import hmda.model.institution.InstitutionGenerators._
import hmda.api.http.codec.institution.InstitutionCodec._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.persistence.institution.InstitutionPersistence
import akka.testkit._
import hmda.api.http.model.admin.InstitutionDeletedResponse
import hmda.auth.{KeycloakTokenVerifier, OAuth2Authorization}
import io.circe.generic.auto._
import org.keycloak.adapters.KeycloakDeploymentBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class InstitutionAdminHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with InstitutionAdminHttpApi
    with ScalatestRouteTest {

  val duration = 10.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  override implicit val typedSystem: ActorSystem[_] = system.toTyped
  override implicit val materializer: ActorMaterializer = ActorMaterializer()
  override val log: LoggingAdapter = NoLogging
  override val ec: ExecutionContext = system.dispatcher
  override implicit val timeout: Timeout = Timeout(duration)
  override val sharding: ClusterSharding = ClusterSharding(typedSystem)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
  }

  override def afterAll(): Unit = super.afterAll()

  val lei = "AAA"
  val sampleInstitution =
    institutionGen.sample.getOrElse(Institution.empty).copy(LEI = lei)

  val modified =
    sampleInstitution.copy(emailDomains = List("email@bank.com"))

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
      Post("/institutions", sampleInstitution) ~> institutionAdminRoutes(
        oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Created
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Return a 404 on a wrongpath to create an institution" in {
      Post("/wrongpath", sampleInstitution) ~> Route.seal(
        institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Return a 404 on a wrongpath" in {
      Post("/wrongpath") ~> Route.seal(
        institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Get an institution" in {
      Get(s"/institutions/${sampleInstitution.LEI}") ~> institutionAdminRoutes(
        oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Institution] mustBe sampleInstitution
      }
    }

    "Return a 404 on a wrongpath to get an institution " in {
      Get(s"/wrongpath/${sampleInstitution.LEI}") ~> Route.seal(
        institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Modify an institution" in {
      Put("/institutions", modified) ~> institutionAdminRoutes(
        oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[Institution] mustBe modified
      }
    }

    "Return a 404 on a wrongpath to modiy an institution" in {
      Put("/wrongpath", modified) ~> Route.seal(
        institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "Delete an institution" in {
      Delete("/institutions", modified) ~> institutionAdminRoutes(
        oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Accepted
        responseAs[InstitutionDeletedResponse] mustBe InstitutionDeletedResponse(
          lei)
      }
    }

    "Return a 404 on a wrongpath to delete an institution" in {
      Delete("/institutions", modified) ~> Route.seal(
        institutionAdminRoutes(oAuth2Authorization)) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

  }

}
