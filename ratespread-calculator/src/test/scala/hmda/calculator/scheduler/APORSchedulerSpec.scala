//package hmda.calculator.scheduler
//
//import java.time.LocalDate
//import java.time.format.DateTimeFormatter
//
//import akka.actor.ActorSystem
//import akka.actor.typed.scaladsl.adapter._
//import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
//import akka.stream.alpakka.s3.scaladsl.S3
//import akka.stream.alpakka.s3.{ MemoryBufferType, S3Attributes, S3Settings }
//import akka.stream.scaladsl.{ Sink, Source }
//import akka.testkit.{ ImplicitSender, TestKit }
//import akka.util.ByteString
//import com.adobe.testing.s3mock.S3MockApplication
//import hmda.calculator.apor.APOR
//import hmda.calculator.scheduler.APORScheduler.Command
//import org.scalatest.concurrent.{ Eventually, ScalaFutures }
//import org.scalatest.time.{ Millis, Minutes, Span }
//import org.scalatest.{ BeforeAndAfterAll, FreeSpecLike, Matchers }
//import software.amazon.awssdk.auth.credentials.{ AwsBasicCredentials, StaticCredentialsProvider }
//import software.amazon.awssdk.regions.Region
//import software.amazon.awssdk.regions.providers.AwsRegionProvider
//
//import scala.collection.JavaConverters._
//import scala.collection.mutable
//import scala.util.Try
//
//class APORSchedulerSpec
//  extends TestKit(ActorSystem("apor-scheduler-spec"))
//    with ImplicitSender
//    with FreeSpecLike
//    with Matchers
//    with ScalaFutures
//    with BeforeAndAfterAll
//    with Eventually {
//
//  override implicit def patienceConfig: PatienceConfig = PatienceConfig(timeout = Span(2, Minutes), interval = Span(100, Millis))
//
//  var s3mock: S3MockApplication = _
//
//  "APORScheduler should publish data to the S3 bucket" in {
//
//    val awsConfig                            = system.settings.config.getConfig("aws")
//    val accessKeyId                          = awsConfig.getString("access-key-id")
//    val secretAccess                         = awsConfig.getString("secret-access-key ")
//    val region                               = awsConfig.getString("region")
//    val bucket                               = awsConfig.getString("public-bucket")
//    val environment                          = awsConfig.getString("environment")
//    val awsCredentialsProvider               = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
//    val awsRegionProvider: AwsRegionProvider = () => Region.of(region)
//
//    val s3Settings = S3Settings(system)
//      .withBufferType(MemoryBufferType)
//      .withCredentialsProvider(awsCredentialsProvider)
//      .withS3RegionProvider(awsRegionProvider)
//      .withListBucketApiVersion(ListBucketVersion2)
//
//    val config               = system.settings.config
//    val aporConfig           = config.getConfig("hmda.apors")
//    val fixedRateFileName    = aporConfig.getString("fixed.rate.fileName")
//    val variableRateFileName = aporConfig.getString("variable.rate.fileName ")
//    val fixedBucketKey       = s"$environment/apor/$fixedRateFileName"
//    val variableBucketKey    = s"$environment/apor/$variableRateFileName"
//
//    val exampleAPOR: APOR = APOR(
//      LocalDate.parse("2018-03-22", DateTimeFormatter.ISO_LOCAL_DATE),
//      Seq(1.01, 1.02, 1.03, 1.04, 1.05, 1.06, 1.07, 1.08, 1.09, 1.1, 1.11, 1.12, 1.13, 1.14, 1.15, 1.16, 1.17, 1.18, 1.19, 1.2, 1.21, 1.22,
//        1.23, 1.24, 1.25, 1.26, 1.27, 1.28, 1.29, 1.3, 1.31, 1.32, 1.33, 1.34, 1.35, 1.36, 1.37, 1.38, 1.39, 1.40, 1.41, 1.42, 1.43, 1.44,
//        1.45, 1.46, 1.47, 1.48, 1.49, 1.5)
//    )
//
//    val sinkFixed    = S3.multipartUpload(bucket, fixedBucketKey).withAttributes(S3Attributes.settings(s3Settings))
//    val sinkVariable = S3.multipartUpload(bucket, variableBucketKey).withAttributes(S3Attributes.settings(s3Settings))
//
//    whenReady(Source.single(ByteString(exampleAPOR.toCSV)).runWith(sinkFixed))(_ => ())
//    whenReady(Source.single(ByteString(exampleAPOR.toCSV)).runWith(sinkVariable))(_ => ())
//
//    val actor = system.spawn(APORScheduler(), APORScheduler.name)
//    actor ! Command.Initialize
//
//    eventually {
//      val result =
//        S3.getObjectMetadata(bucket, fixedBucketKey)
//          .withAttributes(S3Attributes.settings(s3Settings))
//          .runWith(Sink.head)
//      whenReady(result)(_ should not be empty)
//    }
//
//    eventually {
//      val result =
//        S3.getObjectMetadata(bucket, variableBucketKey)
//          .withAttributes(S3Attributes.settings(s3Settings))
//          .runWith(Sink.head)
//      whenReady(result)(_ should not be empty)
//    }
//
//    watch(actor.toClassic)
//    system.stop(actor.toClassic)
//    expectTerminated(actor.toClassic)
//  }
//
//  override def beforeAll(): Unit = {
//    super.beforeAll()
//    val properties: mutable.Map[String, Object] =
//      mutable // S3 Mock mutates the map so we cannot use an immutable map :(
//        .Map(
//          S3MockApplication.PROP_HTTPS_PORT      -> S3MockApplication.DEFAULT_HTTPS_PORT,
//          S3MockApplication.PROP_HTTP_PORT       -> S3MockApplication.DEFAULT_HTTP_PORT,
//          S3MockApplication.PROP_SILENT          -> true,
//          S3MockApplication.PROP_INITIAL_BUCKETS -> "cfpb-hmda-public,cfpb-hmda-export"
//        )
//        .map { case (k, v) => (k, v.asInstanceOf[Object]) }
//
//    s3mock = S3MockApplication.start(properties.asJava)
//  }
//
//  override def afterAll(): Unit = {
//    Try(s3mock.stop())
//    super.afterAll()
//  }
//
//}