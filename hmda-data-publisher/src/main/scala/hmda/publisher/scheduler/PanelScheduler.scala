package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, MultipartUploadResult, S3Attributes, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.{PrivateAWSConfigLoader, SnapshotCheck}
import hmda.publisher.query.component.{InstitutionEmailComponent, PublisherComponent2018, PublisherComponent2019}
import hmda.publisher.query.panel.{InstitutionAltEntity, InstitutionEmailEntity, InstitutionEntity}
import hmda.publisher.scheduler.schedules.Schedules.{PanelScheduler2018, PanelScheduler2019}
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import software.amazon.awssdk.auth.credentials.{AwsBasicCredentials, StaticCredentialsProvider}
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.regions.providers.AwsRegionProvider

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success}

class PanelScheduler extends HmdaActor with PublisherComponent2018 with PublisherComponent2019 with InstitutionEmailComponent with PrivateAWSConfigLoader {

  implicit val ec: ExecutionContext       = context.system.dispatcher
  implicit val materializer: Materializer = Materializer(context)
  private val fullDate                    = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def institutionRepository2018           = new InstitutionRepository2018(dbConfig)
  def institutionRepository2019           = new InstitutionRepository2019(dbConfig)
  def emailRepository                     = new InstitutionEmailsRepository2018(dbConfig)

  val awsConfig =
    ConfigFactory.load("application.conf").getConfig("private-aws")


  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPrivate)
    .withS3RegionProvider(awsRegionProviderPrivate)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit =
    try {
      QuartzSchedulerExtension(context.system)
        .schedule("PanelScheduler2018", self, PanelScheduler2018)
      QuartzSchedulerExtension(context.system)
        .schedule("PanelScheduler2019", self, PanelScheduler2019)
    } catch { case e: Throwable => println(e) }

  override def postStop(): Unit = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler2019")

  }

  override def receive: Receive = {
    case PanelScheduler2018 =>
      panelSync2018()

    case PanelScheduler2019 =>
      panelSync2019()

  }

  private def panelSync2018(): Unit = {

    val allResults: Future[Seq[InstitutionEntity]] =
      institutionRepository2018.findActiveFilers(getFilterList())
    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = s"$formattedDate" + "2018_panel.txt"

    val s3Path = s"$environmentPrivate/panel/"
    val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

    val s3Sink =
      S3.multipartUpload(bucketPrivate, fullFilePath)
        .withAttributes(S3Attributes.settings(s3Settings))
    val results: Future[MultipartUploadResult] = Source
      .future(allResults)
      .mapConcat(seek => seek.toList)
      .mapAsync(1)(institution => appendEmailDomains2018(institution))
      .map(institution => institution.toPSV + "\n")
      .map(s => ByteString(s))
      .runWith(s3Sink)

    results onComplete {
      case Success(result) =>
        log.info("Pushed to S3: " + s"$bucketPrivate/$fullFilePath" +".")
      case Failure(t) =>
        log.error("An error has occurred getting Panel Data 2018: " + t.getMessage)
    }
  }

  private def panelSync2019(): Unit = {

    val allResults: Future[Seq[InstitutionEntity]] =
      institutionRepository2019.findActiveFilers(getFilterList())
    val now           = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName      = s"$formattedDate" + "2019_panel.txt"
    val s3Path = s"$environmentPrivate/panel/"
    val fullFilePath=  SnapshotCheck.pathSelector(s3Path,fileName)

    val s3Sink =
      S3.multipartUpload(bucketPrivate, fullFilePath)
        .withAttributes(S3Attributes.settings(s3Settings))
    val results: Future[MultipartUploadResult] = Source
      .future(allResults)
      .mapConcat(seek => seek.toList)
      .mapAsync(1)(institution => appendEmailDomains(institution))
      .map(institution => institution.toPSV + "\n")
      .map(s => ByteString(s))
      .runWith(s3Sink)

    results onComplete {
      case Success(result) =>
        log.info("Pushed to S3: " + s"$bucketPrivate/$fullFilePath" + ".")

      case Failure(t) =>
        log.error("An error has occurred getting Panel Data 2019: " + t.getMessage)
    }
  }

  def appendEmailDomains2018(institution: InstitutionEntity): Future[InstitutionAltEntity] = {

    val emails: Future[Seq[InstitutionEmailEntity]] =
      emailRepository.findByLei(institution.lei)

    emails.map(emailList =>
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
      )
    )
  }

  def appendEmailDomains(institution: InstitutionEntity): Future[InstitutionAltEntity] = {
    val emails: Future[Seq[InstitutionEmailEntity]] =
      emailRepository.findByLei(institution.lei)

    emails.map(emailList =>
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
      )
    )
  }
}