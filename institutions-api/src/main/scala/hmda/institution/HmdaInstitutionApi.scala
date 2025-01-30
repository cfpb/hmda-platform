package hmda.institution

import akka.Done
import akka.actor.ActorSystem
import akka.actor.typed.scaladsl.adapter._
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.kafka.scaladsl.{ Committer, Consumer }
import akka.kafka.{ CommitterSettings, ConsumerSettings, Subscriptions }
import akka.stream.Materializer
import akka.stream.scaladsl.{ Keep, Sink, Source }
import com.typesafe.config.ConfigFactory
import hmda.institution.api.http.HmdaInstitutionQueryApi
import hmda.institution.projection.{ InstitutionDBProjection, ProjectEvent }
import hmda.messages.institution.InstitutionEvents.{ InstitutionCreated, InstitutionDeleted, InstitutionKafkaEvent, InstitutionModified }
import hmda.messages.pubsub.{ HmdaGroups, HmdaTopics }
import hmda.publication.KafkaUtils._
import hmda.serialization.kafka.InstitutionKafkaEventsDeserializer
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory

import scala.concurrent.Future

// $COVERAGE-OFF$
object HmdaInstitutionApi extends App {

  val log = LoggerFactory.getLogger("hmda")

  log.info("""
             | _____          _   _ _         _   _                    ___  ______ _____
             ||_   _|        | | (_) |       | | (_)                  / _ \ | ___ \_   _|
             |  | | _ __  ___| |_ _| |_ _   _| |_ _  ___  _ __  ___  / /_\ \| |_/ / | |
             |  | || '_ \/ __| __| | __| | | | __| |/ _ \| '_ \/ __| |  _  ||  __/  | |
             | _| || | | \__ \ |_| | |_| |_| | |_| | (_) | | | \__ \ | | | || |    _| |_
             | \___/_| |_|___/\__|_|\__|\__,_|\__|_|\___/|_| |_|___/ \_| |_/\_|    \___/
    """.stripMargin)

  val config = ConfigFactory.load()

  implicit val system       = ActorSystem("hmda-institutions")
  implicit val materializer = Materializer(system)
  implicit val ec           = system.dispatcher

  val host = config.getString("hmda.institution.http.host")
  val port = config.getString("hmda.institution.http.port")

  val kafkaConfig = system.settings.config.getConfig("akka.kafka.consumer")

  val jdbcUrl = config.getString("db.db.url")
  log.info(s"Connection URL is \n\n$jdbcUrl\n")

  system.spawn[Nothing](HmdaInstitutionQueryApi(), HmdaInstitutionQueryApi.name)

  val consumerSettings: ConsumerSettings[String, InstitutionKafkaEvent] =
    ConsumerSettings(kafkaConfig, new StringDeserializer, new InstitutionKafkaEventsDeserializer)
      .withBootstrapServers(kafkaHosts)
      .withGroupId(HmdaGroups.institutionsGroup)
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withProperties(getKafkaConfig)
  val control: DrainingControl[Done] = Consumer
    .committableSource(consumerSettings, Subscriptions.topics(HmdaTopics.institutionTopic))
    .mapAsync(1)(msg => processData(msg.record.value()).map(_ => msg.committableOffset))
    .toMat(Committer.sink(CommitterSettings(system).withParallelism(1)))(Keep.both)
    .mapMaterializedValue(DrainingControl.apply)
    .run()

  val institutionDBProjector =
    system.spawn(InstitutionDBProjection.behavior, InstitutionDBProjection.name)

  def processData(evt: InstitutionKafkaEvent): Future[Done] =
    Source
      .single(evt)
      .map { evt =>
        val evtType = evt.eventType match {
          case "InstitutionCreated" =>
            evt.institutionEvent.asInstanceOf[InstitutionCreated]
          case "InstitutionModified" =>
            evt.institutionEvent.asInstanceOf[InstitutionModified]
          case "InstitutionDeleted" =>
            evt.institutionEvent.asInstanceOf[InstitutionDeleted]
          case _ => evt.institutionEvent
        }
        institutionDBProjector ! ProjectEvent(evtType)
      }
      .toMat(Sink.ignore)(Keep.right)
      .run()

}
// $COVERAGE-ON$
