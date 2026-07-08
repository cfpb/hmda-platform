package hmda.analytics

import akka.Done
import akka.actor.{ActorSystem, typed}
import akka.actor.typed.scaladsl.adapter._
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.{ByteString, Timeout}
import com.typesafe.config.ConfigFactory
import hmda.analytics.HmdaAnalyticsApp.generateInstitutionEntity
import hmda.analytics.query._
import hmda.messages.HmdaMessageFilter
import hmda.messages.institution.InstitutionCommands.{GetInstitution, InstitutionCommand, ModifyInstitution}
import hmda.messages.institution.InstitutionEvents.InstitutionEvent
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.model.institution.{Agency, Institution, Respondent}
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.persistence.institution.InstitutionPersistence
import hmda.persistence.institution.InstitutionPersistence.startShardRegion
import hmda.publication.KafkaUtils._
import hmda.query.DbConfiguration.dbConfig
import hmda.query.HmdaQuery.{readRawData, readSubmission}
import hmda.query.ts.{TransmittalSheetConverter, TransmittalSheetEntity}
import hmda.util.BankFilterUtils._
import hmda.util.streams.FlowUtils.framing
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.{ExecutionContextExecutor, Future}
import scala.concurrent.duration._

// $COVERAGE-OFF$
object HmdaAnalyticsApp extends App with TransmittalSheetComponent with LarComponent with SubmissionHistoryComponent with InstitutionTSComponent{

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

  implicit val system: ActorSystem = ActorSystem()
  implicit val typedSystem: typed.ActorSystem[Nothing] = system.toTyped
  implicit val materializer: Materializer = Materializer(system)
  implicit val ec: ExecutionContextExecutor = system.dispatcher

  implicit val timeout: Timeout = Timeout(5.seconds)

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val config      = ConfigFactory.load()
  val parallelism = config.getInt("hmda.analytics.parallelism")
  val larDeletion = config.getBoolean("hmda.analytics.larDeletion")
  val historyInsertion = config.getBoolean("hmda.analytics.historyInsertion")
  val tsDeletion = config.getBoolean("hmda.analytics.tsDeletion")
  val sharding = ClusterSharding(typedSystem)

  startShardRegion(sharding)


  /**
   * Note: hmda-analytics microservice reads the JDBC_URL env var from inst-postgres-credentials secret.
   * In beta namespace this environment variable has currentSchema=hmda_beta_user appended to it to change the schema
   * to BETA
   */

  private def getTableName(template: String, year: String, quarter: String): String = {
    val tsConfig = String.format(template, year, quarter)
    config.getString(tsConfig)
  }

  private def getLarRepo(msg: String, year: String, quarter: String): LarRepository = {
    try {
      val tsTable =  getTableName("hmda.analytics.%s.larTableName%s", year, quarter)
      new LarRepository(dbConfig, tsTable)
    } catch {
      case e: Exception =>
        val errMsg = s"$msg: ${e.getMessage}"
        log.error(errMsg)
        throw new IllegalArgumentException(errMsg)
    }
  }

  private def getInstitutionRepo(msg: String, year: String, quarter: String): InstitutionRepository = {
    try {
      val institutionTable =  getTableName("hmda.analytics.%s.institutionTableName%s", year, quarter)
      new InstitutionRepository(dbConfig, institutionTable)
    }  catch {
      case e: Exception =>
        val errMsg = s"$msg: ${e.getMessage}"
        log.error(errMsg)
        throw new IllegalArgumentException(errMsg)
    }
  }

  private def getTsRepo(msg: String, year: String, quarter: String): TransmittalSheetRepository = {
    try {
      val tsTable =  getTableName("hmda.analytics.%s.tsTableName%s", year, quarter)
      new TransmittalSheetRepository(dbConfig, tsTable)
    } catch {
      case e: Exception =>
        val errMsg = s"$msg: ${e.getMessage}"
        log.error(errMsg)
        throw new IllegalArgumentException(errMsg)
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
      .withProperties(getKafkaConfig)

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

    val rawData = readRawData(submissionId).map(l => l.data)
    val rawDataSource =  rawData.map(ByteString(_)).via(framing("\n")).map(_.utf8String.trim)

    def signDate: Future[Option[Long]] =
      readSubmission(submissionId)
        .map(l => l.submission.end)
        .runWith(Sink.lastOption)

    def firstSignDateSubmissionHistory: Future[Seq[Long]] = {
      submissionHistoryRepository.firstSignDate(submissionId)
    }

    def deleteTsRow: Future[Done] =
      rawData.take(1)
        .map(s => TsCsvParser(s, fromCassandra = true))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "" && tsDeletion)
        .map(ts => TransmittalSheetConverter(ts, submissionIdOption))
        .mapAsync(1) { ts =>
          val tsRepo = getTsRepo(
            s"Unable to discern period from $submissionId to delete TS rows",
            submissionId.period.year.toString,
            submissionId.period.quarter.getOrElse("")
          )
          if (submissionId.period.quarter.isDefined) {
            tsRepo.deleteByLeiAndQuarter(lei = ts.lei)
          } else {
            tsRepo.deleteByLei(lei = ts.lei)
          }
        }
        .runWith(Sink.ignore)

    def insertSubmissionHistory: Future[Done] =
      rawDataSource.take(1)
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
      rawDataSource.take(1)
        .map(s => TsCsvParser(s, fromCassandra = true))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "")
        .map(ts => TransmittalSheetConverter(ts, submissionIdOption))
        .mapAsync(1) { ts =>
          val tsRepo = getTsRepo(
            s"Unable to discern period from $submissionId to insert TS rows",
            submissionId.period.year.toString,
            submissionId.period.quarter.getOrElse("")
          )
          val institutionRepo = getInstitutionRepo(
            s"Unable to discern period from $submissionId to update panel rows",
            submissionId.period.year.toString,
            submissionId.period.quarter.getOrElse("")
          )
          val institutionEntity=generateInstitutionEntity(ts)
          val institution= Institution.empty
          val institutionPersistence = InstitutionPersistence.selectInstitution(sharding, institutionEntity.lei, institutionEntity.activityYear)

          val enforceQuarterly = submissionId.period.quarter.isDefined
          if (ts.year>2023) {institutionRepo.updateByLei(generateInstitutionEntity(ts))}
          for {
             originalInstituion <- institutionPersistence ? GetInstitution
            _ <- modifyInstitutionViaCassandra(institution, originalInstituion,ts,institutionPersistence)
            signdate <- signDate
            firstsigndate <- firstSignDateSubmissionHistory
            insertorupdate <- {
              val resolvedSignDate = Some(signdate.getOrElse(0L))
              val resolvedFirstSignDate = {
                if (firstsigndate.head == 0) resolvedSignDate
                else Some(firstsigndate.head)
              }
              tsRepo.insert(copyTs(ts, resolvedSignDate, resolvedFirstSignDate, enforceQuarterly))
            }
          } yield insertorupdate
        }
        .runWith(Sink.ignore)

