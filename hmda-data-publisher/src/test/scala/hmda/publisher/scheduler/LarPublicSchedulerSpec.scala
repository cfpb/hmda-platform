package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.actor.{ ActorSystem, Props }
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{ MemoryBufferType, S3Attributes, S3Settings }
import akka.stream.scaladsl.Sink
import akka.testkit.{ ImplicitSender, TestKit }
import com.adobe.testing.s3mock.S3MockApplication
import com.typesafe.config.Config
import hmda.publisher.query.component.{ PublisherComponent2018, PublisherComponent2019, PublisherComponent2020 }
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
import scala.concurrent.{ Await, Future }
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._

class LarSchedulerSpec
  extends TestKit(ActorSystem("lar-scheduler-spec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with EmbeddedPostgres
    with Eventually {
  import dbConfig.profile.api._

  var s3mock: S3MockApplication = _

  val larRepository2018 = new LarRepository2018(dbConfig)
  val larRepository2019 = new LarRepository2019(dbConfig)
  val larRepository2020 = new LarRepository2020(dbConfig)

  val awsConfig: Config    = system.settings.config.getConfig("private-aws")
  val accessKeyId: String  = awsConfig.getString("private-access-key-id")
  val secretAccess: String = awsConfig.getString("private-secret-access-key ")
  val region: String       = awsConfig.getString("private-region")
  val bucket: String       = awsConfig.getString("private-s3-bucket")
  val environment: String  = awsConfig.getString("private-environment")
  val awsCredentialsProvider: StaticCredentialsProvider =
    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
  val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

  val fullDate: DateTimeFormatter          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  val fullDateQuarterly: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  val s3Settings: S3Settings = S3Settings(system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProvider)
    .withS3RegionProvider(awsRegionProvider)
    .withListBucketApiVersion(ListBucketVersion2)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(larRepository2018.createSchema(), 30.seconds)
    Await.ready(larRepository2019.createSchema(), 30.seconds)

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

  // We cannot rely on the larRepo's create and delete schema since it is broken (see PublisherComponent2020#LarRepository2020 for more information)
  override def bootstrapSqlFile: String = "loanapplicationregister2020.sql"

  "LarScheduler should publish data to the private S3 bucket for 2018" in {
    val data = LarEntityImpl2018(
      LarPartOne2018(lei = "EXAMPLE-LEI-1"),
      LarPartTwo2018(),
      LarPartThree2018(),
      LarPartFour2018(),
      LarPartFive2018(),
      LarPartSix2018()
    )

    whenReady(for {
      r1 <- larRepository2018.insert(data)
      r2 <- larRepository2018.insert(data.copy(larPartOne = data.larPartOne.copy(lei = "EXAMPLE-LEI-2")))
    } yield r1 + r2)(_ == 2)

    val actor = system.actorOf(Props[LarScheduler], "lar-scheduler")
    actor ! Schedules.LarScheduler2018

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = formattedDate + "2018_lar.txt"
    val key           = s"$environment/lar/$fileName"

    // it takes a while for the data to appear in S3 so keep rerunning the following block until the assertion is true
    val metadata = eventually {
      val result =
        S3.getObjectMetadata(bucket, key)
          .withAttributes(S3Attributes.settings(s3Settings))
          .runWith(Sink.head)

      whenReady(result)(metadataOpt => {
        metadataOpt should not be empty
        metadataOpt.get
      })
    }
    val rowCount = metadata.metadata.find(_.lowercaseName() == s"x-amz-meta-${LarScheduler.entriesCountMetaName}").map(_.value())
    assert(rowCount.contains("2"))

    watch(actor)
    system.stop(actor)
    expectTerminated(actor)
  }

  "LarScheduler should publish data to the private S3 bucket for 2019" in {
    val data = LarEntityImpl2019(
      LarPartOne2019(lei = "EXAMPLE-LEI-1"),
      LarPartTwo2019(),
      LarPartThree2019(),
      LarPartFour2019(),
      LarPartFive2019(),
      LarPartSix2019(),
      LarPartSeven2019()
    )

    whenReady(for {
      r1 <- larRepository2019.insert(data)
      r2 <- larRepository2019.insert(data.copy(larPartOne = data.larPartOne.copy(lei = "EXAMPLE-LEI-2")))
    } yield r1 + r2)(_ == 2)

    val actor = system.actorOf(Props[LarScheduler], "lar-scheduler")
    actor ! Schedules.LarScheduler2019

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = formattedDate + "2019_lar.txt"
    val key           = s"$environment/lar/$fileName"

    // it takes a while for the data to appear in S3 so keep rerunning the following block until the assertion is true
    val metadata = eventually {
      val result =
        S3.getObjectMetadata(bucket, key)
          .withAttributes(S3Attributes.settings(s3Settings))
          .runWith(Sink.head)

      whenReady(result)(metadataOpt => {
        metadataOpt should not be empty
        metadataOpt.get
      })
    }
    val rowCount = metadata.metadata.find(_.lowercaseName() == s"x-amz-meta-${LarScheduler.entriesCountMetaName}").map(_.value())
    assert(rowCount.contains("2"))

    watch(actor)
    system.stop(actor)
    expectTerminated(actor)
  }

  "LarScheduler should publish data to the private S3 bucket for LarSchedulerLoanLimit2019" in {
    val data = LarEntityImpl2019(
      LarPartOne2019(lei = "EXAMPLE-LEI-3"),
      LarPartTwo2019(),
      LarPartThree2019(),
      LarPartFour2019(),
      LarPartFive2019(),
      LarPartSix2019(),
      LarPartSeven2019()
    )

    whenReady(for {
      r1 <- larRepository2019.insert(data)
      r2 <- larRepository2019.insert(data.copy(larPartOne = data.larPartOne.copy(lei = "EXAMPLE-LEI-4")))
    } yield r1 + r2)(_ == 2)

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = "2019F_AGY_LAR_withFlag_" + s"$formattedDate" + "2019_lar.txt"
    val key           = s"$environment/lar/$fileName"

    val actor = system.actorOf(Props[LarScheduler], "lar-scheduler")
    actor ! Schedules.LarSchedulerLoanLimit2019

    val metadata = eventually {
      val result =
        S3.getObjectMetadata(bucket, key)
          .withAttributes(S3Attributes.settings(s3Settings))
          .runWith(Sink.head)

      whenReady(result)(metadataOpt => {
        metadataOpt should not be empty
        metadataOpt.get
      })
    }
    val rowCount = metadata.metadata.find(_.lowercaseName() == s"x-amz-meta-${LarScheduler.entriesCountMetaName}").map(_.value())
    assert(rowCount.contains("2"))

    watch(actor)
    system.stop(actor)
    expectTerminated(actor)
  }

  "LarScheduler should publish data to the private S3 bucket for LarSchedulerQuarterly2020" in {
    val data = LarEntityImpl2020(
      LarPartOne2020(lei = "EXAMPLE-LEI"),
      LarPartTwo2020(),
      LarPartThree2020(),
      LarPartFour2020(),
      LarPartFive2020(),
      LarPartSix2020(),
      LarPartSeven2020()
    )

    whenReady(larRepository2020.insert(data))(_ shouldBe 1)

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDateQuarterly.format(now)
    val fileName      = s"$formattedDate" + "quarterly_2020_lar.txt"
    val key           = s"$environment/lar/$fileName"

    val actor = system.actorOf(Props[LarScheduler], "lar-scheduler")
    actor ! Schedules.LarSchedulerQuarterly2020

    val metadata = eventually {
      val result =
        S3.getObjectMetadata(bucket, key)
          .withAttributes(S3Attributes.settings(s3Settings))
          .runWith(Sink.head)

      whenReady(result)(metadataOpt => {
        metadataOpt should not be empty
        metadataOpt.get
      })
    }
    val rowCount = metadata.metadata.find(_.lowercaseName() == s"x-amz-meta-${LarScheduler.entriesCountMetaName}").map(_.value())
    assert(rowCount.contains("1"))

    watch(actor)
    system.stop(actor)
    expectTerminated(actor)
  }
}