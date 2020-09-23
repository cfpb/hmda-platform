package hmda.api.http.filing

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.testkit._
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents._
import hmda.model.filing.{ FilingDetails, InProgress }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators._
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import io.circe.generic.auto._
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.duration._
import scala.util.Random

class FilingHttpApiSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest {

  val duration = 90.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  implicit val typedSystem: ActorSystem[_] = system.toTyped
  val log: Logger                          = LoggerFactory.getLogger(getClass)
  implicit val timeout: Timeout            = Timeout(duration)
  val sharding: ClusterSharding            = ClusterSharding(typedSystem)
  val filingRoutes                         = FilingHttpApi.create(log, sharding)

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  val sampleInstitution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty.copy(LEI = Random.alphanumeric.take(10).mkString))

  val period = "2018"

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    val institutionPersistence =
      sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-${sampleInstitution.LEI}")
    institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
    institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))
  }

  override def afterAll(): Unit = super.afterAll()

  val url    = s"/institutions/${sampleInstitution.LEI}/filings/$period"
  val badUrl = s"/institutions/xxxx/filings/$period"

  "Filings" must {
    "return Bad Request when institution does not exist" in {
      Get(badUrl) ~> filingRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
    "return NotFound when institution exists but filing has not been created" in {
      Get(url) ~> filingRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }
    "create filing and return it" in {
      Post(url) ~> filingRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Created
        val details = responseAs[FilingDetails]
        details.filing.lei mustBe sampleInstitution.LEI
        details.filing.period mustBe period
        details.filing.status mustBe InProgress
        details.submissions mustBe Nil
      }
      Post(url) ~> filingRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
      Post(badUrl) ~> filingRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
  }
}