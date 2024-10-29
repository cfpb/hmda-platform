package hmda.analytics

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import hmda.analytics.query._
import hmda.messages.HmdaMessageFilter
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.publication.KafkaUtils.kafkaHosts
import hmda.query.DbConfiguration.dbConfig
import hmda.query.HmdaQuery.{readRawData, readSubmission}
import hmda.query.ts.{TransmittalSheetConverter, TransmittalSheetEntity}
import hmda.util.BankFilterUtils._
import hmda.util.streams.FlowUtils.framing
import hmda.utils.YearUtils.Period
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future
import scala.concurrent.duration._

// $COVERAGE-OFF$
object HmdaAnalyticsApp extends App with TransmittalSheetComponent with LarComponent with SubmissionHistoryComponent {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
             | _    _ __  __ _____                                 _       _   _
             || |  | |  \/  |  __ \   /\         /\               | |     | | (_)
             || |__| | \  / | |  | | /  \       /  \   _ __   __ _| |_   _| |_ _  ___ ___
             ||  __  | |\/| | |  | |/ /\ \     / /\ \ | '_ \ / _` | | | | | __| |/ __/ __|
             || |  | | |  | | |__| / ____ \   / ____ \| | | | (_| | | |_| | |_| | (__\__ \
             ||_|  |_|_|  |_|_____/_/    \_\ /_/    \_\_| |_|\__,_|_|\__, |\__|_|\___|jmo/
             |                                                        __/ |
             |                                                       |___/
    """.stripMargin)

  implicit val system       = ActorSystem()
  implicit val typedSystem  = system.toTyped
  implicit val materializer = Materializer(system)
  implicit val ec           = system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val config      = ConfigFactory.load()
  val parallelism = config.getInt("hmda.analytics.parallelism")
  val larDeletion = config.getBoolean("hmda.analytics.larDeletion")
  val historyInsertion = config.getBoolean("hmda.analytics.historyInsertion")
  val tsDeletion = config.getBoolean("hmda.analytics.tsDeletion")
  /**
   * Note: hmda-analytics microservice reads the JDBC_URL env var from inst-postgres-credentials secret.
   * In beta namespace this environment variable has currentSchema=hmda_beta_user appended to it to change the schema
   * to BETA
   */

  case class YearlyTransmittalSheetRepositoryWrapper(year: String){
    val transmittalSheet = new TransmittalSheetRepository(dbConfig, getTableNameByYear(year))

    def getTransmittalSheet = transmittalSheet

    def getId(row: TransmittalSheetTable): Unit = transmittalSheet.getId(row)
    def createSchema(): Unit = transmittalSheet.createSchema()
    def dropSchema(): Unit = transmittalSheet.dropSchema()

    def insert(ts: TransmittalSheetEntity): Future[Int] = transmittalSheet.insert(ts)

    def findByLei(lei: String): Future[Seq[TransmittalSheetEntity]] = transmittalSheet.findByLei(lei)

    def deleteByLei(lei: String): Future[Int] = transmittalSheet.deleteByLei(lei)

    def deleteByLeiAndQuarter(lei: String): Future[Int] = transmittalSheet.deleteByLeiAndQuarter(lei)

    def updateByLei(ts: TransmittalSheetEntity): Future[Int] = transmittalSheet.updateByLei(ts)

    def count(): Future[Int] = transmittalSheet.count()

    private def getTableNameByYear: String = {
      val configString = s"hmda.analytics.$year.tsTableName"
      config.getString(configString)
    }

  }

  case class QuarterlyTransmittalSheetRepositoryWrapper(year: String, quarter: String){
    val transmittalSheet = new TransmittalSheetRepository(dbConfig, getTableNameByYear(year))

    def getTransmittalSheet = transmittalSheet


    def getId(row: TransmittalSheetTable): Unit = transmittalSheet.getId(row)
    def createSchema(): Unit = transmittalSheet.createSchema()
    def dropSchema(): Unit = transmittalSheet.dropSchema()

    def insert(ts: TransmittalSheetEntity): Future[Int] = transmittalSheet.insert(ts)

    def findByLei(lei: String): Future[Seq[TransmittalSheetEntity]] = transmittalSheet.findByLei(lei)

    def deleteByLei(lei: String): Future[Int] = transmittalSheet.deleteByLei(lei)

    def deleteByLeiAndQuarter(lei: String): Future[Int] = transmittalSheet.deleteByLeiAndQuarter(lei)

    def updateByLei(ts: TransmittalSheetEntity): Future[Int] = transmittalSheet.updateByLei(ts)

    def count(): Future[Int] = transmittalSheet.count()

    private def getTableNameByYear: String = {
      val configString = s"hmda.analytics.$year.tsTableName$quarter"
      config.getString(configString)
    }

  }

  case class YearlyLarRepositoryWrapper(year: String){
    val larRepo = new LarRepository(dbConfig, getTableNameByYear(year))

    def getLarRepository = larRepo

    def insert(le: LarEntity): Future[Int] = larRepo.insert(le)

    private def getTableNameByYear: String = {
      val configString = s"hmda.analytics.$year.larTableName"
      config.getString(configString)
    }

  }

  case class QuarterlyLarRepositoryWrapper(year: String, quarter: String){
    val larRepo = new LarRepository(dbConfig, getTableNameByYear(year))

    def getLarRepository = larRepo

    // for 2019 and beyond
    def insert(le: LarEntity): Future[Int] = larRepo.insert(le)

    // for 2018
    def insert(le: LarEntity2018): Future[Int] = larRepo.insert(le)
    
    def deletebyLeiAndQuarter(lei: String): Future[Int] = larRepo.deletebyLeiAndQuarter(lei)

    def deleteByLei(lei: String): Future[Int] = larRepo.deleteByLei(lei)

    private def getTableNameByYear: String = {
      val configString = s"hmda.analytics.$year.larTableName$quarter"
      config.getString(configString)
    }

  }

  //submission_history table remains same regardless of the year. There is a sign_date column and submission_id column which would show which year the filing was for
  val histTableName    = config.getString("hmda.analytics.2018.historyTableName")



  val submissionHistoryRepository    = new SubmissionHistoryRepository(dbConfig, histTableName)

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig, new StringDeserializer, new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId(HmdaGroups.analyticsGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer
    .committableSource(consumerSettings, Subscriptions.topics(HmdaTopics.signTopic, HmdaTopics.analyticsTopic))
    .mapAsync(parallelism)(HmdaMessageFilter.processOnlyValidKeys { msg =>
      log.info(s"Processing: $msg")
      processData(msg.record.value()).map(_ => msg.committableOffset)
    })
    .toMat(Committer.sink(CommitterSettings(system).withParallelism(2)))(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()

  def processData(msg: String): Future[Done] =
    Source
      .single(msg)
      .map(msg => SubmissionId(msg))
      .filter(institution => filterBankWithLogging(institution.lei))
      .mapAsync(1) { id =>
        log.info(s"Adding data for $id")
        addTs(id)
      }
      .toMat(Sink.ignore)(Keep.right)
      .run()

  private def addTs(submissionId: SubmissionId): Future[Done] = {
    val submissionIdOption = Some(submissionId.toString)

    def signDate: Future[Option[Long]] =
      readSubmission(submissionId)
        .map(l => l.submission.end)
        .runWith(Sink.lastOption)

    def deleteTsRow: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .take(1)
        .map(s => TsCsvParser(s, fromCassandra = true))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "" && tsDeletion)
        .map(ts => TransmittalSheetConverter(ts, submissionIdOption))
        .mapAsync(1) { ts =>
          for {
            delete <- submissionId.period match {
              case Period(2018, None) => YearlyTransmittalSheetRepositoryWrapper("2018").deleteByLei(ts.lei)
              case Period(2019, None) => YearlyTransmittalSheetRepositoryWrapper("2019").deleteByLei(ts.lei)
              case Period(2020, Some("Q1")) => QuarterlyTransmittalSheetRepositoryWrapper("2020", "Q1").deleteByLei(ts.lei)
              case Period(2020, Some("Q2")) => QuarterlyTransmittalSheetRepositoryWrapper("2020", "Q2").deleteByLei(ts.lei)
              case Period(2020, Some("Q3")) => QuarterlyTransmittalSheetRepositoryWrapper("2020", "Q3").deleteByLei(ts.lei)
              case Period(2020, None) => YearlyTransmittalSheetRepositoryWrapper("2020").deleteByLei(ts.lei)
              case Period(2021, None) => YearlyTransmittalSheetRepositoryWrapper("2021").deleteByLei(ts.lei)
              case Period(2022, None) => YearlyTransmittalSheetRepositoryWrapper("2022").deleteByLei(ts.lei)
              case Period(2023, None) => YearlyTransmittalSheetRepositoryWrapper("2023").deleteByLei(ts.lei)
              case Period(2021, Some("Q1")) => QuarterlyTransmittalSheetRepositoryWrapper("2021", "Q1").deleteByLei(ts.lei)
              case Period(2021, Some("Q2")) => QuarterlyTransmittalSheetRepositoryWrapper("2021", "Q2").deleteByLei(ts.lei)
              case Period(2021, Some("Q3")) => QuarterlyTransmittalSheetRepositoryWrapper("2021", "Q3").deleteByLei(ts.lei)
              case Period(2022, Some("Q1")) => QuarterlyTransmittalSheetRepositoryWrapper("2022", "Q1").deleteByLei(ts.lei)
              case Period(2022, Some("Q2")) => QuarterlyTransmittalSheetRepositoryWrapper("2022", "Q2").deleteByLei(ts.lei)
              case Period(2022, Some("Q3")) => QuarterlyTransmittalSheetRepositoryWrapper("2022", "Q3").deleteByLei(ts.lei)
              case Period(2023, Some("Q1")) => QuarterlyTransmittalSheetRepositoryWrapper("2023", "Q1").deleteByLei(ts.lei)
              case Period(2023, Some("Q2")) => QuarterlyTransmittalSheetRepositoryWrapper("2023", "Q2").deleteByLei(ts.lei)
              case Period(2023, Some("Q3")) => QuarterlyTransmittalSheetRepositoryWrapper("2023", "Q3").deleteByLei(ts.lei)
              case Period(2024, Some("Q1")) => QuarterlyTransmittalSheetRepositoryWrapper("2024", "Q1").deleteByLei(ts.lei)
              case Period(2024, Some("Q2")) => QuarterlyTransmittalSheetRepositoryWrapper("2024", "Q2").deleteByLei(ts.lei)
              case Period(2024, Some("Q3")) => QuarterlyTransmittalSheetRepositoryWrapper("2024", "Q3").deleteByLei(ts.lei)
              case _ => {
                log.error(s"Unable to discern period from $submissionId to delete TS rows.")
                throw new IllegalArgumentException(s"Unable to discern period from $submissionId to delete TS rows.")
              }
            }
          } yield delete
        }
        .runWith(Sink.ignore)

    def insertSubmissionHistory: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .map(ByteString(_))
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .take(1)
        .map(s => TsCsvParser(s, fromCassandra = true))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "" && historyInsertion)
        .map(ts => TransmittalSheetConverter(ts, submissionIdOption))
        .mapAsync(1) { ts =>
          for {
            signdate <- signDate
            submissionHistory <- submissionHistoryRepository.insert(ts.lei, submissionId, signdate)
          } yield submissionHistory
        }
        .runWith(Sink.ignore)

    def insertTsRow: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .map(ByteString(_))
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .take(1)
        .map(s => TsCsvParser(s, fromCassandra = true))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "")
        .map(ts => TransmittalSheetConverter(ts, submissionIdOption))
        .mapAsync(1) { ts =>
          val (repo, enforceQuarterly) = submissionId.period match {
            case Period(2018, None) => (YearlyTransmittalSheetRepositoryWrapper("2018").getTransmittalSheet, false)
            case Period(2019, None) => (YearlyTransmittalSheetRepositoryWrapper("2019").getTransmittalSheet, false)
            case Period(2020, Some("Q1")) => (QuarterlyTransmittalSheetRepositoryWrapper("2020", "Q1").getTransmittalSheet, true)
            case Period(2020, Some("Q2")) => (QuarterlyTransmittalSheetRepositoryWrapper("2020", "Q2").getTransmittalSheet, true)
            case Period(2020, Some("Q3")) => (QuarterlyTransmittalSheetRepositoryWrapper("2020", "Q3").getTransmittalSheet, true)
            case Period(2020, None) => (YearlyTransmittalSheetRepositoryWrapper("2020").getTransmittalSheet, false)
            case Period(2021, None) => (YearlyTransmittalSheetRepositoryWrapper("2021").getTransmittalSheet, false)
            case Period(2022, None) => (YearlyTransmittalSheetRepositoryWrapper("2022").getTransmittalSheet, false)
            case Period(2023, None) => (YearlyTransmittalSheetRepositoryWrapper("2023").getTransmittalSheet, false)
            case Period(2021, Some("Q1")) => (QuarterlyTransmittalSheetRepositoryWrapper("2021", "Q1").getTransmittalSheet, true)
            case Period(2021, Some("Q2")) => (QuarterlyTransmittalSheetRepositoryWrapper("2021", "Q2").getTransmittalSheet, true)
            case Period(2021, Some("Q3")) => (QuarterlyTransmittalSheetRepositoryWrapper("2021", "Q3").getTransmittalSheet, true)
            case Period(2022, Some("Q1")) => (QuarterlyTransmittalSheetRepositoryWrapper("2022", "Q1").getTransmittalSheet, true)
            case Period(2022, Some("Q2")) => (QuarterlyTransmittalSheetRepositoryWrapper("2022", "Q2").getTransmittalSheet, true)
            case Period(2022, Some("Q3")) => (QuarterlyTransmittalSheetRepositoryWrapper("2022", "Q3").getTransmittalSheet, true)
            case Period(2023, Some("Q1")) => (QuarterlyTransmittalSheetRepositoryWrapper("2023", "Q1").getTransmittalSheet, true)
            case Period(2023, Some("Q2")) => (QuarterlyTransmittalSheetRepositoryWrapper("2023", "Q2").getTransmittalSheet, true)
            case Period(2023, Some("Q3")) => (QuarterlyTransmittalSheetRepositoryWrapper("2023", "Q3").getTransmittalSheet, true)
            case Period(2024, Some("Q1")) => (QuarterlyTransmittalSheetRepositoryWrapper("2024", "Q1").getTransmittalSheet, true)
            case Period(2024, Some("Q2")) => (QuarterlyTransmittalSheetRepositoryWrapper("2024", "Q2").getTransmittalSheet, true)
            case Period(2024, Some("Q3")) => (QuarterlyTransmittalSheetRepositoryWrapper("2024", "Q3").getTransmittalSheet, true)
            case _ =>{
              log.error(s"Unable to discern period from $submissionId to insert TS rows.")
              throw new IllegalArgumentException(s"Unable to discern period from $submissionId to insert TS rows.")
            }
          }

          for {
            signdate <- signDate
            insertorupdate <- repo.insert(copyTs(ts, Some(signdate.getOrElse(0L)), enforceQuarterly))
          } yield insertorupdate
        }
        .runWith(Sink.ignore)

    def copyTs(ts: TransmittalSheetEntity, signdate: Option[Long], enforceQuarterly: Boolean): TransmittalSheetEntity =
      if (enforceQuarterly) {
        ts.copy(lei = ts.lei.toUpperCase, signDate = signdate, isQuarterly = Some(true))
      } else {
        ts.copy(lei = ts.lei.toUpperCase, signDate = signdate)
      }

    def deleteLarRows: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .map(ByteString(_))
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .drop(1)
        .take(1)
        .map(s => LarCsvParser(s, true))
        .map(_.getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "" && larDeletion)
        .mapAsync(1) { lar =>
          for {
            delete <- submissionId.period match {
              case Period(2018, None) => YearlyLarRepositoryWrapper("2018").getLarRepository.deleteByLei(lar.larIdentifier.LEI)
              case Period(2019, None) => YearlyLarRepositoryWrapper("2019").getLarRepository.deleteByLei(lar.larIdentifier.LEI)
              case Period(2020, Some("Q1")) =>QuarterlyLarRepositoryWrapper("2020", "Q1").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2020, Some("Q2")) => QuarterlyLarRepositoryWrapper("2020", "Q2").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2020, Some("Q3")) => QuarterlyLarRepositoryWrapper("2020", "Q3").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2020, None) => YearlyLarRepositoryWrapper("2020").getLarRepository.deleteByLei(lar.larIdentifier.LEI)
              case Period(2021, None) =>YearlyLarRepositoryWrapper("2021").getLarRepository.deleteByLei(lar.larIdentifier.LEI)
              case Period(2022, None) =>YearlyLarRepositoryWrapper("2022").getLarRepository.deleteByLei(lar.larIdentifier.LEI)
              case Period(2023, None) =>YearlyLarRepositoryWrapper("2023").getLarRepository.deleteByLei(lar.larIdentifier.LEI)
              case Period(2021, Some("Q1")) => QuarterlyLarRepositoryWrapper("2021", "Q1").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2021, Some("Q2")) => QuarterlyLarRepositoryWrapper("2021", "Q2").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2021, Some("Q3")) => QuarterlyLarRepositoryWrapper("2021", "Q3").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2022, Some("Q1")) => QuarterlyLarRepositoryWrapper("2022", "Q1").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2022, Some("Q2")) => QuarterlyLarRepositoryWrapper("2022", "Q2").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2022, Some("Q3")) => QuarterlyLarRepositoryWrapper("2022", "Q3").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2023, Some("Q1")) => QuarterlyLarRepositoryWrapper("2023", "Q1").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2023, Some("Q2")) => QuarterlyLarRepositoryWrapper("2023", "Q2").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2023, Some("Q3")) => QuarterlyLarRepositoryWrapper("2023", "Q3").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2024, Some("Q1")) => QuarterlyLarRepositoryWrapper("2024", "Q1").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2024, Some("Q2")) => QuarterlyLarRepositoryWrapper("2024", "Q2").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case Period(2024, Some("Q3")) => QuarterlyLarRepositoryWrapper("2024", "Q3").getLarRepository.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
              case _ => {
                log.error(s"Unable to discern period from $submissionId to delete LAR rows.")
                throw new IllegalArgumentException(s"Unable to discern period from $submissionId to delete LAR rows.")
              }

            }
          } yield delete
        }
        .runWith(Sink.ignore)

    def insertLarRows: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .map(ByteString(_))
        .via(framing("\n"))
        .map(_.utf8String)
        .map(_.trim)
        .drop(1)
        .map(s => LarCsvParser(s, true))
        .map(_.getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "")
        .mapAsync(1) { lar =>
          for {
            insertorupdate <- submissionId.period match {
              case Period(2018, None) => YearlyLarRepositoryWrapper("2018").getLarRepository.insert(LarConverter2018(lar))
              case Period(2019, None) =>
                YearlyLarRepositoryWrapper("2019").getLarRepository.insert(
                  LarConverter(lar, 2019)
                )
              case Period(2020, Some("Q1")) =>
               QuarterlyLarRepositoryWrapper("2020", "Q1").getLarRepository.insert(
                  LarConverter(lar = lar, 2020, isQuarterly = true)
                )
              case Period(2020, Some("Q2")) =>
                QuarterlyLarRepositoryWrapper("2020", "Q2").getLarRepository.insert(
                  LarConverter(lar = lar, 2020, isQuarterly = true)
                )
              case Period(2020, Some("Q3")) =>
                QuarterlyLarRepositoryWrapper("2020", "Q3").getLarRepository.insert(
                  LarConverter(lar = lar, 2020, isQuarterly = true)
                )
              case Period(2020, None) =>
                YearlyLarRepositoryWrapper("2020").getLarRepository.insert(
                  LarConverter(lar = lar, 2020)
                )
              case Period(2021, None) =>
               YearlyLarRepositoryWrapper("2021").getLarRepository.insert(
                  LarConverter(lar = lar, 2021)
                )
              case Period(2022, None) =>
               YearlyLarRepositoryWrapper("2022").getLarRepository.insert(
                  LarConverter(lar = lar, 2022)
                )
              case Period(2023, None) =>
               YearlyLarRepositoryWrapper("2023").getLarRepository.insert(
                  LarConverter(lar = lar, 2023)
                )
              case Period(2021, Some("Q1")) =>
                QuarterlyLarRepositoryWrapper("2021", "Q1").getLarRepository.insert(
                  LarConverter(lar = lar, 2021, isQuarterly = true)
                )
              case Period(2021, Some("Q2")) =>
                QuarterlyLarRepositoryWrapper("2021", "Q2").getLarRepository.insert(
                  LarConverter(lar = lar, 2021, isQuarterly = true)
                )
              case Period(2021, Some("Q3")) =>
                QuarterlyLarRepositoryWrapper("2021", "Q3").getLarRepository.insert(
                  LarConverter(lar = lar, 2021, isQuarterly = true)
                )
              case Period(2022, Some("Q1")) =>
                QuarterlyLarRepositoryWrapper("2022", "Q1").getLarRepository.insert(
                  LarConverter(lar = lar, 2022, isQuarterly = true)
                )
              case Period(2022, Some("Q2")) =>
                QuarterlyLarRepositoryWrapper("2022", "Q2").getLarRepository.insert(
                  LarConverter(lar = lar, 2022, isQuarterly = true)
                )
              case Period(2022, Some("Q3")) =>
                QuarterlyLarRepositoryWrapper("2022", "Q3").getLarRepository.insert(
                  LarConverter(lar = lar, 2022, isQuarterly = true)
                )
              case Period(2023, Some("Q1")) =>
                QuarterlyLarRepositoryWrapper("2023", "Q1").getLarRepository.insert(
                  LarConverter(lar = lar, 2023, isQuarterly = true)
                )
              case Period(2023, Some("Q2")) =>
                QuarterlyLarRepositoryWrapper("2023", "Q2").getLarRepository.insert(
                  LarConverter(lar = lar, 2023, isQuarterly = true)
                )
              case Period(2023, Some("Q3")) =>
                QuarterlyLarRepositoryWrapper("2023", "Q3").getLarRepository.insert(
                  LarConverter(lar = lar, 2023, isQuarterly = true)
                )
              case Period(2024, Some("Q1")) =>
                QuarterlyLarRepositoryWrapper("2024", "Q1").getLarRepository.insert(
                  LarConverter(lar = lar, 2024, isQuarterly = true)
                )
              case Period(2024, Some("Q2")) =>
                QuarterlyLarRepositoryWrapper("2024", "Q2").getLarRepository.insert(
                  LarConverter(lar = lar, 2024, isQuarterly = true)
                )
              case Period(2024, Some("Q3")) =>
                QuarterlyLarRepositoryWrapper("2024", "Q3").getLarRepository.insert(
                  LarConverter(lar = lar, 2024, isQuarterly = true)
                )
              case _ => {
                log.error(s"Unable to discern period from $submissionId to insert LAR rows.")
                throw new IllegalArgumentException(s"Unable to discern period from $submissionId to insert LAR rows.")
              }
            }
          } yield insertorupdate
        }
        .runWith(Sink.ignore)

    def result =
      for {

        _ <- deleteTsRow
        _ = if(tsDeletion)
          log.info(s"Attempt to remove data from TS table for  $submissionId  completed.")
        else
          log.info(s"Skipping Delete TS -- no deletion needed")

        _ <- deleteLarRows
        _ = if(larDeletion)
          log.info(s"Attempt to remove data from LAR table for  $submissionId  completed.")
        else
          log.info(s"Skipping Delete LAR -- no deletion needed")

        _ <- insertTsRow
        _ =  log.info(s"Attempt to add data to TS table for  $submissionId  completed.")


        _ <- insertLarRows
        _ = log.info(s"Attempt to add data to LAR table for  $submissionId  completed.")


        dateSigned   <- signDate
        _ = log.info(s"Date signed $dateSigned")

        res <- insertSubmissionHistory
        _ = if(historyInsertion)
          log.info(s"Attempt to add data to Submission History Table for  $submissionId  completed.")

        else
          log.info(s"Skipping Insert Submission History")

      } yield res



    result.recover {
      case t: Throwable =>
        log.error("Error happened in inserting: ", t)
        akka.Done.done()
    }

  }

}
// $COVERAGE-ON$
