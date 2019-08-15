package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3._
import akka.stream.scaladsl._
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.query.ts._
import hmda.regulator.helper.TSHeader
import hmda.regulator.query.component.RegulatorComponent2018
import hmda.regulator.scheduler.schedules.Schedules.TsPublicScheduler2018

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsPublicScheduler
    extends HmdaActor
    with RegulatorComponent2018
    with TSHeader {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def tsRepository2018 = new TransmittalSheetRepository2018(dbConfig)

  val awsConfig = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyId = awsConfig.getString("public-access-key-id")
  val secretAccess = awsConfig.getString("public-secret-access-key ")
  val region = awsConfig.getString("public-region")
  val bucket = awsConfig.getString("public-s3-bucket")
  val environment = awsConfig.getString("public-environment")
  val year = awsConfig.getString("public-year")
  val bankFilter =
    ConfigFactory.load("application.conf").getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")
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

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("TsPublicScheduler2018", self, TsPublicScheduler2018)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("TsPublicScheduler2018")

  }

  override def receive: Receive = {

    case TsPublicScheduler2018 =>
      println("test timer public ts")

      val now = LocalDateTime.now().minusDays(1)

      val fileNamePSV = "2018_ts.txt"

      val s3SinkPSV =
        S3.multipartUpload(bucket, s"$environment/ts/$fileNamePSV")
          .withAttributes(S3Attributes.settings(s3Settings))

      val allResults: Future[Seq[TransmittalSheetEntity]] =
        tsRepository2018.getAllSheets(bankFilterList)

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
            "Pushing to S3: " + s"$bucket/$environment/ts/$fileNamePSV" + ".")
        }
        case Failure(t) =>
          println(
            "An error has occurred gettingPublic PSV TS Data 2018: " + t.getMessage)
      }
  }
}
