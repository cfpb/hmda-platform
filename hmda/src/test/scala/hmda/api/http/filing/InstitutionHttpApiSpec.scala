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
import hmda.messages.filing.FilingCommands.CreateFiling
import hmda.messages.filing.FilingEvents.{ FilingCreated, FilingEvent }
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionEvent }
import hmda.model.filing.Filing
import hmda.model.filing.FilingGenerator._
import hmda.model.institution.InstitutionGenerators._
import hmda.model.institution.{ Institution, InstitutionDetail }
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import io.circe.generic.auto._
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class InstitutionHttpApiSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest {

  val duration = 30.seconds

  implicit val routeTimeout = RouteTestTimeout(duration.dilated)

  implicit val typedSystem: ActorSystem[_] = system.toTyped
  val log: Logger                          = LoggerFactory.getLogger(getClass)
  val ec: ExecutionContext                 = system.dispatcher
  implicit val timeout: Timeout            = Timeout(duration)
  val sharding: ClusterSharding            = ClusterSharding(typedSystem)
  val institutionRoutes                    = InstitutionHttpApi.create(log, sharding)

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
    .getOrElse(Institution.empty.copy(LEI = "AAA"))

  val period = "2018"

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")
  val filingProbe      = TestProbe[FilingEvent]("filing-probe")

  val sampleFiling = filingGen.sample
    .getOrElse(Filing())
    .copy(lei = sampleInstitution.LEI)
    .copy(period = period)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    val institutionPersistence =
      sharding.entityRefFor(InstitutionPersistence.typeKey, s"${InstitutionPersistence.name}-${sampleInstitution.LEI}")
    institutionPersistence ! CreateInstitution(sampleInstitution, institutionProbe.ref)
    institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))
    val filingPersistence =
      sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${sampleInstitution.LEI}-$period"
      )
    filingPersistence ! CreateFiling(sampleFiling, filingProbe.ref)

    filingProbe.expectMessage(FilingCreated(sampleFiling))
  }

  override def afterAll(): Unit = super.afterAll()

  val url    = s"/institutions/${sampleInstitution.LEI}/year/2018"
  val badUrl = s"/institutions/xxxx/year/2018"

  "Institutions" must {
    "return NotFound when institution does not exist" in {
      Get(badUrl) ~> institutionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
      }
    }

    "return Institution when found" in {
      Get(url) ~> institutionRoutes(oAuth2Authorization) ~> check {
        val details = responseAs[InstitutionDetail]
        details.filings.head.lei mustBe sampleInstitution.LEI
      }
    }
  }

}