package hmda.api.http.filing.submissions

import akka.actor.testkit.typed.scaladsl.TestProbe
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.event.{LoggingAdapter, NoLogging}
import akka.http.scaladsl.testkit.{RouteTestTimeout, ScalatestRouteTest}
import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}
import org.scalatest.MustMatchers
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.typed.{Cluster, Join}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.Uri.Path
import akka.kafka.ConsumerSettings
import akka.stream.scaladsl.Source
import hmda.api.http.filing.FileUploadUtils
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
import hmda.model.filing.Filing
import hmda.model.filing.FilingGenerator.filingGen
import hmda.model.filing.submission.{
  Created,
  Submission,
  SubmissionId,
  Uploaded
}
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.Institution
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._
import hmda.api.http.codec.filing.submission.SubmissionStatusCodec._
import hmda.api.http.model.ErrorResponse
import hmda.api.http.codec.ErrorResponseCodec._
import hmda.auth.{KeycloakTokenVerifier, OAuth2Authorization}
import hmda.model.institution.InstitutionGenerators.institutionGen
import hmda.model.submission.SubmissionGenerator.submissionGen
import hmda.persistence.AkkaCassandraPersistenceSpec
import hmda.persistence.filing.FilingPersistence
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.submission.{SubmissionManager, SubmissionPersistence}
import net.manub.embeddedkafka.{EmbeddedKafka, EmbeddedKafkaConfig}
import hmda.model.filing.ts.TsGenerators._
import hmda.model.filing.lar.LarGenerators._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.keycloak.adapters.KeycloakDeploymentBuilder

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class UploadHttpApiSpec
    extends AkkaCassandraPersistenceSpec
    with MustMatchers
    with UploadHttpApi
    with ScalatestRouteTest
    with FileUploadUtils {

  implicit val typedSystem = system.toTyped
  override val ec: ExecutionContext = system.dispatcher
  override val log: LoggingAdapter = NoLogging
  override val sharding: ClusterSharding = ClusterSharding(typedSystem)
  override implicit val timeout: Timeout = Timeout(10.seconds)
  private implicit val routeTimeout = RouteTestTimeout(3.seconds)
  override val config: Config = ConfigFactory.load()

  val oAuth2Authorization = OAuth2Authorization(
    log,
    new KeycloakTokenVerifier(
      KeycloakDeploymentBuilder.build(
        getClass.getResourceAsStream("/keycloak.json")
      )
    )
  )

  val kafkaHosts = config.getString("kafka.hosts")

  val period = "2018"

  val institutionProbe = TestProbe[InstitutionEvent]("institution-probe")
  val filingProbe = TestProbe[FilingEvent]("filing-probe")
  val submissionProbe = TestProbe[SubmissionEvent]("submission-probe")
  val maybeSubmissionProbe =
    TestProbe[Option[Submission]]("submission-probe")

  val sampleInstitution = institutionGen
    .suchThat(_.LEI != "")
    .sample
    .getOrElse(Institution.empty.copy(LEI = "AAA"))

  val sampleFiling = filingGen.sample
    .getOrElse(Filing())
    .copy(lei = sampleInstitution.LEI)
    .copy(period = period)

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
    implicit val embeddedKafkaConfig: EmbeddedKafkaConfig = EmbeddedKafkaConfig(
      9092,
      2182,
      Map("offsets.topic.replication.factor" -> "1",
          "zookeeper.connection.timeout.ms" -> "20000"))
    EmbeddedKafka.start()

    InstitutionPersistence.startShardRegion(sharding)
    SubmissionManager.startShardRegion(sharding)
    FilingPersistence.startShardRegion(sharding)
    SubmissionPersistence.startShardRegion(sharding)

    val institutionPersistence =
      sharding.entityRefFor(
        InstitutionPersistence.typeKey,
        s"${InstitutionPersistence.name}-${sampleInstitution.LEI}")
    institutionPersistence ! CreateInstitution(sampleInstitution,
                                               institutionProbe.ref)
    institutionProbe.expectMessage(InstitutionCreated(sampleInstitution))

    val filingPersistence =
      sharding.entityRefFor(
        FilingPersistence.typeKey,
        s"${FilingPersistence.name}-${sampleInstitution.LEI}-$period"
      )
    filingPersistence ! CreateFiling(sampleFiling, filingProbe.ref)

    val submissionPersistence =
      sharding.entityRefFor(
        SubmissionPersistence.typeKey,
        s"${SubmissionPersistence.name}-${sampleSubmission.id.toString}")

    submissionPersistence ! CreateSubmission(sampleSubmission.id,
                                             submissionProbe.ref)
    submissionProbe.expectMessageType[SubmissionCreated]
  }

  override def afterAll(): Unit = {
    EmbeddedKafka.stop()
    super.afterAll()
  }

  val url =
    s"/institutions/${sampleInstitution.LEI}/filings/$period/submissions/1"

  val badUrl =
    s"/institutions/${sampleInstitution.LEI}/filings/$period/submissions/2"

  val ts = tsGen.sample.getOrElse(TransmittalSheet())
  val tsCsv = ts.toCSV + "\n"
  val tsSource = Source.fromIterator(() => List(tsCsv).iterator)

  val larList = larNGen(10).suchThat(_.nonEmpty).sample.getOrElse(Nil)
  val larCsv = larList.map(lar => lar.toCSV + "\n")
  val larSource = Source.fromIterator(() => larCsv.iterator)

  val hmdaFileCsv = List(tsCsv) ++ larCsv
  val hmdaFile = multiPartFile(hmdaFileCsv.mkString(""), "sample.txt")

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val consumerSettings =
    ConsumerSettings(consumerConfig,
                     new StringDeserializer,
                     new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

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
        response.message mustBe s"Submission ${sampleInstitution.LEI}-${period}-2 not available for upload"
      }
    }
  }

}