    def copyTs(ts: TransmittalSheetEntity, signdate: Option[Long], firstsigndate: Option[Long], enforceQuarterly: Boolean): TransmittalSheetEntity =
      if (enforceQuarterly) {
        ts.copy(lei = ts.lei.toUpperCase, signDate = signdate, firstSignDate = firstsigndate, isQuarterly = Some(true))
      } else {
        val newts = ts.copy(lei = ts.lei.toUpperCase, firstSignDate = firstsigndate, signDate = signdate)
        println("new ts")
        println(newts)
        newts
      }

    def deleteLarRows: Future[Done] =
      rawDataSource.drop(1)
        .take(1)
        .map(s => LarCsvParser(s, true))
        .map(_.getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "" && larDeletion)
        .mapAsync(1) { lar =>
          val larRepo = getLarRepo(
            s"Unable to discern period from $submissionId to delete LAR rows",
            submissionId.period.year.toString,
            submissionId.period.quarter.getOrElse("")
          )
          if (submissionId.period.quarter.isDefined) {
            larRepo.deletebyLeiAndQuarter(lar.larIdentifier.LEI)
          } else {
            larRepo.deleteByLei(lar.larIdentifier.LEI)
          }
        }
        .runWith(Sink.ignore)

    def insertLarRows: Future[Done] =
      rawDataSource.drop(1)
        .map(s => LarCsvParser(s, true))
        .map(_.getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "")
        .mapAsync(1) { lar =>
          val year = submissionId.period.year
          val larRepo = getLarRepo(
            s"Unable to discern period from $submissionId to insert LAR rows",
            year.toString,
            submissionId.period.quarter.getOrElse("")
          )
          val isQuarterly = submissionId.period.quarter.isDefined

          if (year == 2018) {
            larRepo.insert(LarConverter2018(lar))
          } else {
            larRepo.insert(
              LarConverter(lar = lar, year, isQuarterly = isQuarterly)
            )
          }
        }
        .runWith(Sink.ignore)

    def result =
      for {

        firstSignDate <- firstSignDateSubmissionHistory
        _ = log.info(s"First date signed $firstSignDate")

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


        dateSigned <- signDate
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

  private def modifyInstitutionViaCassandra(
                          incomingInstitution: Institution,
                          originalInstOpt: Option[Institution],
                          transmittalSheetEntity: TransmittalSheetEntity,
                          institutionPersistence: EntityRef[InstitutionCommand]
                        ): Future[InstitutionEvent] = {
    val originalFilerFlag      = originalInstOpt.getOrElse(Institution.empty).hmdaFiler
    val originalHasFiledQ1Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ1
    val originalHasFiledQ2Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ2
    val originalHasFiledQ3Flag = originalInstOpt.getOrElse(Institution.empty).quarterlyFilerHasFiledQ3
    val institutionId_2017 =originalInstOpt.getOrElse(Institution.empty).institutionId_2017

    val iFilerFlagsSet = incomingInstitution.copy(
      LEI = transmittalSheetEntity.lei,
      activityYear = transmittalSheetEntity.year,
      agency =Agency.valueOf( transmittalSheetEntity.agency),
      taxId = Some(transmittalSheetEntity.taxId),
      respondent = Respondent(Some(transmittalSheetEntity.institutionName),Some(transmittalSheetEntity.state),Some(transmittalSheetEntity.city)),
      institutionId_2017=institutionId_2017,
      hmdaFiler = originalFilerFlag,
      quarterlyFilerHasFiledQ1 = originalHasFiledQ1Flag,
      quarterlyFilerHasFiledQ2 = originalHasFiledQ2Flag,
      quarterlyFilerHasFiledQ3 = originalHasFiledQ3Flag
    )
    institutionPersistence ? (ref => ModifyInstitution(iFilerFlagsSet, ref))
  }

  private def generateInstitutionEntity(ts: TransmittalSheetEntity) = {

      InstitutionTSEntity(
        lei = ts.lei,
        activityYear = ts.year,
        agency = ts.agency,
        taxId = ts.taxId,
        respondentName = ts.institutionName,
        respondentState = ts.state,
        respondentCity = ts.city,
    )
  }
}
// $COVERAGE-ON$
