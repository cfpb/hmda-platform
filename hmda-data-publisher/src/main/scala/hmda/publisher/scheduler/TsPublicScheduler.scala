package hmda.publisher.scheduler

import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.util.BankFilterUtils._
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.TSHeader
import hmda.publisher.query.component.PublisherComponent2018
import hmda.publisher.scheduler.schedules.Schedules.TsPublicScheduler2018
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsPublicScheduler
    extends HmdaActor
    with PublisherComponent2018
    with TSHeader {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  def tsRepository2018 = new TransmittalSheetRepository2018(dbConfig)

  val awsConfig = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyId = awsConfig.getString("public-access-key-id")
  val secretAccess = awsConfig.getString("public-secret-access-key ")
  val region = awsConfig.getString("public-region")
  val bucket = awsConfig.getString("public-s3-bucket")
  val environment = awsConfig.getString("public-environment")
  val year = awsConfig.getString("public-year")
  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val s3Settings = S3Settings(
    MemoryBufferType,
    awsCredentialsProvider,
    awsRegionProvider,
    ListBucketVersion2
  )

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("TsPublicScheduler2018", self, TsPublicScheduler2018)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("TsPublicScheduler2018")

  }

  override def receive: Receive = {

    case TsPublicScheduler2018 =>
      log.info("starting job for TsPublicScheduler2018 ")

      val fileNamePSV = "2018_ts.txt"

      val s3SinkPSV =
        S3.multipartUpload(bucket,
                           s"$environment/dynamic-data/2018/$fileNamePSV")
          .withAttributes(S3Attributes.settings(s3Settings))

      val allResults: Future[Seq[TransmittalSheetEntity]] =
        tsRepository2018.getAllSheets(getFilterList())

      //SYNC PSV
      val resultsPSV: Future[MultipartUploadResult] = Source
        .fromFuture(allResults)
        .map(seek => seek.toList)
        .mapConcat(identity)
        .zipWithIndex
        .map(transmittalSheet =>
          if (transmittalSheet._2 == 0)
            TSHeader.concat(transmittalSheet._1.toPublicPSV) + "\n"
          else transmittalSheet._1.toPublicPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3SinkPSV)

      resultsPSV onComplete {
        case Success(result) => {
          log.info(
            "Pushed to S3: " + s"$bucket/$environment/dynamic-data/2018/$fileNamePSV" + ".")
        }
        case Failure(t) =>
          println(
            " An error has occurred getting Public PSV TS Data 2018: " + t.getMessage)
      }
  }
}
