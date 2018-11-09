package hmda.api.http.filing.submissions

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.util.Timeout
import hmda.persistence.AkkaCassandraPersistenceSpec
import org.scalatest.MustMatchers
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import hmda.api.http.model.filing.submissions.ParsingErrorSummary
import hmda.messages.filing.FilingCommands.CreateFiling
import hmda.messages.filing.FilingEvents.FilingEvent
import hmda.messages.institution.InstitutionCommands.CreateInstitution
import hmda.messages.institution.InstitutionEvents.{
  InstitutionCreated,
  InstitutionEvent
}
import hmda.messages.submission.SubmissionCommands.CreateSubmission
import hmda.messages.submission.SubmissionEvents.{
  SubmissionCreated,
  SubmissionEvent
}
import hmda.messages.submission.SubmissionProcessingCommands.{
  GetParsedWithErrorCount,
  PersistHmdaRowParsedError
}
import hmda.model.filing.Filing
import hmda.model.filing.FilingGenerator.filingGen
import hmda.model.filing.submission.{ParsedWithErrors, Submission, SubmissionId}
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.institutionGen
import hmda.model.submission.SubmissionGenerator.submissionGen
import hmda.parser.filing.lar.LarParserErrorModel.InvalidId
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.{HmdaParserError, SubmissionPersistence}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import hmda.api.http.codec.filing.submission.ParsingErrorSummaryCodec._
import hmda.messages.submission.SubmissionProcessingEvents.{
  HmdaRowParsedCount,
  SubmissionProcessingEvent
}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class ParseErrorHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with ParseErrorHttpApi
    with ScalatestRouteTest {

  val duration = 10.seconds

  override implicit val typedSystem: ActorSystem[_] = system.toTyped
  override val log: LoggingAdapter = NoLogging
  override implicit val timeout: Timeout = Timeout(duration)
  override val sharding: ClusterSharding = ClusterSharding(typedSystem)
  val ec: ExecutionContext = system.dispatcher

  val period = "2018"

  val sId = SubmissionId("12345", period, 1)

  val sampleInstitution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .map(_.copy(LEI = sId.lei))
    .getOrElse(Institution.empty.copy(LEI = sId.lei))

  val sampleFiling = filingGen.sample
    .getOrElse(Filing())
    .copy(lei = sId.lei)
    .copy(period = period)

  val sampleSubmission = submissionGen
    .suchThat(s => !s.id.isEmpty)
    .suchThat(s => s.id.lei != "")
    .suchThat(s => s.status == ParsedWithErrors)
    .sample
    .map(_.copy(id = sId))
    .getOrElse(Submission(sId))

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")
  val filingProbe = TestProbe[FilingEvent]("filing-probe")
  val submissionProbe = TestProbe[SubmissionEvent]("submission-probe")
  val errorsProbe = TestProbe[SubmissionProcessingEvent]("parsing-error-probe")

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)
    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(sharding)
    HmdaParserError.startShardRegion(sharding)

    val institutionPersistence =
      sharding.entityRefFor(InstitutionPersistence.typeKey,
                            s"${InstitutionPersistence.name}-${sId.lei}")
    institutionPersistence ! CreateInstitution(sampleInstitution,
                                               institutionProbe.ref)
    institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

    val filingPersistence =
      sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${sId.lei}-$period"
      )
    filingPersistence ! CreateFiling(sampleFiling, filingProbe.ref)

    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey,
                            s"${SubmissionPersistence.name}-${sId.toString}")
    submissionPersistence ! CreateSubmission(sId, submissionProbe.ref)
    submissionProbe.expectMessageType[SubmissionCreated]

    val hmdaParserError = sharding.entityRefFor(
      HmdaParserError.typeKey,
      s"${HmdaParserError.name}-${sId.toString}")
    for (i <- 1 to 100) {
      val errorList = List(InvalidId)
      hmdaParserError ! PersistHmdaRowParsedError(i,
                                                  errorList.map(_.errorMessage),
                                                  None)
    }

    hmdaParserError ! GetParsedWithErrorCount(errorsProbe.ref)
    errorsProbe.expectMessage(HmdaRowParsedCount(100))
  }

  override def afterAll(): Unit = super.afterAll()

  "Parser HTTP API" must {
    "Return Bad Request when requesting parsing errors from submission that doesn't exist" in {
      val badUrl = "/institutions/XXX/filings/2019/submissions/1/parseErrors"
      Get(badUrl) ~> parserErrorRoute ~> check {
        status mustBe StatusCodes.BadRequest
      }
    }
    "Return paginated response with errors" in {
      val firstPage =
        s"/institutions/${sampleInstitution.LEI}/filings/${sampleSubmission.id.period}/submissions/${sampleSubmission.id.sequenceNumber}/parseErrors"

      Get(firstPage) ~> parserErrorRoute ~> check {
        status mustBe StatusCodes.OK
        val result = responseAs[ParsingErrorSummary]
        result.total mustBe 100
        result.count mustBe 20
        result.links.self mustBe "?page=1"
        result.links.next mustBe "?page=2"
      }

      val secondPage =
        s"/institutions/${sampleInstitution.LEI}/filings/${sampleSubmission.id.period}/submissions/${sampleSubmission.id.sequenceNumber}/parseErrors?page=2"

      Get(secondPage) ~> parserErrorRoute ~> check {
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
