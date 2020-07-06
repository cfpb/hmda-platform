package hmda.calculator.scheduler

import akka.actor.typed.SupervisorStrategy
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.actor.typed.{Behavior, PostStop}
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, ObjectMetadata, S3Attributes, S3Settings}
import akka.stream.scaladsl.{Flow, Framing, Sink, Source}
import akka.stream.{Attributes, Materializer}
import akka.util.ByteString
import akka.{Done, NotUsed}
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.calculator.apor.{AporListEntity, FixedRate, RateType, VariableRate}
import hmda.calculator.parser.APORCsvParser
import org.slf4j.LoggerFactory
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

object APORScheduler {
  val logger = LoggerFactory.getLogger("hmda")

  val name: String = "APORScheduler"

  sealed trait Command
  object Command {
    case object Initialize extends Command
    type Initialize = Initialize.type

    private[APORScheduler] case object FinishedFixedRate extends Command
    type FinishedFixedRate = FinishedFixedRate.type

    private[APORScheduler] case object FinishedVariableRate extends Command
    type FinishedVariableRate = FinishedVariableRate.type
  }

  def apply(): Behavior[Command.Initialize] =
    Behaviors
      .supervise(
        Behaviors
          .setup[Command] { ctx =>
            implicit val ec: ExecutionContext = ctx.executionContext
            implicit val mat: Materializer    = Materializer(ctx.system)
            val config                        = ctx.system.settings.config
            val aporConfig                    = config.getConfig("hmda.apors")
            val fixedRateFileName             = aporConfig.getString("fixed.rate.fileName")
            val variableRateFileName          = aporConfig.getString("variable.rate.fileName ")

            val awsConfig         = ConfigFactory.load("application.conf").getConfig("aws")
            val accessKeyId       = awsConfig.getString("access-key-id")
            val secretAccess      = awsConfig.getString("secret-access-key")
            val region            = awsConfig.getString("region")
            val bucket            = awsConfig.getString("public-bucket")
            val environment       = awsConfig.getString("environment")
            val fixedBucketKey    = s"$environment/apor/$fixedRateFileName"
            val variableBucketKey = s"$environment/apor/$variableRateFileName"
            val quartz            = QuartzSchedulerExtension(ctx.system)

            val awsCredentialsProvider = StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKeyId, secretAccess))
            val awsRegionProvider: AwsRegionProvider = new AwsRegionProvider {
              override def getRegion: Region = Region.of(region)
            }

            val s3Settings = S3Settings(ctx.system.toClassic)
              .withBufferType(MemoryBufferType)
              .withCredentialsProvider(awsCredentialsProvider)
              .withS3RegionProvider(awsRegionProvider)
              .withListBucketApiVersion(ListBucketVersion2)

            val s3Attributes = S3Attributes.settings(s3Settings)

            quartz.schedule(APORScheduler.name, ctx.self.toClassic, Command.Initialize)

            Behaviors
              .receiveMessage[Command] {
                case Command.Initialize =>
                  loadAPOR(s3Attributes, bucket, fixedBucketKey, FixedRate).foreach(_ => ctx.self ! Command.FinishedFixedRate)
                  loadAPOR(s3Attributes, bucket, variableBucketKey, VariableRate).foreach(_ => ctx.self ! Command.FinishedVariableRate)
                  Behaviors.same

                case Command.FinishedFixedRate =>
                  logger.info("Loaded APOR data from S3 for: " + FixedRate + " from:  " + bucket + "/" + fixedBucketKey)
                  Behaviors.same

                case Command.FinishedVariableRate =>
                  logger.info("Loaded APOR data from S3 for: " + VariableRate + " from:  " + bucket + "/" + variableBucketKey)
                  Behaviors.same
              }
              .receiveSignal {
                case (_, PostStop) =>
                  val result = quartz.cancelJob(APORScheduler.name)
                  if (result)
                    logger.info(s"${APORScheduler.name} was successfully stopped by the Quartz Scheduler due to receiving a PostStop signal")
                  else logger.error(s"${APORScheduler.name} was unable to be cancelled by Quartz due after receiving a PostStop signal")
                  Behaviors.same[Command]
              }
          }
          .narrow[Command.Initialize]
      )
      .onFailure(SupervisorStrategy.restartWithBackoff(10.second, 1.minute, 0.1))

  private def framing: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(ByteString("\n"), maximumFrameLength = 65536, allowTruncation = true)


  private def loadAPOR(s3Attributes: Attributes, bucket: String, bucketKey: String, rateType: RateType)(
    implicit mat: Materializer
  ): Future[Done] = {

    val s3FixedSource: Source[Option[(Source[ByteString, NotUsed], ObjectMetadata)], NotUsed] =
      S3.download(bucket, bucketKey)
        .withAttributes(s3Attributes)

    s3FixedSource
      .flatMapConcat(src =>{
        checkDownload(src,rateType)
        src.get._1
      }
      )
      .via(framing)
      .drop(1)
      .map(s => s.utf8String)
      .map(s => APORCsvParser(s))
      .map(apor => AporListEntity.AporOperation(apor, rateType))
      .runWith(Sink.ignore)
  }

  private def checkDownload(src: Option[(Source[ByteString, NotUsed], ObjectMetadata)],rateType: RateType){
    if(src ==None){
      logger.error(s"${APORScheduler.name} had an error downloading APOR file for: "+ rateType)
    }
  }
}