package hmda.publisher.scheduler

import akka.NotUsed
import akka.actor.typed.ActorRef
import akka.stream.Materializer
import akka.stream.alpakka.s3.ApiVersion.ListBucketVersion2
import akka.stream.alpakka.s3.scaladsl.S3
import akka.stream.alpakka.s3.{ MemoryBufferType, MetaHeaders, S3Attributes, S3Settings }
import akka.stream.scaladsl.Source
import akka.util.ByteString
import hmda.actor.HmdaActor
import hmda.census.records.CensusRecords
import hmda.model.census.Census
import hmda.model.publication.Msa
import hmda.publisher.helper.CronConfigLoader.{ CronString, larCron, larQuarterlyCron, larQuarterlyYears, larYears, loanLimitCron, loanLimitYears }
import hmda.publisher.helper._
import hmda.publisher.query.component.{ PublisherComponent, PublisherComponent2018, PublisherComponent2019, PublisherComponent2020, PublisherComponent2021, PublisherComponent2022, PublisherComponent2023, YearPeriod }
import hmda.publisher.query.lar.{ LarEntityImpl, LarEntityImpl2019, LarEntityImpl2020, LarEntityImpl2021, LarEntityImpl2022 }
import hmda.publisher.scheduler.schedules.{ Schedule, ScheduleWithYear }
import hmda.publisher.scheduler.schedules.Schedules._
import hmda.publisher.util.{ PublishingReporter, ScheduleCoordinator }
import hmda.publisher.util.PublishingReporter.Command.FilePublishingCompleted
import hmda.publisher.util.ScheduleCoordinator.Command._
import hmda.publisher.validation.PublishingGuard
import hmda.publisher.validation.PublishingGuard.{ Period, Scope }
import hmda.query.DbConfiguration.dbConfig
import hmda.util.BankFilterUtils._

