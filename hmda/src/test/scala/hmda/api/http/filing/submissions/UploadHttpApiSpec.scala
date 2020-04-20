package hmda.api.http.filing.submissions

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.{ RouteTestTimeout, ScalatestRouteTest }
import akka.stream.scaladsl.Source
import akka.testkit._
import akka.util.Timeout
import com.typesafe.config.Config
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.filing.FileUploadUtils
import hmda.api.http.model.ErrorResponse
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import hmda.messages.filing.FilingCommands.CreateFiling
import hmda.messages.filing.FilingEvents.FilingEvent
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents._
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents._
import hmda.model.filing.Filing
import hmda.model.filing.FilingGenerator.filingGen
import hmda.model.filing.lar.LarGenerators._
import hmda.model.filing.submission._
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.filing.ts.TsGenerators._
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.institutionGen
import hmda.model.submission.SubmissionGenerator.submissionGen
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission._
import hmda.utils.YearUtils.Period
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.duration._

class UploadHttpApiSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest with FileUploadUtils {

  implicit val typedSystem                       = system.toTyped
  val log: Logger                                = LoggerFactory.getLogger(getClass)
  val sharding: ClusterSharding                  = ClusterSharding(typedSystem)
  val duration                                   = 10.seconds
  implicit val routeTimeout                      = RouteTestTimeout(duration.dilated)
  implicit val timeout: Timeout                  = Timeout(10.seconds)
  val config: Config                             = typedSystem.settings.config
  val uploadRoutes: OAuth2Authorization => Route = UploadHttpApi.create(log, sharding)

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  val period = Period(2018, None)

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")
  val filingProbe      = TestProbe[FilingEvent]("filing-probe")
  val submissionProbe  = TestProbe[SubmissionEvent]("submission-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("submission-probe")

  val sampleInstitution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty.copy(LEI = "AAA"))

  val sampleFiling = filingGen.sample
    .getOrElse(Filing())
    .copy(lei = sampleInstitution.LEI)
    .copy(period = period.year.toString)

  val submissionId = SubmissionId(sampleInstitution.LEI, period, 1)

  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "" && s.id.lei != "AA")
    .suchThat(s => s.status == Created)
    .sample
    .getOrElse(Submission(submissionId))
    .copy(id = submissionId)

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    SubmissionManager.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(sharding)
    HmdaRawData.startShardRegion(sharding)

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

    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-${sampleSubmission.id.toString}")

    submissionPersistence ! CreateSubmission(sampleSubmission.id, submissionProbe.ref)
    submissionProbe.expectMessageType[SubmissionCreated]
  }

  override def afterAll(): Unit =
    super.afterAll()

  val url =
    s"/institutions/${sampleInstitution.LEI}/filings/$period/submissions/1"

  val badUrl =
    s"/institutions/${sampleInstitution.LEI}/filings/$period/submissions/200"

  val ts       = tsGen.sample.getOrElse(TransmittalSheet())
  val tsCsv    = ts.toCSV + "\n"
  val tsSource = Source.fromIterator(() => List(tsCsv).iterator)

  val larList   = larNGen(10).suchThat(_.nonEmpty).sample.getOrElse(Nil)
  val larCsv    = larList.map(lar => lar.toCSV + "\n")
  val larSource = Source.fromIterator(() => larCsv.iterator)

  val hmdaFileCsv = List(tsCsv) ++ larCsv
  val hmdaFile    = multiPartFile(hmdaFileCsv.mkString(""), "sample.txt")

  "Upload API" must {
    "upload HMDA File" in {
      Post(url, hmdaFile) ~> uploadRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.Accepted
        val submission = responseAs[Submission]
        submission.start must be < System.currentTimeMillis()
        submission.id mustBe submissionId
        submission.status mustBe Uploaded
      }
    }

    "return Bad Request when submission doesn't exist" in {
      Post(badUrl, hmdaFile) ~> uploadRoutes(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
        val response = responseAs[ErrorResponse]
        response.path mustBe Path(badUrl)
        response.message mustBe s"Submission ${sampleInstitution.LEI}-${period}-200 not available for upload"
      }
    }
  }

}