package hmda.api.http.filing.submissions

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{ Cluster, Join }
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import hmda.auth.{ KeycloakTokenVerifier, OAuth2Authorization }
import hmda.messages.filing.FilingCommands.CreateFiling
import hmda.messages.filing.FilingEvents.FilingEvent
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionEvent }
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents.{ SubmissionCreated, SubmissionEvent }
import hmda.messages.submission.SubmissionProcessingCommands.{ GetParsedWithErrorCount, PersistHmdaRowParsedError, _ }
import hmda.messages.submission.SubmissionProcessingEvents.{ HmdaRowParsedCount, SubmissionProcessingEvent }
import hmda.model.filing.Filing
import hmda.model.filing.FilingGenerator.filingGen
import hmda.model.filing.submission.{ Created, Submission, SubmissionId }
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.institutionGen
import hmda.model.submission.SubmissionGenerator.submissionGen
import hmda.parser.filing.lar.LarParserErrorModel.InvalidLarId
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.{ HmdaParserError, SubmissionPersistence }
import hmda.utils.YearUtils.Period
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.slf4j.{ Logger, LoggerFactory }

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.Random

class ParseErrorHttpApiSpec extends AkkaCassandraPersistenceSpec with MustMatchers with ScalatestRouteTest {

  val duration = 10.seconds

  implicit val typedSystem: ActorSystem[_]           = system.toTyped
  val log: Logger                                    = LoggerFactory.getLogger(getClass)
  implicit val timeout: Timeout                      = Timeout(duration)
  val sharding: ClusterSharding                      = ClusterSharding(typedSystem)
  val ec: ExecutionContext                           = system.dispatcher
  val parserErrorRoute: OAuth2Authorization => Route = ParseErrorHttpApi.create(log, sharding)

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  val period = Period(2018, None)

  val sampleInstitution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty.copy(LEI = Random.alphanumeric.take(10).mkString))

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

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")
  val filingProbe      = TestProbe[FilingEvent]("filing-probe")
  val submissionProbe  = TestProbe[SubmissionEvent]("submission-probe")
  val errorsProbe      = TestProbe[SubmissionProcessingEvent]("parsing-error-probe")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(sharding)
    HmdaParserError.startShardRegion(sharding)

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

    val hmdaParserError = sharding.entityRefFor(HmdaParserError.typeKey, s"${HmdaParserError.name}-${sampleSubmission.id.toString}")
    for (i <- 1 to 100) {
      val errorList = List(InvalidLarId("a"))
      hmdaParserError ! PersistHmdaRowParsedError(i, "testULI", errorList.map(x => FieldParserError(x.fieldName, x.inputValue)), None)
    }

    hmdaParserError ! GetParsedWithErrorCount(errorsProbe.ref)
    errorsProbe.expectMessage(HmdaRowParsedCount(100))
  }

  override def afterAll(): Unit = super.afterAll()

  "Parser HTTP API" must {
    "Return Bad Request when requesting parsing errors from submission that doesn't exist" in {
      val badUrl = "/institutions/XXX/filings/2019/submissions/1/parseErrors"
      Get(badUrl) ~> parserErrorRoute(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
    "Return paginated response with errors" in {
      val firstPage =
        s"/institutions/${sampleInstitution.LEI}/filings/${sampleSubmission.id.period}/submissions/${sampleSubmission.id.sequenceNumber}/parseErrors"

      Get(firstPage) ~> parserErrorRoute(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        val result = responseAs[ParsingErrorSummary]
        result.total mustBe 100
        result.count mustBe 20
        result.links.self mustBe "?page=1"
        result.links.next mustBe "?page=2"
      }

      val secondPage =
        s"/institutions/${sampleInstitution.LEI}/filings/${sampleSubmission.id.period}/submissions/${sampleSubmission.id.sequenceNumber}/parseErrors?page=2"

      Get(secondPage) ~> parserErrorRoute(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        val result = responseAs[ParsingErrorSummary]
        result.total mustBe 100
        result.count mustBe 20
        result.links.self mustBe "?page=2"
        result.links.prev mustBe "?page=1"
        result.links.next mustBe "?page=3"
      }
    }
  }

}