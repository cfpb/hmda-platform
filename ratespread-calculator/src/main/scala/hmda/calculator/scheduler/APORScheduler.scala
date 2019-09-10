package hmda.calculator.scheduler

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{
  MemoryBufferType,
  ObjectMetadata,
  S3Attributes,
  S3Settings
}
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.calculator.apor.{AporListEntity, FixedRate, RateType, VariableRate}
import hmda.calculator.parser.APORCsvParser
import hmda.calculator.scheduler.schedules.Schedules.APORScheduler

class APORScheduler extends HmdaActor {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("APORScheduler", self, APORScheduler)
  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("APORScheduler")
  }

  override def receive: Receive = {

    case APORScheduler | "initialize" =>
      val aporConfig =
        ConfigFactory.load("application.conf").getConfig("hmda.apors")
      val fixedRateFileName = aporConfig.getString("fixed.rate.fileName")
      val variableRateFileName = aporConfig.getString("variable.rate.fileName ")

      val awsConfig = ConfigFactory.load("application.conf").getConfig("aws")
      val accessKeyId = awsConfig.getString("access-key-id")
      val secretAccess = awsConfig.getString("secret-access-key")
      val region = awsConfig.getString("region")
      val bucket = awsConfig.getString("public-bucket")
      val environment = awsConfig.getString("environment")
      val fixedBucketKey = s"$environment/apor/$fixedRateFileName"
      val variableBucketKey = s"$environment/apor/$variableRateFileName"

      val awsCredentialsProvider = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(accessKeyId, secretAccess))

      val awsRegionProvider = new AwsRegionProvider {
        override def getRegion: String = region
      }

      val s3Settings = S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        false,
        None,
        ListBucketVersion2
      )

      loadAPOR(s3Settings, bucket, fixedBucketKey, FixedRate)
      loadAPOR(s3Settings, bucket, variableBucketKey, VariableRate)
  }

  def framing: Flow[ByteString, ByteString, NotUsed] = {
    Framing.delimiter(ByteString("\n"),
                      maximumFrameLength = 65536,
                      allowTruncation = true)
  }

  def loadAPOR(s3Settings: S3Settings,
               bucket: String,
               bucketKey: String,
               rateType: RateType) {

    val s3FixedSource
      : Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] =
      S3.download(bucket, bucketKey)
        .withAttributes(S3Attributes.settings(s3Settings))

    s3FixedSource
      .flatMapConcat(_.get._1)
      .via(framing)
      .drop(1)
      .map(s => s.utf8String)
      .map(s => APORCsvParser(s))
      .map(apor => AporListEntity.AporOperation(apor, rateType))
      .runWith(Sink.ignore)

    log.info(
      "Loaded APOR data from S3 for: " + rateType + " from:  " + bucket + "/" + bucketKey)
  }
}
