package hmda.regulator.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.impl.ListBucketVersion2
import akka.stream.alpakka.s3.javadsl.S3Client
import akka.stream.alpakka.s3.{MemoryBufferType, S3Settings}
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.query.DbConfiguration.dbConfig
import hmda.regulator.query.{InstitutionEntity, RegulatorComponent}
import hmda.regulator.scheduler.schedules.Schedules.PanelScheduler

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PanelScheduler extends HmdaActor with RegulatorComponent {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler", self, PanelScheduler)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler")
  }

  override def receive: Receive = {

    case PanelScheduler =>
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
      val fileName = s"${now.format(DateTimeFormatter.ISO_LOCAL_DATE)}" + "_PANEL_" + ".txt"

      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/regulator-panel/$year/$fileName")


      val institutionRepository = new InstitutionRepository(dbConfig)

      val allResults: Future[Seq[InstitutionEntity]] =
        institutionRepository.getAllInstitutions()

      allResults onComplete {
        case Success(institutions) => {
          val source = institutions
            .map(institution => institution.toPSV + "\n")
            .map(s => addHeader(s))

          log.info(s"Uploading Regulator Data file : $fileName" + "  to S3.")
          // ByteString(source).runWith(s3Sink)
          println(source)
        }
        case Failure(t) => println("An error has occurred: " + t.getMessage)
      }
  }
  def addHeader(panelData: String): String = {
    val fileHeader = "lei|activityYear|agency|institutionType|" +
      "id2017|taxId|rssd|respondentName|respondentState|respondentCity|" +
      "parentIdRssd|parentName|assets|otherLenderCode|topHolderIdRssd|topHolderName|hmdaFiler" + "\n"

    fileHeader + panelData
  }

}
