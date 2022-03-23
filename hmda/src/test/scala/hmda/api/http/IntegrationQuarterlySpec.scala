package hmda.api.http

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.StatusCodes.Created
import akka.http.scaladsl.testkit.ScalatestRouteTest
import akka.stream.scaladsl.Sink
import akka.util.Timeout
import com.typesafe.config.Config
import hmda.api.http.admin.{ InstitutionAdminHttpApi, SubmissionAdminHttpApi }
import hmda.api.http.filing.submissions._
import hmda.api.http.filing.{FileUploadUtils, FilingHttpApi}
import hmda.api.http.model.filing.submissions.{EditsSign, EditsVerification}
import hmda.auth.{KeycloakTokenVerifier, OAuth2Authorization}
import hmda.messages.submission.SubmissionProcessingCommands.{CompleteMacro, CompleteQuality, CompleteSyntacticalValidity}
import hmda.model.filing.FilingDetails
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.model.institution.Institution
import hmda.model.institution.InstitutionGenerators.institutionGen
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission._
import hmda.utils.YearUtils.Period
import io.circe.Encoder
import io.circe.generic.semiauto._
import org.keycloak.adapters.KeycloakDeploymentBuilder
import org.scalatest.MustMatchers
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{Millis, Minutes, Span}
import org.slf4j.{Logger, LoggerFactory}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext}
import scala.util.Random

