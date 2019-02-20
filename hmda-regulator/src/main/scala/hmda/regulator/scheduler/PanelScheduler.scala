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
import hmda.regulator.query.panel.{
  InstitutionAltEntity,
  InstitutionEmailEntity,
  InstitutionEntity
}
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

      val fileName = s"$formattedDate" + s"$year" + "_panel" + ".txt"
      val s3Sink =
        s3Client.multipartUpload(bucket, s"$environment/panel/$fileName")

      val allResults: Future[Seq[InstitutionEntity]] =
        institutionRepository.findActiveFilers(bankFilterList)

      val results: Future[MultipartUploadResult] = Source
        .fromFuture(allResults)
        .map(seek => seek.toList)
        .mapConcat(identity)
        .mapAsync(1)(institution => appendEmailDomains(institution))
        .map(institution => institution.toPSV + "\n")
        .map(s => ByteString(s))
        .runWith(s3Sink)

      results onComplete {
        case Success(result) => {
          log.info(
            "Pushing to S3: " + s"$bucket/$environment/panel/$fileName" + ".")
        }
        case Failure(t) =>
          println("An error has occurred getting Panel Data: " + t.getMessage)
      }

  }

  def appendEmailDomains(
      institution: InstitutionEntity): Future[InstitutionAltEntity] = {

    val emails: Future[Seq[InstitutionEmailEntity]] =
      emailRepository.findByLei(institution.lei)

    emails.map(
      emailList =>
        InstitutionAltEntity(
          lei = institution.lei,
          activityYear = institution.activityYear,
          agency = institution.agency,
          institutionType = institution.institutionType,
          id2017 = institution.id2017,
          taxId = institution.taxId,
          rssd = institution.rssd,
          respondentName = institution.respondentName,
          respondentState = institution.respondentState,
          respondentCity = institution.respondentCity,
          parentIdRssd = institution.parentIdRssd,
          parentName = institution.parentName,
          assets = institution.assets,
          otherLenderCode = institution.otherLenderCode,
          topHolderIdRssd = institution.topHolderIdRssd,
          topHolderName = institution.topHolderName,
          hmdaFiler = institution.hmdaFiler,
          emailDomains = emailList.map(email => email.emailDomain).mkString(",")
      ))
  }
}
