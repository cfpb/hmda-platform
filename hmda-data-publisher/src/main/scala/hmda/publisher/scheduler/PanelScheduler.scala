package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

import akka.stream.ActorMaterializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, MultipartUploadResult, S3Attributes, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.regions.AwsRegionProvider
import com.typesafe.akka.extension.quartz.QuartzSchedulerExtension
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.util.BankFilterUtils._
import hmda.query.DbConfiguration.dbConfig
import hmda.publisher.query.component.{InstitutionEmailComponent, PublisherComponent2018, PublisherComponent2019}
import hmda.publisher.query.panel.{InstitutionAltEntity, InstitutionEmailEntity, InstitutionEntity}
import hmda.publisher.scheduler.schedules.Schedules.{PanelScheduler2018, PanelScheduler2019}

import scala.concurrent.Future
import scala.util.{Failure, Success}

class PanelScheduler
    extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
      with InstitutionEmailComponent {

  implicit val ec = context.system.dispatcher
  implicit val materializer = ActorMaterializer()
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def institutionRepository2018 = new InstitutionRepository2018(dbConfig)
  def institutionRepository2019 = new InstitutionRepository2019(dbConfig)
  def emailRepository = new InstitutionEmailsRepository2018(dbConfig)

  val awsConfig =
    ConfigFactory.load("application.conf").getConfig("private-aws")
  val accessKeyId = awsConfig.getString("private-access-key-id")
  val secretAccess = awsConfig.getString("private-secret-access-key ")
  val region = awsConfig.getString("private-region")
  val bucket = awsConfig.getString("private-s3-bucket")
  val environment = awsConfig.getString("private-environment")
  val year = awsConfig.getString("private-year")

  val awsCredentialsProvider = new AWSStaticCredentialsProvider(
    new BasicAWSCredentials(accessKeyId, secretAccess))

  val awsRegionProvider = new AwsRegionProvider {
    override def getRegion: String = region
  }

  val s3Settings =
    S3Settings(
      MemoryBufferType,
      awsCredentialsProvider,
      awsRegionProvider,
      ListBucketVersion2
    )

  override def preStart() = {
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler2018", self, PanelScheduler2018)
    QuartzSchedulerExtension(context.system)
      .schedule("PanelScheduler2019", self, PanelScheduler2019)

  }

  override def postStop() = {
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler2018")
    QuartzSchedulerExtension(context.system).cancelJob("PanelScheduler2019")

  }

  override def receive: Receive = {
    case PanelScheduler2018 =>
      println("Panel test 2018")
      panelSync2018

    case PanelScheduler2019 =>
      println("Panel test 2019")
      panelSync2019

  }

  private def panelSync2018() = {

    val allResults: Future[Seq[InstitutionEntity]] =
      institutionRepository2018.findActiveFilers(getFilterList())
    val now = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName = s"$formattedDate" + "2018_panel.txt"
    val s3Sink =
      S3.multipartUpload(bucket, s"$environment/panel/$fileName")
        .withAttributes(S3Attributes.settings(s3Settings))
    val results: Future[MultipartUploadResult] = Source
      .fromFuture(allResults)
      .map(seek => seek.toList)
      .mapConcat(identity)
      .mapAsync(1)(institution => appendEmailDomains2018(institution))
      .map(institution => institution.toPSV + "\n")
      .map(s => ByteString(s))
      .runWith(s3Sink)

    results onComplete {
      case Success(result) => {
        log.info(
          "Pushed to S3: " + s"$bucket/$environment/panel/$fileName" + ".")
      }
      case Failure(t) =>
        println(
          "An error has occurred getting Panel Data 2018: " + t.getMessage)
    }
  }

  private def panelSync2019() = {

    val allResults: Future[Seq[InstitutionEntity]] =
      institutionRepository2019.findActiveFilers(getFilterList())
    val now = LocalDateTime.now().minusDays(1)
    val formattedDate = fullDate.format(now)
    val fileName = s"$formattedDate" + "2019_panel.txt"
    val s3Sink =
      S3.multipartUpload(bucket, s"$environment/panel/$fileName")
        .withAttributes(S3Attributes.settings(s3Settings))
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
          "Pushed to S3: " + s"$bucket/$environment/panel/$fileName" + ".")
      }
      case Failure(t) =>
        println(
          "An error has occurred getting Panel Data 2019: " + t.getMessage)
    }
  }

  def appendEmailDomains2018(
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
