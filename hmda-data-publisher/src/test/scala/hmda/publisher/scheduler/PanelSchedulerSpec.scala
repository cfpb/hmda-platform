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
import hmda.publisher.query.component.{ InstitutionEmailComponent, PublisherComponent2018, PublisherComponent2019 }
import hmda.publisher.query.panel.{ InstitutionEmailEntity, InstitutionEntity }
import hmda.publisher.scheduler.schedules.Schedules
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
import scala.concurrent.duration._
import scala.concurrent.{ Await, Future }

class PanelSchedulerSpec
  extends TestKit(ActorSystem("panel-scheduler-spec"))
    with ImplicitSender
    with FreeSpecLike
    with Matchers
    with ScalaFutures
    with PublisherComponent2018
    with PublisherComponent2019
    with InstitutionEmailComponent
    with EmbeddedPostgres
    with Eventually
    with BeforeAndAfterEach {
  import dbConfig.profile.api._

  var s3mock: S3MockApplication = _

  val institutionRepository2018 = new InstitutionRepository2018(dbConfig)
  val institutionRepository2019 = new InstitutionRepository2019(dbConfig)
  val emailRepository           = new InstitutionEmailsRepository2018(dbConfig)

  val awsConfig: Config    = system.settings.config.getConfig("private-aws")
  val accessKeyId: String  = awsConfig.getString("private-access-key-id")
  val secretAccess: String = awsConfig.getString("private-secret-access-key ")
  val region: String       = awsConfig.getString("private-region")
  val bucket: String       = awsConfig.getString("private-s3-bucket")
  val environment: String  = awsConfig.getString("private-environment")
  val awsCredentialsProvider: StaticCredentialsProvider =
    StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
  val awsRegionProvider: AwsRegionProvider = () => Region.of(region)

  val fullDate: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-")

  val s3Settings: S3Settings = S3Settings(system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProvider)
    .withS3RegionProvider(awsRegionProvider)
    .withListBucketApiVersion(ListBucketVersion2)

  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))

  override def beforeAll(): Unit = {
    super.beforeAll()
    Await.ready(institutionRepository2018.createSchema(), 30.seconds)
    Await.ready(institutionRepository2019.createSchema(), 30.seconds)
    Await.ready(emailRepository.createSchema(), 30.seconds)

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

  override def afterEach(): Unit = {
    Await.ready(
      Future.sequence(
        List(
          dbConfig.db.run(institutionRepository2018.table.delete),
          dbConfig.db.run(institutionRepository2019.table.delete),
          dbConfig.db.run(institutionEmailsTable2018.delete)
        )
      ),
      30.seconds
    )
    super.afterEach()
  }

  override def afterAll(): Unit = {
    Await.ready(
      Future.sequence(
        List(
          institutionRepository2018.dropSchema(),
          institutionRepository2019.dropSchema(),
          emailRepository.dropSchema()
        )
      ),
      30.seconds
    )
    s3mock.stop()
    super.afterAll()
  }

  override def cleanupAction = DBIO.successful(1)

  override def bootstrapSqlFile: String = ""

  "PanelScheduler should publish data to the private S3 bucket for 2018" in {
    val data = InstitutionEntity(lei = "EXAMPLE-LEI-1", hmdaFiler = true)

    whenReady(for {
      r1 <- institutionRepository2018.insert(data)
      r2 <- institutionRepository2018.insert(data.copy(lei = "EXAMPLE-LEI-2"))
      _ <- dbConfig.db.run(
        institutionEmailsTable2018.insertOrUpdate(InstitutionEmailEntity(lei = "EXAMPLE-LEI-1", emailDomain = "example-lei-1.com"))
      )
      _ <- dbConfig.db.run(
        institutionEmailsTable2018.insertOrUpdate(InstitutionEmailEntity(lei = "EXAMPLE-LEI-2", emailDomain = "example-lei-2.com"))
      )
    } yield r1 + r2)(_ == 2)

    val actor = system.actorOf(Props[PanelScheduler], "panel-scheduler")
    actor ! Schedules.PanelScheduler2018

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = formattedDate + "2018_panel.txt"
    val key           = s"$environment/panel/$fileName"

    // it takes a while for the data to appear in S3 so keep rerunning the following block until the assertion is true
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

  "PanelScheduler should publish data to the private S3 bucket for 2019" in {
    val data = InstitutionEntity(lei = "EXAMPLE-LEI-10", hmdaFiler = true)

    whenReady(for {
      r1 <- institutionRepository2019.insert(data)
      r2 <- institutionRepository2019.insert(data.copy(lei = "EXAMPLE-LEI-11"))
      _ <- dbConfig.db.run(
        institutionEmailsTable2018.insertOrUpdate(InstitutionEmailEntity(lei = "EXAMPLE-LEI-10 ", emailDomain = "example-lei-10.com"))
      )
      _ <- dbConfig.db.run(
        institutionEmailsTable2018.insertOrUpdate(InstitutionEmailEntity(lei = "EXAMPLE-LEI-11", emailDomain = "example-lei-11.com"))
      )
    } yield r1 + r2)(_ == 2)

    val actor = system.actorOf(Props[PanelScheduler], "panel-scheduler")
    actor ! Schedules.PanelScheduler2019

    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = formattedDate + "2019_panel.txt"
    val key           = s"$environment/panel/$fileName"

    // it takes a while for the data to appear in S3 so keep rerunning the following block until the assertion is true
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