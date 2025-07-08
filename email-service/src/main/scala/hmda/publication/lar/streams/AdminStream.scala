package hmda.publication.lar.streams

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.scaladsl._
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import cats.implicits._
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.publication.lar.email.{Email, EmailService}
import monix.eval.Task
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import scala.concurrent.Future
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import hmda.publication.KafkaUtils._
import com.typesafe.config.ConfigFactory

object AdminStream {
  val log =                 LoggerFactory.getLogger("hmda")
  val config = ConfigFactory.load()
  
  val ratespreadFailedSubject = config.getString("hmda.admin.rateSpreadFailureSubject")
  val ratespreadSuccessSubject = config.getString("hmda.admin.rateSpreadSuccessSubject")

  val adminDistro = config.getString("hmda.admin.adminEmail")

  def pullEmails(system: ActorSystem, bootstrapServers: String): SourceWithContext[
    CommittableMessage[String, String],
    CommittableOffset,
    Consumer.Control
  ]#Repr[ConsumerRecord[String, String], CommittableOffset] = {
    val settings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(HmdaGroups.emailGroup)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
        .withProperties(getKafkaConfig)
    
    Consumer
      .committableSource(settings, Subscriptions.topics(HmdaTopics.adminEmailTopic))
      .asSourceWithContext(_.committableOffset) // hide context
      .map(_.record)
  }

  private def retryBackoff[A](t: Task[A], maxRetries: Int, firstDelay: FiniteDuration): Task[A] =
    t.onErrorHandleWith {
      case ex: Exception =>
        if (maxRetries > 0)
          retryBackoff(t, maxRetries - 1, firstDelay * 2)
            .delayExecution(firstDelay)
        else
          Task.raiseError(ex)
    }

  def sendEmailAndUpdateStatus(
    emailService: EmailService,
    timesToRetry: Int = 4
  )(
    record: ConsumerRecord[String, String]
  ): Task[Either[Throwable, Unit]] = {
    val messageType     = record.key()
    val messageContent  = record.value()

      val sendEmail = messageType match {
          case "ratespreadFailed" => emailService.send(Email(adminDistro, ratespreadFailedSubject, messageContent))

          case "rateSpreadSucceeded" => emailService.send(Email(adminDistro, ratespreadSuccessSubject, messageContent))

          case _ => ().asRight.pure[Task]
      }

    retryBackoff(sendEmail, timesToRetry, 5.seconds)
  }

  def sendEmailsIfNecessary(
    emailService: EmailService,
    parallelism: Int = 2,
    timesToRetry: Int = 4
  )(
    implicit s: Scheduler
  ): FlowWithContext[ConsumerRecord[String, String], CommittableOffset, ConsumerRecord[String, String], CommittableOffset, NotUsed]#Repr[
    Either[Throwable, Unit],
    CommittableOffset
  ] =
    FlowWithContext[ConsumerRecord[String, String], CommittableOffset].mapAsync(parallelism) { record =>
     sendEmailAndUpdateStatus(emailService, timesToRetry)(record).runToFuture
    }

  def commitMessages(commitSettings: CommitterSettings): Sink[CommittableOffset, Future[Done]] =
    Flow[CommittableOffset]
      .via(Committer.flow(commitSettings))
      .toMat(Sink.ignore)(Keep.right)
}
