package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.NotUsed
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.{MultipartUploadResult, S3Client}
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.RegulatorComponent
import hmda.query.ts.TransmittalSheetEntity
import hmda.regulator.scheduler.schedules.Schedules.TsScheduler

import scala.concurrent.Future
import scala.util.{Failure, Success}

class TsScheduler extends HmdaActor with RegulatorComponent {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def tsRepository = new TransmittalSheetRepository(dbConfig)

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("TsScheduler", self, TsScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("TsScheduler")
  }

  override def receive: Receive = {

    case TsScheduler =>
      val awsConfig = ConfigFactory.load("application.conf").getConfig("aws")
      val accessKeyId = awsConfig.getString("access-key-id")
      val secretAccess = awsConfig.getString("secret-access-key ")
      val region = awsConfig.getString("region")
      val bucket = awsConfig.getString("public-bucket")
      val environment = awsConfig.getString("environment")
      val year = awsConfig.getString("year")
      val bankFilter =
        ConfigFactory.load("application.conf").getConfig("filter")
      val bankFilterList =
        bankFilter.getString("bank-filter-list").toUpperCase.split(",")
      val awsCredentialsProvider = new AWSStaticCredentialsProvider(
        new BasicAWSCredentials(accessKeyId, secretAccess))

      val awsRegionProvider = new AwsRegionProvider {
        override def getRegion: String = region
      }

      val s3Settings = new S3Settings(
        MemoryBufferType,
        None,
        awsCredentialsProvider,
        awsRegionProvider,
        false,
        None,
        ListBucketVersion2
      )

      val s3Client = new S3Client(s3Settings)(context.system, materializer)

      val now = LocalDateTime.now().minusDays(1)

      val formattedDate = fullDate.format(now)

      val fileName = s"$formattedDate" + s"$year" + "_ts" + ".txt"
      val s3Sink =
        s3Client.multipartUpload(bucket, s"$environment/ts/$fileName")

      val allResults: Future[Seq[TransmittalSheetEntity]] =
        tsRepository.getAllSheets(bankFilterList)

      val results: Future[MultipartUploadResult] = Source
        .fromFuture(allResults)
        .map(seek => seek.toList)
        .mapConcat(identity)
        .map(transmittalSheet => transmittalSheet.toPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/ts/$fileName" + ".")
        }
        case Failure(t) =>
          println("An error has occurred getting TS Data: " + t.getMessage)
      }
  }
}
