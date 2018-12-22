package hmda.regulator.scheduler

import java.time.{LocalDate, LocalDateTime}
import java.time.format.DateTimeFormatter

import akka.http.scaladsl.model.{ContentType, HttpCharsets, MediaTypes}
import akka.stream.Supervision.Decider
import akka.stream.alpakka.s3.impl.{S3Headers, ServerSideEncryption}
import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.{
  InstitutionEntity,
  RegulatorComponent,
  TransmittalSheetEntity
}
import hmda.regulator.scheduler.schedules.Schedules.{
  PanelScheduler,
  TsScheduler
}

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
      val config = ConfigFactory.load("application.conf").getConfig("aws")
      val accessKeyId = config.getString("access-key-id")
      val secretAccess = config.getString("secret-access-key ")
      val region = config.getString("region")
      val bucket = config.getString("public-bucket")
      val environment = config.getString("environment")
      val year = config.getString("year")
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

      val s3Client = new S3Client(s3Settings, context.system, materializer)

      val now = LocalDateTime.now()

      val formattedDate = fullDate.format(now)

      val fileName = s"$formattedDate" + s"$year" + "_ts" + ".txt"
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/regulator-ts/$year/$fileName")

      val allResults: Future[Seq[TransmittalSheetEntity]] =
        tsRepository.getAllSheets()

      allResults onComplete {
        case Success(transmittalSheets) => {
          val source = transmittalSheets
            .map(transmittalSheet => transmittalSheet.toPSV + "\n")
            .map(s => ByteString(s))
            .toList

          log.info(s"Uploading TS Regulator Data file : $fileName" + "  to S3.")
          Source(source).runWith(s3Sink)
        }
        case Failure(t) =>
          println(
            "An error has occurred getting Transmittal Sheet Data: " + t.getMessage)
      }
  }
}
