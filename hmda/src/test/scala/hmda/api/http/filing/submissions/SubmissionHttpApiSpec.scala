package hmda.api.http.filing.submissions

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.testkit._
import akka.util.Timeout
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.ErrorResponse
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import hmda.messages.filing.FilingCommands.CreateFiling
import hmda.messages.filing.FilingEvents.FilingEvent
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionEvent }
import hmda.model.filing.Filing
import hmda.model.filing.FilingGenerator._
import hmda.model.filing.submission.{ Submission, SubmissionId, VerificationStatus }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.institutionGen
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.{ HmdaValidationError, SubmissionPersistence }
import hmda.utils.YearUtils.Period
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.duration._

class SubmissionHttpApiSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest {

  val duration: FiniteDuration = 30.seconds
  val config: Config = system.settings.config

  implicit val routeTimeout: RouteTestTimeout = RouteTestTimeout(duration.dilated)

  implicit val typedSystem: ActorSystem[_] = system.toTyped
  val log: Logger                          = LoggerFactory.getLogger(getClass)
  implicit val timeout: Timeout            = Timeout(duration)
  val sharding: ClusterSharding            = ClusterSharding(typedSystem)
  val submissionRoutes                     = SubmissionHttpApi.create(config, log, sharding)

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  val period = Period(2018, None)

  val sampleInstitution: Institution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty.copy(LEI = "AAA"))

  val sampleFiling: Filing = filingGen.sample
    .getOrElse(Filing())
    .copy(lei = sampleInstitution.LEI)
    .copy(period = period.year.toString)

  val institutionProbe: TestProbe[InstitutionEvent] =
    TestProbe[InstitutionEvent]("institution-probe")

  val filingProbe: TestProbe[FilingEvent] =
    TestProbe[FilingEvent]("filing-probe")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(sharding)
    HmdaValidationError.startShardRegion(sharding)

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
  }

  override def afterAll(): Unit = super.afterAll()

  val url =
    s"/institutions/${sampleInstitution.LEI}/filings/$period/submissions"
  val noInstitutionUrl = s"/institutions/xxx/filings/$period/submissions"
  val noFilingUrl =
    s"/institutions/${sampleInstitution.LEI}/filings/2019/submissions"

  "Submissions HTTP API" must {
    "create new submission" in {
      Post(url) ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Created
        responseAs[Submission].id mustBe SubmissionId(sampleInstitution.LEI, period, 1)
      }
    }
    "fail to create a new submission if institutions does not exist" in {
      Post(noInstitutionUrl) ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
    "fail to create a new submission if filing does not exist" in {
      Post(noFilingUrl) ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
    "return latest submission" in {
      Get(s"$url/latest") ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Submission].id.sequenceNumber mustBe 1
      }
      Post(url) ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Created
      }
      Get(s"$url/latest") ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        responseAs[Submission].id.sequenceNumber mustBe 2

        import io.circe.generic.auto._
        responseAs[VerificationStatus] mustBe VerificationStatus(qualityVerified = false, macroVerified = false)
      }
    }

    "return the submission summary as not found when the file has not yet been uploaded" in {
      Get(s"$url/1/summary") ~> submissionRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.NotFound
        responseAs[ErrorResponse]
      }
    }
  }

}