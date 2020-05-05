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
import hmda.publisher.query.component.{ PublisherComponent2018, PublisherComponent2019, PublisherComponent2020 }
import hmda.publisher.scheduler.schedules.Schedules
import hmda.query.ts.TransmittalSheetEntity
import hmda.utils.EmbeddedPostgres
import org.scalatest.concurrent.{ Eventually, ScalaFutures }
import org.scalatest.time.{ Millis, Minutes, Span }
import org.scalatest.{ BeforeAndAfterEach, FreeSpecLike, Matchers }
import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.collection.JavaConverters._
import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.{ Await, Future }
import scala.concurrent.duration._

class TsSchedulerSpec
  extends TestKit(ActorSystem("ts-scheduler-spec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with EmbeddedPostgres
    with Eventually
    with BeforeAndAfterEach {
  import dbConfig.profile.api._
  import dbConfig._

  var s3mock: S3MockApplication = _

  val tsRepository2018 = new TransmittalSheetRepository2018(dbConfig)
  val tsRepository2019 = new TransmittalSheetRepository2019(dbConfig)
  val tsRepository2020 = new TransmittalSheetRepository2020(dbConfig)

  val fullDate          = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  override def cleanupAction: DBIO[Int] = DBIO.successful(1)

  override def bootstrapSqlFile: String = ""

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
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

  override def beforeEach(): Unit = {
    super.beforeEach()
    Await.ready(
      Future.sequence(
        List(
          tsRepository2018.createSchema(),
          tsRepository2019.createSchema(),
          tsRepository2020.createSchema(),
          db.run(tsRepository2018.table.delete),
          db.run(tsRepository2019.table.delete),
          db.run(tsRepository2020.table.delete)
        )
      ),
      30.seconds
    )
  }

  override def afterEach(): Unit = {
    Await.ready(
      Future.sequence(
        List(tsRepository2018.dropSchema(), tsRepository2019.dropSchema(), tsRepository2020.dropSchema())
      ),
      30.seconds
    )
    super.afterEach()
  }

  val awsConfig    = system.settings.config.getConfig("private-aws")
  val accessKeyId  = awsConfig.getString("private-access-key-id")
  val secretAccess = awsConfig.getString("private-secret-access-key ")
  val region       = awsConfig.getString("private-region")
  val bucket       = awsConfig.getString("private-s3-bucket")
  val environment  = awsConfig.getString("private-environment")
  val year         = awsConfig.getString("private-year")

  val awsCredentialsProvider               = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
  val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

  val s3Settings = S3Settings(system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProvider)
    .withS3RegionProvider(awsRegionProvider)
    .withListBucketApiVersion(ListBucketVersion2)

  "TsScheduler uploads 2018 file to S3" in {
    whenReady(
      for {
        r1 <- tsRepository2018.insert(
          TransmittalSheetEntity(lei = "EXAMPLE-LEI-20", institutionName = "EXAMPLE-INSTITUTION", year = 2018)
        )
        r2 <- tsRepository2018.insert(
          TransmittalSheetEntity(lei = "EXAMPLE-LEI-21", institutionName = "EXAMPLE-INSTITUTION-1", year = 2018)
        )
      } yield r1 + r2
    )(_ shouldBe 2)

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = s"$formattedDate" + "2018_ts.txt"
    val key           = s"$environment/ts/$fileName"

    val actor = system.actorOf(Props[TsScheduler], "ts-scheduler")
    actor ! Schedules.TsScheduler2018

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

  "TsScheduler uploads 2019 file to S3" in {
    whenReady(
      for {
        r1 <- tsRepository2019.insert(
          TransmittalSheetEntity(lei = "EXAMPLE-LEI-20", institutionName = "EXAMPLE-INSTITUTION", year = 2019)
        )
        r2 <- tsRepository2019.insert(
          TransmittalSheetEntity(lei = "EXAMPLE-LEI-21", institutionName = "EXAMPLE-INSTITUTION-1", year = 2019)
        )
      } yield r1 + r2
    )(_ shouldBe 2)

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = s"$formattedDate" + "2019_ts.txt"
    val key           = s"$environment/ts/$fileName"

    val actor = system.actorOf(Props[TsScheduler], "ts-scheduler")
    actor ! Schedules.TsScheduler2019

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

  "TsScheduler uploads 2020 file to S3" in {
    whenReady(
      for {
        r1 <- tsRepository2020.insert(
          TransmittalSheetEntity(
            lei = "EXAMPLE-LEI-20",
            institutionName = "EXAMPLE-INSTITUTION",
            year = 2020,
            isQuarterly = Some(true)
          )
        )
        r2 <- tsRepository2020.insert(
          TransmittalSheetEntity(
            lei = "EXAMPLE-LEI-21",
            institutionName = "EXAMPLE-INSTITUTION-1",
            year = 2020,
            isQuarterly = Some(true)
          )
        )
      } yield r1 + r2
    )(_ shouldBe 2)

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDateQuarterly.format(now)
    val fileName      = s"$formattedDate" + "quarterly_2020_ts.txt"
    val key           = s"$environment/ts/$fileName"

    val actor = system.actorOf(Props[TsScheduler], "ts-scheduler")
    actor ! Schedules.TsSchedulerQuarterly2020

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