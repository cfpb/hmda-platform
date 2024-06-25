package hmda.publisher.scheduler

import java.time.LocalDateTime
import java.time.Instant
import java.time.format.DateTimeFormatter
import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{MemoryBufferType, MultipartUploadResult, S3Attributes, S3Settings}
import akka.stream.scaladsl.Source
import akka.util.ByteString
import com.typesafe.config.ConfigFactory
import hmda.actor.HmdaActor
import hmda.publisher.helper.CronConfigLoader.{CronString, panelCron, panelYears, specificPanelCron, specificPanelYears}
import hmda.publisher.helper.{PrivateAWSConfigLoader, S3Utils, SnapshotCheck}
import hmda.publisher.query.component.{InstitutionEmailComponent, InstitutionRepository, PublisherComponent, PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022, PublisherComponent2023}
import hmda.publisher.query.panel.{InstitutionAltEntity, InstitutionEmailEntity, InstitutionEntity}
import hmda.publisher.scheduler.schedules.{Schedule, ScheduleWithYear}
import hmda.publisher.scheduler.schedules.Schedules.PanelSchedule
import hmda.publisher.util.{PublishingReporter, ScheduleCoordinator}
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._
import hmda.util.CSVConsolidator.listDeDupeToString

import scala.concurrent.duration.HOURS
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
// $COVERAGE-OFF$
class PanelScheduler(publishingReporter: ActorRef[PublishingReporter.Command], scheduler: ActorRef[ScheduleCoordinator.Command])
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with PublisherComponent2023
    with InstitutionEmailComponent
    with PrivateAWSConfigLoader {

  implicit val ec: ExecutionContext       = context.system.dispatcher
  implicit val materializer: Materializer = Materializer(context)
  private val fullDate                    = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  def institutionRepository2018           = new InstitutionRepository2018(dbConfig)
  def institutionRepository2019           = new InstitutionRepository2019(dbConfig)
  def institutionRepository2020           = new InstitutionRepository2020(dbConfig)
  def institutionRepository2021           = new InstitutionRepository2021(dbConfig)
  def institutionRepository2022           = new InstitutionRepository2022(dbConfig)

  val availableRepos = panelAvailableYears.map(year => year -> {
    val component = new PublisherComponent(year)
    new InstitutionRepository(dbConfig, component.institutionsTable)
  }).toMap


  def emailRepository                     = new InstitutionEmailsRepository2018(dbConfig)

  val awsConfig =
    ConfigFactory.load("application.conf").getConfig("private-aws")

  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPrivate)
    .withS3RegionProvider(awsRegionProviderPrivate)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart(): Unit = {
    panelYears.zipWithIndex.foreach {
      case (year, idx) => scheduler ! Schedule(s"PanelSchedule_$year", self, ScheduleWithYear(PanelSchedule, year), panelCron.applyOffset(idx, HOURS))
    }

    specificPanelYears.zipWithIndex.foreach {
      case (year, idx) => scheduler ! Schedule(s"PanelSchedule_$year", self, ScheduleWithYear(PanelSchedule, year), specificPanelCron.applyOffset(idx, HOURS))
    }
  }

  override def postStop(): Unit = {
    panelYears.foreach(year => scheduler ! Unschedule(s"PanelSchedule_$year"))
    specificPanelYears.foreach(year => scheduler ! Unschedule(s"PanelSchedule_$year"))
  }

  override def receive: Receive = {


    case ScheduleWithYear(schedule, year) if schedule == PanelSchedule =>
      panelSync(year)

  }

  private def panelSync(year: Int): Unit = {
    availableRepos.get(year) match {
      case Some(repo) =>
        val allResults = repo.findActiveFilers(getFilterList())
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = s"$formattedDate${year}_panel.txt"
        val s3Path = s"$environmentPrivate/panel/"
        val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)

        val s3Sink =
          S3.multipartUpload(bucketPrivate, fullFilePath)
            .withAttributes(S3Attributes.settings(s3Settings))
        val source = Source
          .future(allResults)
          .mapConcat(seek => seek.toList)
          .mapAsync(1)(institution => appendEmailDomains(institution))
          .map(institution => institution.toPSV + "\n")
          .map(s => ByteString(s))
        val results: Future[MultipartUploadResult] = S3Utils.uploadWithRetry(source, s3Sink)

        results.onComplete(reportPublishingComplete(_, PanelSchedule, fullFilePath))
      case None => log.error("No available panel publisher for year {}", year)
    }
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
        emailDomains = listDeDupeToString(emailList.map(email => email.emailDomain))
      )
    )
  }



  protected def reportPublishingComplete(result: Try[Any], schedule: Schedule, fullFilePath: String): Unit =
    result match {
      case Success(result) =>
        publishingReporter ! FilePublishingCompleted(
          schedule,
          fullFilePath,
          None,
          Instant.now,
          FilePublishingCompleted.Status.Success
        )
        log.info("Pushed to S3: " + s"$bucketPrivate/$fullFilePath" + ".")
      case Failure(t) =>
        publishingReporter ! FilePublishingCompleted(
          schedule,
          fullFilePath,
          None,
          Instant.now,
          FilePublishingCompleted.Status.Error(t.getMessage)
        )
        log.error(s"An error has occurred getting panel data for schedule ${schedule}: " + t.getMessage)
    }

}
// $COVERAGE-ON$