class IntegrationQuarterlySpec
  extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with ScalatestRouteTest
    with FileUploadUtils
    with Eventually
    with ScalaFutures {

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  val duration: FiniteDuration             = 10.seconds
  implicit val typedSystem: ActorSystem[_] = system.toTyped
  val log: Logger                          = LoggerFactory.getLogger(getClass)
  implicit val timeout: Timeout            = Timeout(duration)
  val sharding: ClusterSharding            = ClusterSharding(typedSystem)
  val ec: ExecutionContext                 = system.dispatcher
  val config: Config                       = system.settings.config

  val institutionAdminRoute = InstitutionAdminHttpApi.create(config, sharding)
  val submissionAdminRoute  = SubmissionAdminHttpApi.create(log, config, sharding, duration)
  val filingRoute           = FilingHttpApi.create(log, sharding)
  val submissionRoute       = SubmissionHttpApi.create(config, log, sharding)
  val editsRoute            = EditsHttpApi.create(log, sharding)
  val fileUploadRoute       = UploadHttpApi.create(log, sharding)
  val verifyRoute           = VerifyHttpApi.create(log, sharding)
  val signRoute             = SignHttpApi.create(log, sharding)

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  val quarterlyPeriod = Period(2020, Some("Q1"))

  val lei = Random.alphanumeric.take(20).mkString.toUpperCase
  val sampleInstitutionQuarterly: Institution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty)
    .copy(
      LEI = lei,
      activityYear = quarterlyPeriod.year,
      hmdaFiler = true,
      quarterlyFiler = true,
      quarterlyFilerHasFiledQ1 = false,
      quarterlyFilerHasFiledQ2 = false,
      quarterlyFilerHasFiledQ3 = false,
      taxId = Option("12-3456789")
    )

  val sampleSubmissionQuarterly = SubmissionId(sampleInstitutionQuarterly.LEI, quarterlyPeriod, 1)

  val hmdaFile = multiPartFile(
    contents = scala.io.Source.fromResource("clean_file_1000_rows_Bank0_syntax_validity.txt").getLines().mkString("\n"),
    fileName = "clean_file_1000_rows_Bank0_syntax_validity.txt"
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    Cluster(typedSystem).manager ! Join(Cluster(typedSystem).selfMember.address)

    InstitutionPersistence.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(sharding)
    HmdaRawData.startShardRegion(sharding)
    SubmissionManager.startShardRegion(sharding)
    HmdaParserError.startShardRegion(sharding)
    HmdaValidationError.startShardRegion(sharding)
    EditDetailsPersistence.startShardRegion(sharding)
  }

  override def afterAll(): Unit = super.afterAll()

  "IntegrationQuarterlySpec" must {
    "run through for a quarterly submission" in {
      import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
      val institution = Post("/institutions", sampleInstitutionQuarterly) ~> institutionAdminRoute(oAuth2Authorization) ~> check {
        response.status mustBe Created
        responseAs[Institution]
      }

      val filingDetails =
        Post(s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}") ~> filingRoute(
          oAuth2Authorization
        ) ~> check {
          status mustBe StatusCodes.Created
          import io.circe.generic.auto._
          responseAs[FilingDetails]
        }

      val submissionQuarterly =
        Post(
          s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions"
        ) ~> submissionRoute(
          oAuth2Authorization
        ) ~> check {
          status mustBe StatusCodes.Created
          responseAs[Submission]
        }

      val uploadFileSubmission =
        Post(
          s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${submissionQuarterly.id.sequenceNumber}",
          hmdaFile
        ) ~> fileUploadRoute(
          oAuth2Authorization
        ) ~> check {
          status mustBe StatusCodes.Accepted
          responseAs[Submission]
        }

      Get(s"/receipt/${submissionQuarterly.id}/hmdafile") ~> submissionAdminRoute(oAuth2Authorization) ~> check {
        status mustBe StatusCodes.OK
        val futureLineCount =
          response.entity.dataBytes
            .via(FlowUtils.framing)
            .map(_.utf8String)
            .runWith(Sink.fold(0L)((acc, _) => acc + 1))

        whenReady(futureLineCount)(actualLineCount => actualLineCount mustBe 1001)
      }

      val editsSummary =
        Get(
          s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/edits"
        ) ~> editsRoute(
          oAuth2Authorization
        ) ~> check {
          status mustBe StatusCodes.OK
        }

      Get(
        s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/edits/csv"
      ) ~> editsRoute(
        oAuth2Authorization
      ) ~> check {
        status mustBe StatusCodes.OK
      }

      Get(
        s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/edits/Q600"
      ) ~> editsRoute(
        oAuth2Authorization
      ) ~> check {
        status mustBe StatusCodes.OK
      }

      implicit val encoderEditsVerification: Encoder[EditsVerification] = deriveEncoder[EditsVerification]
      eventually {
        Post(
          s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/edits/quality",
          EditsVerification(verified = true)
        ) ~> verifyRoute(oAuth2Authorization) ~> check {
          // send after to get more coverage
          HmdaValidationError.selectHmdaValidationError(sharding, uploadFileSubmission.id) ! CompleteSyntacticalValidity(
            uploadFileSubmission.id
          )
          HmdaValidationError.selectHmdaValidationError(sharding, uploadFileSubmission.id) ! CompleteQuality(uploadFileSubmission.id)
          status mustBe StatusCodes.OK
        }
      }

      eventually {
        Post(
          s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/edits/macro",
          EditsVerification(verified = true)
        ) ~> verifyRoute(oAuth2Authorization) ~> check {
          // send after to get more coverage
          HmdaValidationError.selectHmdaValidationError(sharding, uploadFileSubmission.id) ! CompleteMacro(uploadFileSubmission.id)
          status mustBe StatusCodes.OK
        }
      }

      implicit val encoderEditsSign: Encoder[EditsSign] = deriveEncoder[EditsSign]
      Get(
        s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/sign"
      ) ~> signRoute(
        oAuth2Authorization
      ) ~> check {
        println {
          "sign get" +
            Await.result(responseEntity.dataBytes.runWith(Sink.fold("")(_ ++ _.utf8String)), 30.seconds)
        }
        status mustBe StatusCodes.OK
      }

      Post(
        s"/institutions/${sampleInstitutionQuarterly.LEI}/filings/${quarterlyPeriod.year}/quarter/${quarterlyPeriod.quarter.get}/submissions/${uploadFileSubmission.id.sequenceNumber}/sign",
        EditsSign(signed = true)
      ) ~> signRoute(
        oAuth2Authorization
      ) ~> check {
        println {
          "sign post" +
            Await.result(responseEntity.dataBytes.runWith(Sink.fold("")(_ ++ _.utf8String)), 30.seconds)
        }
        status mustBe StatusCodes.OK
      }
    }
  }
}