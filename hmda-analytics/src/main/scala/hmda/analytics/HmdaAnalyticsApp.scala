package hmda.analytics

import akka.Done
import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import akka.util.Timeout
import com.typesafe.config.ConfigFactory
import hmda.analytics.query.{
  LarComponent,
  LarConverter,
  TransmittalSheetComponent,
  TransmittalSheetConverter
}
import hmda.messages.pubsub.HmdaTopics.{analyticsTopic, signTopic}
import hmda.model.filing.lar.LoanApplicationRegister
import hmda.model.filing.submission.SubmissionId
import hmda.model.filing.ts.TransmittalSheet
import hmda.parser.filing.lar.LarCsvParser
import hmda.parser.filing.ts.TsCsvParser
import hmda.publication.KafkaUtils.kafkaHosts
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import hmda.query.DbConfiguration.dbConfig
import hmda.query.HmdaQuery.readRawData

import scala.concurrent.Future
import scala.concurrent.duration._

object HmdaAnalyticsApp
    extends App
    with TransmittalSheetComponent
    with LarComponent {

  val log = LoggerFactory.getLogger("hmda")

  log.info(
    """
      | _    _ __  __ _____                                 _       _   _
      || |  | |  \/  |  __ \   /\         /\               | |     | | (_)
      || |__| | \  / | |  | | /  \       /  \   _ __   __ _| |_   _| |_ _  ___ ___
      ||  __  | |\/| | |  | |/ /\ \     / /\ \ | '_ \ / _` | | | | | __| |/ __/ __|
      || |  | | |  | | |__| / ____ \   / ____ \| | | | (_| | | |_| | |_| | (__\__ \
      ||_|  |_|_|  |_|_____/_/    \_\ /_/    \_\_| |_|\__,_|_|\__, |\__|_|\___|jmo/
      |                                                        __/ |
      |                                                       |___/
    """.stripMargin)

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val ec = system.dispatcher

  implicit val timeout = Timeout(5.seconds)

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val config = ConfigFactory.load()
  val bankFilter = config.getConfig("filter")
  val bankFilterList =
    bankFilter.getString("bank-filter-list").toUpperCase.split(",")
  val parallelism = config.getInt("hmda.analytics.parallelism")

  val transmittalSheetRepository = new TransmittalSheetRepository(dbConfig)
  val larRepository =
    new LarRepository(tableName = "loanapplicationregister2018", dbConfig)
  val db = transmittalSheetRepository.db

  val consumerSettings: ConsumerSettings[String, String] =
    ConsumerSettings(kafkaConfig,
                     new StringDeserializer,
                     new StringDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId("hmda-analytics")
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  Consumer
    .committableSource(consumerSettings,
                       Subscriptions.topics(signTopic, analyticsTopic))
    .mapAsync(parallelism) { msg =>
      processData(msg.record.value()).map(_ => msg.committableOffset)
    }
    .mapAsync(parallelism * 2)(offset => offset.commitScaladsl())
    .toMat(Sink.seq)(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()

  def processData(msg: String): Future[Done] = {
    Source
      .single(msg)
      .map(msg => SubmissionId(msg))
      .filter(id =>
        !bankFilterList.exists(bankLEI => bankLEI.equalsIgnoreCase(id.lei)))
      .mapAsync(1) { id =>
        log.info(s"Adding data for  $id")
        addTs(id)
      }
      .toMat(Sink.ignore)(Keep.right)
      .run()
  }
  private def addTs(submissionId: SubmissionId): Future[Done] = {

    def deleteTsRow: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .take(1)
        .map(s => TsCsvParser(s))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "")
        .map(ts => TransmittalSheetConverter(ts))
        .mapAsync(1) { ts =>
          for {

            delete <- transmittalSheetRepository.deleteByLei(ts.lei)

          } yield delete
        }
        .runWith(Sink.ignore)
    def insertTsRow: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .take(1)
        .map(s => TsCsvParser(s))
        .map(_.getOrElse(TransmittalSheet()))
        .filter(t => t.LEI != "" && t.institutionName != "")
        .map(ts => TransmittalSheetConverter(ts))
        .mapAsync(1) { ts =>
          for {

            insertorupdate <- transmittalSheetRepository.insert(ts)

          } yield insertorupdate
        }
        .runWith(Sink.ignore)

    def deleteLarRows: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .drop(1)
        .take(1)
        .map(s => LarCsvParser(s))
        .map(_.getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "")
        .map(lar => LarConverter(lar))
        .mapAsync(1) { lar =>
          larRepository.deleteByLei(lar.lei)
        }
        .runWith(Sink.ignore)

    def insertLarRows: Future[Done] =
      readRawData(submissionId)
        .map(l => l.data)
        .drop(1)
        .map(s => LarCsvParser(s))
        .map(_.getOrElse(LoanApplicationRegister()))
        .filter(lar => lar.larIdentifier.LEI != "")
        .map(lar => LarConverter(lar))
        .mapAsync(1) { lar =>
          larRepository.insert(lar)
        }
        .runWith(Sink.ignore)

    def result =
      for {
        _ <- deleteTsRow
        _ = log.info(s"Deleting data from TS for  $submissionId")
        _ <- insertTsRow
        _ = log.info(s"Adding data into TS for  $submissionId")
        _ <- deleteLarRows
        _ = log.info(s"Done deleting data from LAR for  $submissionId")
        res <- insertLarRows
        _ = log.info(s"Done inserting data into LAR for  $submissionId")
      } yield res
    result.recover {
      case t: Throwable =>
        log.error("Error happened in inserting: ", t)
        throw t
    }

  }

}
