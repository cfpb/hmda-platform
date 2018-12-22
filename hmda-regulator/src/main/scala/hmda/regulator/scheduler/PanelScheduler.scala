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
import hmda.regulator.query.{InstitutionEntity, RegulatorComponent}
import hmda.regulator.scheduler.schedules.Schedules.PanelScheduler

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PanelScheduler extends HmdaActor with RegulatorComponent {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def institutionRepository = new InstitutionRepository(dbConfig)
  def emailRepository = new InstitutionEmailsRepository(dbConfig)

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

      val formattedDate = fullDate.format(now)

      val fileName = s"$formattedDate" + s"$year" + "_panel" + ".txt"
      val s3Sink = s3Client.multipartUpload(
        bucket,
        s"$environment/regulator-panel/$year/$fileName")

      val allResults: Future[Seq[InstitutionEntity]] =
        institutionRepository.getAllInstitutions()

      allResults onComplete {
        case Success(institutions) => {
          val source = institutions
            .map(institution => appendEmailDomains(institution))
            .map(institution => institution.toPSV + "\n")
            .map(s => ByteString(s))
            .toList

          log.info(
            s"Uploading Panel Regulator Data file : $fileName" + "  to S3.")
          Source(source).runWith(s3Sink)
        }
        case Failure(t) =>
          println("An error has occurred getting Panel Data: " + t.getMessage)
      }
  }

  def appendEmailDomains(institution: InstitutionEntity): InstitutionEntity = {
    val emails = emailRepository.findByLei(institution.lei)

    emails onComplete {
      case Success(emails) => {

        val emailList = emails.map(email => email.emailDomain)

        institution.emailDomains = emailList.mkString(",")
        institution
      }
      case Failure(t) =>
        println("An error has occurred getting Email by LEI: " + t.getMessage)
    }
    institution
  }
}