import java.time.format.DateTimeFormatter
import java.time.{ Clock, Instant, LocalDateTime }
import scala.concurrent.Future
import scala.concurrent.duration.HOURS
import scala.util.{ Failure, Success }
// $COVERAGE-OFF$
class LarScheduler(publishingReporter: ActorRef[PublishingReporter.Command], scheduler: ActorRef[ScheduleCoordinator.Command])
  extends HmdaActor
    with PublisherComponent2018
    with PublisherComponent2019
    with PublisherComponent2020
    with PublisherComponent2021
    with PublisherComponent2022
    with PublisherComponent2023
    with LoanLimitLarHeader
    with PrivateAWSConfigLoader {

  implicit val ec = context.system.dispatcher
  implicit val materializer = Materializer(context)
  private val fullDate = DateTimeFormatter.ofPattern("yyyy-MM-dd-")
  private val fullDateQuarterly = DateTimeFormatter.ofPattern("yyyy-MM-dd_")

  // Regulator File Scheduler Repos Annual
  def larRepository2018 = new LarRepository2018(dbConfig)

  def larRepository2019 = new LarRepository2019(dbConfig)

  def larRepository2020 = createLarRepository2020(dbConfig, Year2020Period.Whole)

  def larRepository2021 = createLarRepository2021(dbConfig, Year2021Period.Whole)

  def larRepository2022 = createLarRepository2022(dbConfig, Year2022Period.Whole)


  // Regulator File Scheduler Repos Quarterly
  def larRepository2020Q1 = createLarRepository2020(dbConfig, Year2020Period.Q1)

  def larRepository2020Q2 = createLarRepository2020(dbConfig, Year2020Period.Q2)

  def larRepository2020Q3 = createLarRepository2020(dbConfig, Year2020Period.Q3)

  def larRepository2021Q1 = createLarRepository2021(dbConfig, Year2021Period.Q1)

  def larRepository2021Q2 = createLarRepository2021(dbConfig, Year2021Period.Q2)

  def larRepository2021Q3 = createLarRepository2021(dbConfig, Year2021Period.Q3)

  def larRepository2022Q1 = createLarRepository2022(dbConfig, Year2022Period.Q1)

  def larRepository2022Q2 = createLarRepository2022(dbConfig, Year2022Period.Q2)

  def larRepository2022Q3 = createLarRepository2022(dbConfig, Year2022Period.Q3)

  def larRepository2023Q1 = createLarRepository2023(dbConfig, Year2023Period.Q1)

  def larRepository2023Q2 = createLarRepository2023(dbConfig, Year2023Period.Q2)

  def larRepository2023Q3 = createLarRepository2023(dbConfig, Year2023Period.Q3)


  val publishingGuard: PublishingGuard = PublishingGuard.create(this)(context.system)
  val timeBarrier: QuarterTimeBarrier = new QuarterTimeBarrier(Clock.systemDefaultZone())

  val indexTractMap2018: Map[String, Census] = CensusRecords.indexedTract2018
  val indexTractMap2019: Map[String, Census] = CensusRecords.indexedTract2019
  val indexTractMap2020: Map[String, Census] = CensusRecords.indexedTract2020
  val indexTractMap2021: Map[String, Census] = CensusRecords.indexedTract2021
  val indexTractMap2022: Map[String, Census] = CensusRecords.indexedTract2022

  val annualRepos = larAvailableYears.map(yr => yr -> {
    val component = new PublisherComponent(yr)
    component.createLarRepository(dbConfig, YearPeriod.Whole)
  }).toMap

  val quarterRepos = larQuarterAvailableYears.map(yr => yr -> {
    val component = new PublisherComponent(yr)
    (
      component.createLarRepository(dbConfig, YearPeriod.Q1),
      component.createLarRepository(dbConfig, YearPeriod.Q2),
      component.createLarRepository(dbConfig, YearPeriod.Q3)
    )
  }).toMap


  val s3Settings = S3Settings(context.system)
    .withBufferType(MemoryBufferType)
    .withCredentialsProvider(awsCredentialsProviderPrivate)
    .withS3RegionProvider(awsRegionProviderPrivate)
    .withListBucketApiVersion(ListBucketVersion2)

  override def preStart() = {
    larYears.zipWithIndex.foreach {
      case (year, idx) =>
        scheduler ! Schedule(s"LarSchedule_$year", self, ScheduleWithYear(LarSchedule, year), larCron.applyOffset(idx, HOURS))
    }

    loanLimitYears.zipWithIndex.foreach {
      case (year, idx) =>
        scheduler ! Schedule(s"LoanLimitSchedule_$year", self, ScheduleWithYear(LarLoanLimitSchedule, year), loanLimitCron.applyOffset(idx, HOURS))
    }

    larQuarterlyYears.zipWithIndex.foreach {
      case (year, idx) =>
        scheduler ! Schedule(s"LarQuarterlySchedule_$year", self, ScheduleWithYear(LarQuarterlySchedule, year), larQuarterlyCron.applyOffset(idx, HOURS))
    }
  }

  override def postStop() = {
    larYears.foreach(year => scheduler ! Unschedule(s"LarSchedule_$year"))
    loanLimitYears.foreach(year => scheduler ! Unschedule(s"LoanLimitSchedule_$year"))
    larQuarterlyYears.foreach(year => scheduler ! Unschedule(s"LarQuarterlySchedule_$year"))
  }

  override def receive: Receive = {

    case LarScheduler2018 =>
      publishingGuard.runIfDataIsValid(Period.y2018, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = s"$formattedDate" + "2018_lar.txt"

        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2018.getAllLARs(getFilterList()))
            .map(larEntity => larEntity.toRegulatorPSV)

        def countF: Future[Int] = larRepository2018.getAllLARsCount(getFilterList())

        for {
          s3ObjName <- publishPSVtoS3(fileName, allResultsSource, countF, LarScheduler2018)
          // _ <- persistFileForQa(s3ObjName, LarEntityImpl2018.parseFromPSVUnsafe, qaLarRepository2018)
        } yield ()
      }

    case LarScheduler2019 =>
      publishingGuard.runIfDataIsValid(Period.y2019, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = s"$formattedDate" + "2019_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2019.getAllLARs(getFilterList()))
            .map(larEntity => larEntity.toRegulatorPSV)

        def countF: Future[Int] = larRepository2019.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarScheduler2019)
        } yield ()
      }

    case LarScheduler2020 =>
      publishingGuard.runIfDataIsValid(Period.y2020, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = s"$formattedDate" + "2020_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2020.getAllLARs(getFilterList()))
            .map(larEntity => larEntity.toRegulatorPSV)

        def countF: Future[Int] = larRepository2020.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarScheduler2020)
        } yield ()
      }

    case LarScheduler2021 =>
      publishingGuard.runIfDataIsValid(Period.y2021, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = s"$formattedDate" + "2021_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2021.getAllLARs(getFilterList()))
            .map(larEntity => larEntity.toRegulatorPSV)

        def countF: Future[Int] = larRepository2021.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarScheduler2021)
        } yield ()
      }

    case LarScheduler2022 =>
      publishingGuard.runIfDataIsValid(Period.y2022, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = s"$formattedDate" + "2022_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2022.getAllLARs(getFilterList()))
            .map(larEntity => larEntity.toRegulatorPSV)

        def countF: Future[Int] = larRepository2022.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarScheduler2022)
        } yield ()
      }
    case LarSchedulerLoanLimit2019 =>
      publishingGuard.runIfDataIsValid(Period.y2019, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = "2019F_AGY_LAR_withFlag_" + s"$formattedDate" + "2019_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2019.getAllLARs(getFilterList()))
            .map(larEntity => appendCensus2019(larEntity, 2019))
            .prepend(Source(List(LoanLimitHeader)))

        def countF: Future[Int] = larRepository2019.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerLoanLimit2019)
        } yield ()
      }

    case LarSchedulerLoanLimit2020 =>
      publishingGuard.runIfDataIsValid(Period.y2020, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = "2020F_AGY_LAR_withFlag_" + s"$formattedDate" + "2020_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2020.getAllLARs(getFilterList()))
            .map(larEntity => appendCensus2020(larEntity, 2020))
            .prepend(Source(List(LoanLimitHeader)))

        def countF: Future[Int] = larRepository2020.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerLoanLimit2020)
        } yield ()
      }
    case LarSchedulerLoanLimit2021 =>
      publishingGuard.runIfDataIsValid(Period.y2021, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = "2021F_AGY_LAR_withFlag_" + s"$formattedDate" + "2021_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2021.getAllLARs(getFilterList()))
            .map(larEntity => appendCensus2021(larEntity, 2021))
            .prepend(Source(List(LoanLimitHeader)))

        def countF: Future[Int] = larRepository2021.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerLoanLimit2021)
        } yield ()
      }

    case LarSchedulerLoanLimit2022 =>
      publishingGuard.runIfDataIsValid(Period.y2022, Scope.Private) {
        val now = LocalDateTime.now().minusDays(1)
        val formattedDate = fullDate.format(now)
        val fileName = "2022F_AGY_LAR_withFlag_" + s"$formattedDate" + "2022_lar.txt"
        val allResultsSource: Source[String, NotUsed] =
          Source
            .fromPublisher(larRepository2022.getAllLARs(getFilterList()))
            .map(larEntity => appendCensus2022(larEntity, 2022))
            .prepend(Source(List(LoanLimitHeader)))

        def countF: Future[Int] = larRepository2022.getAllLARsCount(getFilterList())

        for {
          _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerLoanLimit2022)
        } yield ()
      }
    case LarSchedulerQuarterly2020 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)

      def publishQuarter2020[Table <: RealLarTable2020](
                                                         quarter: Period.Quarter,
                                                         fileNameSuffix: String,
                                                         repo: LarRepository2020Base[Table]) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {
            val fileName = formattedDate + fileNameSuffix

            val allResultsSource: Source[String, NotUsed] = Source
              .fromPublisher(repo.getAllLARs(getFilterList()))
              .map(larEntity => larEntity.toRegulatorPSV)

            def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

            for {
              _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerQuarterly2020)
            } yield ()
          }
        }

      publishQuarter2020(Period.y2020Q1, "quarter_1_2020_lar.txt", larRepository2020Q1)
      publishQuarter2020(Period.y2020Q2, "quarter_2_2020_lar.txt", larRepository2020Q2)
      publishQuarter2020(Period.y2020Q3, "quarter_3_2020_lar.txt", larRepository2020Q3)

    case LarSchedulerQuarterly2021 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)

      def publishQuarter2021[Table <: RealLarTable2021](
                                                         quarter: Period.Quarter,
                                                         fileNameSuffix: String,
                                                         repo: LarRepository2021Base[Table]) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {

            val fileName = formattedDate + fileNameSuffix

            val allResultsSource: Source[String, NotUsed] = Source
              .fromPublisher(repo.getAllLARs(getFilterList()))
              .map(larEntity => larEntity.toRegulatorPSV)

            def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

            for {

              _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerQuarterly2021)
            } yield ()
          }
        }

      publishQuarter2021(Period.y2021Q1, "quarter_1_2021_lar.txt", larRepository2021Q1)
      publishQuarter2021(Period.y2021Q2, "quarter_2_2021_lar.txt", larRepository2021Q2)
      publishQuarter2021(Period.y2021Q3, "quarter_3_2021_lar.txt", larRepository2021Q3)

    case LarSchedulerQuarterly2022 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)

      def publishQuarter2022[Table <: RealLarTable2022](
                                                         quarter: Period.Quarter,
                                                         fileNameSuffix: String,
                                                         repo: LarRepository2022Base[Table]
                                                       ) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {

            val fileName = formattedDate + fileNameSuffix

            val allResultsSource: Source[String, NotUsed] = Source
              .fromPublisher(repo.getAllLARs(getFilterList()))
              .map(larEntity => larEntity.toRegulatorPSV)

            def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

            for {

              _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerQuarterly2022)
            } yield ()
          }
        }

      publishQuarter2022(Period.y2022Q1, "quarter_1_2022_lar.txt", larRepository2022Q1)
      publishQuarter2022(Period.y2022Q2, "quarter_2_2022_lar.txt", larRepository2022Q2)
      publishQuarter2022(Period.y2022Q3, "quarter_3_2022_lar.txt", larRepository2022Q3)

    case LarSchedulerQuarterly2023 =>
      val now = LocalDateTime.now().minusDays(1)
      val formattedDate = fullDateQuarterly.format(now)

      def publishQuarter2023[Table <: RealLarTable2023](
                                                         quarter: Period.Quarter,
                                                         fileNameSuffix: String,
                                                         repo: LarRepository2023Base[Table]
                                                       ) =
        timeBarrier.runIfStillRelevant(quarter) {
          publishingGuard.runIfDataIsValid(quarter, Scope.Private) {

            val fileName = formattedDate + fileNameSuffix

            val allResultsSource: Source[String, NotUsed] = Source
              .fromPublisher(repo.getAllLARs(getFilterList()))
              .map(larEntity => larEntity.toRegulatorPSV)

            def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

            for {

              _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedulerQuarterly2023)
            } yield ()
          }
        }

      publishQuarter2023(Period.y2023Q1, "quarter_1_2023_lar.txt", larRepository2023Q1)
      publishQuarter2023(Period.y2023Q2, "quarter_2_2023_lar.txt", larRepository2023Q2)
      publishQuarter2023(Period.y2023Q3, "quarter_3_2023_lar.txt", larRepository2023Q3)

    case ScheduleWithYear(schedule, year) if schedule in (LarSchedule, LarQuarterlySchedule, LarLoanLimitSchedule) =>
      schedule match {
        case LarSchedule =>
          publishingGuard.runIfDataIsValid(year, YearPeriod.Whole, Scope.Private) {
            val now = LocalDateTime.now().minusDays(1)
            val formattedDate = fullDate.format(now)
            val fileName = s"$formattedDate${year}_lar.txt"

            annualRepos.get(year) match {
              case Some(repo) =>
                val allResultsSource: Source[String, NotUsed] =
                  Source
                    .fromPublisher(repo.getAllLARs(getFilterList()))
                    .map(larEntity => larEntity.toRegulatorPSV)

                def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

                for {
                  _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarSchedule)
                } yield ()
              case None => log.error("No available publisher found for {} in year {}", LarSchedule, year)
            }
          }

        case LarQuarterlySchedule =>
          quarterRepos.get(year) match {
            case Some((q1Repo, q2Repo, q3Repo)) =>
              val now = LocalDateTime.now().minusDays(1)
              val formattedDate = fullDateQuarterly.format(now)
              Seq((YearPeriod.Q1, 1, q1Repo), (YearPeriod.Q2, 2, q2Repo), (YearPeriod.Q3, 3, q3Repo)).foreach {
                case (quarterPeriod, quarterNumber, repo) =>
                  timeBarrier.runIfStillRelevant(year, quarterPeriod) {
                    publishingGuard.runIfDataIsValid(year, quarterPeriod, Scope.Private) {
                      val fileName = s"${formattedDate}quarter_${quarterNumber}_${year}_lar.txt"

                      val allResultsSource: Source[String, NotUsed] = Source
                        .fromPublisher(repo.getAllLARs(getFilterList()))
                        .map(larEntity => larEntity.toRegulatorPSV)

                      def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

                      for {
                        _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarQuarterlySchedule)
                      } yield ()
                    }
                  }
              }
            case None => log.error("No available quarterly publisher found for {} in year {}", LarQuarterlySchedule, year)
          }

        case LarLoanLimitSchedule =>
          publishingGuard.runIfDataIsValid(year, YearPeriod.Whole, Scope.Private) {
            val now = LocalDateTime.now().minusDays(1)
            val formattedDate = fullDate.format(now)
            val fileName = s"${year}F_AGY_LAR_withFlag_$formattedDate${year}_lar.txt"

            annualRepos.get(year) match {
              case Some(repo) =>
                val allResultsSource: Source[String, NotUsed] =
                  Source
                    .fromPublisher(repo.getAllLARs(getFilterList()))
                    .map(larEntity => appendCensus(larEntity, year))
                    .prepend(Source(List(LoanLimitHeader)))

                def countF: Future[Int] = repo.getAllLARsCount(getFilterList())

                for {
                  _ <- publishPSVtoS3(fileName, allResultsSource, countF, LarLoanLimitSchedule)
                } yield ()
              case None => log.error("No available publisher found for {} in year {}", LarLoanLimitSchedule, year)
            }
          }
      }
  }


  // returns effective file name/s3 object key
  def publishPSVtoS3(fileName: String, rows: Source[String, NotUsed], countF: => Future[Int], schedule: Schedule): Future[String] = {
    val s3Path = s"$environmentPrivate/lar/"
    val fullFilePath = SnapshotCheck.pathSelector(s3Path, fileName)

    val bytesStream: Source[ByteString, NotUsed] =
      rows
        .map(_ + "\n")
        .map(s => ByteString(s))

    val results = for {
      count <- countF
      s3Sink = S3
        .multipartUpload(bucketPrivate, fullFilePath, metaHeaders = MetaHeaders(Map(LarScheduler.entriesCountMetaName -> count.toString)))
        .withAttributes(S3Attributes.settings(s3Settings))
      _ <- S3Utils.uploadWithRetry(bytesStream, s3Sink)
    } yield count

    def sendPublishingNotif(error: Option[String], count: Option[Int]): Unit = {
      val status = error match {
        case Some(value) => FilePublishingCompleted.Status.Error(value)
        case None => FilePublishingCompleted.Status.Success
      }
      publishingReporter ! FilePublishingCompleted(schedule, fullFilePath, count, Instant.now(), status)
    }

    results onComplete {
      case Success(count) =>
        sendPublishingNotif(None, Some(count))
        log.info(s"Pushed to S3: $bucketPrivate/$fullFilePath.")
      case Failure(t) =>
        sendPublishingNotif(Some(t.getMessage), None)
        log.info(s"An error has occurred pushing LAR Data to $bucketPrivate/$fullFilePath: ${t.getMessage}")
    }

    results.map(_ => fullFilePath)
  }

  def getCensus(hmdaGeoTract: String, year: Int): Msa = {

    val indexTractMap = year match {
      case 2018 => indexTractMap2018
      case 2019 => indexTractMap2019
      case 2020 => indexTractMap2020
      case 2021 => indexTractMap2021
      case 2022 => indexTractMap2022
      case _ => indexTractMap2021
    }
    val censusResult = indexTractMap.getOrElse(hmdaGeoTract, Census())
    val censusID =
      if (censusResult.msaMd == 0) "-----" else censusResult.msaMd.toString
    val censusName =
      if (censusResult.name.isEmpty) "MSA/MD NOT AVAILABLE" else censusResult.name
    Msa(censusID, censusName)
  }

  def appendCensus2019(lar: LarEntityImpl2019, year: Int): String = {
    val msa = getCensus(lar.larPartOne.tract, year)
    lar.appendMsa(msa).toRegulatorPSV
  }

  def appendCensus2020(lar: LarEntityImpl2020, year: Int): String = {
    val msa = getCensus(lar.larPartOne.tract, year)
    lar.appendMsa(msa).toRegulatorPSV
  }

  def appendCensus2021(lar: LarEntityImpl2021, year: Int): String = {
    val msa = getCensus(lar.larPartOne.tract, year)
    lar.appendMsa(msa).toRegulatorPSV
  }

  def appendCensus2022(lar: LarEntityImpl2022, year: Int): String = {
    val msa = getCensus(lar.larPartOne.tract, year)
    lar.appendMsa(msa).toRegulatorPSV
  }

  def appendCensus(lar: LarEntityImpl, year: Int): String = {
    val msa = getCensus(lar.larPartOne.tract, year)
    lar.appendMsa(msa).toRegulatorPSV
  }
}
  object LarScheduler {
    val entriesCountMetaName = "entries-count"
  }

// $COVERAGE-ON$