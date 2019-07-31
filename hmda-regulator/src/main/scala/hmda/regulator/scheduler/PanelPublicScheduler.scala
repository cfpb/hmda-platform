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
import hmda.regulator.query.component.RegulatorComponent2018
import hmda.regulator.query.panel.{
  InstitutionAltEntity,
  InstitutionEmailEntity,
  InstitutionEntity
}
import hmda.regulator.scheduler.schedules.Schedules.PanelPublicScheduler2018

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PanelPublicScheduler extends HmdaActor with RegulatorComponent2018 {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def institutionRepository2018 = new InstitutionRepository2018(dbConfig)
  def emailRepository2018 = new InstitutionEmailsRepository2018(dbConfig)

  val bankFilter =
    ConfigFactory.load("application.conf").getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")

  val awsConfig = ConfigFactory.load("application.conf").getConfig("public-aws")
  val accessKeyId = awsConfig.getString("public-access-key-id")
  val secretAccess = awsConfig.getString("public-secret-access-key ")
  val region = awsConfig.getString("public-region")
  val bucket = awsConfig.getString("public-s3-bucket")
  val environment = awsConfig.getString("public-environment")
  val year = awsConfig.getString("year")

  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val s3Settings =
    S3Settings(
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
      .schedule("PanelPublicScheduler2018", self, PanelPublicScheduler2018)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system)
      .cancelJob("PanelPublicScheduler2018")
  }

  override def receive: Receive = {
    case PanelPublicScheduler2018 =>
      panelPublicSync2018

  }

  private def panelPublicSync2018 = {
    println("test timer public panel")

    val allResults: Future[Seq[InstitutionEntity]] =
      institutionRepository2018.findActiveFilers(bankFilterList)
    val now = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileNameCSV = s"$formattedDate" + "2018_public_panel.csv"
    val fileNamePSV = s"$formattedDate" + "2018_public_panel.txt"

    //PSV Sync
    val s3SinkPSV =
      S3.multipartUpload(bucket, s"$environment/panel/$fileNamePSV")
        .withAttributes(S3Attributes.settings(s3Settings))

    val resultsPSV: Future[MultipartUploadResult] = Source
      .fromFuture(allResults)
      .map(seek => seek.toList)
      .mapConcat(identity)
      .mapAsync(1)(institution => appendEmailDomains2018(institution))
      .map(institution => institution.toPublicPSV + "\n")
      .map(s => ByteString(s))
      .runWith(s3SinkPSV)

    resultsPSV onComplete {
      case Success(result) => {
        log.info(
          "Pushing to S3: " + s"$bucket/$environment/panel/$fileNamePSV" + ".")
      }
      case Failure(t) =>
        println(
          "An error has occurred getting Panel Public Data 2018: " + t.getMessage)
    }

    //CSV Sync
    val s3SinkCSV =
      S3.multipartUpload(bucket, s"$environment/panel/$fileNameCSV")
        .withAttributes(S3Attributes.settings(s3Settings))

    val resultsCSV: Future[MultipartUploadResult] = Source
      .fromFuture(allResults)
      .map(seek => seek.toList)
      .mapConcat(identity)
      .mapAsync(1)(institution => appendEmailDomains2018(institution))
      .map(institution => institution.toPublicCSV + "\n")
      .map(s => ByteString(s))
      .runWith(s3SinkCSV)

    resultsCSV onComplete {
      case Success(result) => {
        log.info(
          "Pushing to S3: " + s"$bucket/$environment/panel/$fileNameCSV" + ".")
      }
      case Failure(t) =>
        println(
          "An error has occurred getting Panel Public Data 2018: " + t.getMessage)
    }
  }

  def appendEmailDomains2018(
      institution: InstitutionEntity): Future[InstitutionAltEntity] = {

    val emails: Future[Seq[InstitutionEmailEntity]] =
      emailRepository2018.findByLei(institution.lei)

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
