package hmda.publisher.qa

import akka.actor.ActorSystem
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, S3Attributes, S3Headers, S3Settings}
import akka.stream.scaladsl.{Sink, Source}
import akka.util.ByteString
import com.adobe.testing.s3mock.S3MockApplication
import hmda.publisher.util.MattermostNotifier
import org.scalamock.scalatest.MockFactory
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.{BeforeAndAfterAll, FreeSpec}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.collection.mutable
import scala.concurrent.Future
import scala.jdk.CollectionConverters._

class QAFilePersistorTest extends FreeSpec with BeforeAndAfterAll with ScalaFutures with IntegrationPatience with MockFactory {

  implicit val system           = ActorSystem("as")
  implicit val ec               = system.dispatcher
  var s3Mock: S3MockApplication = _

  override protected def beforeAll(): Unit = {
    super.beforeAll()
    s3Mock = S3MockApplication.start(s3MockProperties.asJava)
  }

  override def afterAll(): Unit = {
    super.afterAll()
    Option(s3Mock).foreach(_.stop())
  }

  val s3Settings = S3Settings(system)
    .withBufferType(MemoryBufferType)
    .withS3RegionProvider(() => Region.AP_EAST_1)
    .withListBucketApiVersion(ListBucketVersion2)

  "smoke test" in {
    val notifier      = stub[MattermostNotifier]
    val persistor     = new QAFilePersistor(notifier)
    val repo          = new TestQaRepository
    val (bucket, key) = ("bucket", "key")
    val data =
      """Header
        |Line1
        |Line2
        |Line3
        |Line4""".stripMargin

    val headers = S3Headers()
    S3.makeBucketSource(bucket)
      .withAttributes(S3Attributes.settings(s3Settings))
      .runWith(Sink.ignore)
      .futureValue
    S3.putObject(bucket, key, Source.single(ByteString(data)), data.getBytes.length, s3Headers = headers)
      .withAttributes(S3Attributes.settings(s3Settings))
      .runWith(Sink.ignore)
      .futureValue
    (notifier.report _).when(*).returns(Future.unit)

    val spec = QAFileSpec(
      bucket = bucket,
      key = key,
      s3Settings = s3Settings,
      withHeaderLine = true,
      parseLine = row => TestEntity(row),
      repository = repo
    )

    persistor.fetchAndPersist(spec).futureValue

   // assert(repo.deletions == List(spec.filePath))
    assert(
      repo.batches == List(
        (
          List(TestEntity("Line1"), TestEntity("Line2"), TestEntity("Line3"), TestEntity("Line4")),
          spec.filePath
        )
      )
    )
    (notifier.report _).verify("bucket/key loaded to qa_repo with 4 rows")

  }

  case class TestEntity(row: String)

  class TestQaRepository extends QARepository[TestEntity] {
    var deletions                  = List[Any]()
    var batches                    = List[(Seq[TestEntity], String)]()
    override def tableName: String = "qa_repo"
    override def deletePreviousRecords(timeStamp: Long): Future[Unit] = {
      deletions = deletions :+ timeStamp
      Future.unit
    }

    override def saveAll(batch: Seq[TestEntity], fileName: String,timestamp: Long): Future[Unit] = {
      batches = batches :+ ((batch, fileName))
      Future.unit
    }
  }

  val s3MockProperties: mutable.Map[String, Object] =
    mutable // S3 Mock mutates the map so we cannot use an immutable map :(
      .Map(
        S3MockApplication.PROP_HTTPS_PORT      -> S3MockApplication.DEFAULT_HTTPS_PORT,
        S3MockApplication.PROP_HTTP_PORT       -> S3MockApplication.DEFAULT_HTTP_PORT,
        S3MockApplication.PROP_SILENT          -> true,
        S3MockApplication.PROP_INITIAL_BUCKETS -> "cfpb-hmda-public-dev"
      )
      .map { case (k, v) => (k, v.asInstanceOf[Object]) }


}