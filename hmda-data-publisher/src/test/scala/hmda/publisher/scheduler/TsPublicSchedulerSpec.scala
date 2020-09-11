package hmda.publisher.scheduler

import akka.actor.{ ActorSystem, Props }
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Attributes, S3Settings }
import akka.stream.scaladsl.Sink
import akka.testkit.{ ImplicitSender, TestKit }
import com.adobe.testing.s3mock.S3MockApplication
import hmda.publisher.query.component.PublisherComponent2018
import hmda.publisher.scheduler.schedules.Schedules
import hmda.query.ts.TransmittalSheetEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Millis, Minute, Span }
import org.scalatest.{ BeforeAndAfterEach, FreeSpecLike, Matchers }
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class TsPublicSchedulerSpec
  extends TestKit(ActorSystem("ts-public-scheduler-spec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with PublisherComponent2018
    with EmbeddedPostgres
    with Eventually
    with BeforeAndAfterEach {

  var s3mock: S3MockApplication = _
  val tsRepository              = new TransmittalSheetRepository2018(dbConfig)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(1, Minute), interval = Span(100, Millis))

  override def bootstrapSqlFile: String = ""

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(tsRepository.createSchema(), 30.seconds)
    val properties: mutable.Map[String, Object] =
      mutable // S3 Mock mutates the map so we cannot use an immutable map :(
        .Map(
          S3MockApplication.PROP_HTTPS_PORT      -> S3MockApplication.DEFAULT_HTTPS_PORT,
          S3MockApplication.PROP_HTTP_PORT       -> S3MockApplication.DEFAULT_HTTP_PORT,
          S3MockApplication.PROP_SILENT          -> true,
          S3MockApplication.PROP_INITIAL_BUCKETS -> "cfpb-hmda-public,cfpb-hmda-export"
        )
        .map { case (k, v) => (k, v.asInstanceOf[Object]) }

    s3mock = S3MockApplication.start(properties.asJava)
  }

  override def afterAll(): Unit = {
    s3mock.stop()
    super.afterAll()
  }

  "TsPublicScheduler should publish data to S3 for 2018" in {
    val data = TransmittalSheetEntity(lei = "EXAMPLE-LEI-1")
    whenReady(tsRepository.insert(data))(_ shouldBe 1)
    whenReady(tsRepository.insert(data.copy(lei = "EXAMPLE-LEI-2", isQuarterly = Some(true), year = 2018, quarter = 1)))(_ shouldBe 1)

    val awsConfig    = system.settings.config.getConfig("public-aws")
    val accessKeyId  = awsConfig.getString("public-access-key-id")
    val secretAccess = awsConfig.getString("public-secret-access-key ")
    val region       = awsConfig.getString("public-region")
    val bucket       = awsConfig.getString("public-s3-bucket")
    val environment  = awsConfig.getString("public-environment")

    val awsCredentialsProvider               = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
    val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

    val s3Settings = S3Settings(system)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(awsCredentialsProvider)
      .withS3RegionProvider(awsRegionProvider)
      .withListBucketApiVersion(ListBucketVersion2)

    val fileNamePSV = "2018_ts.txt"
    val key         = s"$environment/dynamic-data/2018/$fileNamePSV"

    val actor = system.actorOf(Props[TsPublicScheduler], "ts-public-scheduler")
    actor ! Schedules.TsPublicScheduler2018

    eventually {
      val result =
        S3.getObjectMetadata(bucket, key)
          .withAttributes(S3Attributes.settings(s3Settings))
          .runWith(Sink.head)

      whenReady(result)(_ should not be empty)
    }

    watch(actor)
    system.stop(actor)
    expectTerminated(actor)
  }
}