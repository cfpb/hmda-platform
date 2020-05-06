package hmda.publisher.scheduler

import akka.actor.{ ActorSystem, Props }
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Attributes, S3Settings }
import akka.stream.scaladsl.Sink
import akka.testkit.{ ImplicitSender, TestKit }
import com.adobe.testing.s3mock.S3MockApplication
import hmda.publisher.query.component.PublisherComponent2018
import hmda.publisher.query.lar._
import hmda.publisher.scheduler.schedules.Schedules
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Millis, Minutes, Span }
import org.scalatest.{ FreeSpecLike, Matchers }
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.Await
import scala.concurrent.duration._

class LarPublicSchedulerSpec
  extends TestKit(ActorSystem("lar-publisher-scheduler-spec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with PublisherComponent2018
    with EmbeddedPostgres
    with Eventually {
  import dbConfig.profile.api._

  var s3mock: S3MockApplication = _
  val modifiedLarRepository2018 = new ModifiedLarRepository2018(dbConfig)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(modifiedLarRepository2018.createSchema(), 30.seconds)

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

  override def bootstrapSqlFile: String = ""

  "LarPublicScheduler should publish data to the public S3 bucket" in {
    whenReady(
      modifiedLarRepository2018.insert(
        ModifiedLarEntityImpl(
          ModifiedLarPartOne(filingYear = Some(2018), lei = "EXAMPLE-LEI", msaMd = Some(3378)),
          ModifiedLarPartTwo(),
          ModifiedLarPartThree(),
          ModifiedLarPartFour(),
          ModifiedLarPartFive(),
          ModifiedLarPartSix()
        )
      )
    )(res => res should be > 0)

    whenReady(
      modifiedLarRepository2018.insert(
        ModifiedLarEntityImpl(
          ModifiedLarPartOne(filingYear = Some(2018), lei = "EXAMPLE-LEI-2", msaMd = Some(33789)),
          ModifiedLarPartTwo(businessOrCommercial = Some(1)),
          ModifiedLarPartThree(),
          ModifiedLarPartFour(),
          ModifiedLarPartFive(),
          ModifiedLarPartSix()
        )
      )
    )(res => res should be > 0)

    val actor = system.actorOf(Props[LarPublicScheduler], "lar-public-scheduler")
    actor ! Schedules.LarPublicScheduler2018

    val awsConfig                            = system.settings.config.getConfig("public-aws")
    val accessKeyId                          = awsConfig.getString("public-access-key-id")
    val secretAccess                         = awsConfig.getString("public-secret-access-key ")
    val region                               = awsConfig.getString("public-region")
    val bucket                               = awsConfig.getString("public-s3-bucket")
    val environment                          = awsConfig.getString("public-environment")
    val awsCredentialsProvider               = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
    val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

    val s3Settings = S3Settings(system)
      .withBufferType(MemoryBufferType)
      .withCredentialsProvider(awsCredentialsProvider)
      .withS3RegionProvider(awsRegionProvider)
      .withListBucketApiVersion(ListBucketVersion2)

    // it takes a while for the data to appear in S3 so keep rerunning the following block until the assertion is true
    eventually {
      val result =
        S3.getObjectMetadata(bucket, s"$environment/dynamic-data/2018/2018_lar.txt")
          .withAttributes(S3Attributes.settings(s3Settings))
          .runWith(Sink.head)

      whenReady(result)(_ should not be empty)
    }

    watch(actor)
    system.stop(actor)
    expectTerminated(actor)
  }
}