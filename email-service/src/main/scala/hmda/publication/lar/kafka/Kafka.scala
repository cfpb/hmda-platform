package hmda.publication.lar.kafka

import akka.{ Done, NotUsed }
import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.scaladsl._
import akka.kafka.{ CommitterSettings, ConsumerSettings, Subscriptions }
import akka.stream.scaladsl._
import cats.implicits._
import hmda.publication.lar.email.{ Email, EmailService }
import monix.eval.Task
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future
import scala.concurrent.duration._

object Kafka {
  def pullEmails(system: ActorSystem,
                 bootstrapServers: String,
                 topic: String,
                 applicationGroupId: String): Source[CommittableMessage[String, String], Consumer.Control] = {
    val settings =
      ConsumerSettings(system, new StringDeserializer, new StringDeserializer)
        .withBootstrapServers(bootstrapServers)
        .withGroupId(applicationGroupId)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer.committableSource(settings, Subscriptions.topics(topic))
  }
  Committer

  private def retryBackoff[A](source: Task[A], maxRetries: Int, firstDelay: FiniteDuration): Task[A] =
    source.onErrorHandleWith {
      case ex: Exception =>
        if (maxRetries > 0)
          retryBackoff(source, maxRetries - 1, firstDelay * 2)
            .delayExecution(firstDelay)
        else
          Task.raiseError(ex)
    }

  def sendEmails(emailService: EmailService, emailContent: String, parallelism: Int = 2, timesToRetry: Int = 4)(
    implicit s: Scheduler
  ): Flow[CommittableMessage[String, String], (Either[Throwable, Unit], CommittableMessage[String, String]), NotUsed] =
    Flow[CommittableMessage[String, String]]
      .mapAsync(parallelism) { kafkaMessage =>
        // TODO: determine what needs to be done with the key (submission ID)
        val subject   = kafkaMessage.record.key()
        val toAddress = kafkaMessage.record.value()
        val email     = Email(toAddress, subject, emailContent)
        val response =
          retryBackoff(emailService.send(email), timesToRetry, 1.second)
        response.tupleRight(kafkaMessage).runToFuture
      }

  def commitMessages(commitSettings: CommitterSettings): Sink[(Either[Throwable, Unit], CommittableMessage[String, String]), Future[Done]] =
    Flow[(Either[Throwable, Unit], CommittableMessage[String, String])].map { case (_, commit) => commit.committableOffset }
      .via(Committer.flow(commitSettings))
      .toMat(Sink.ignore)(Keep.right)
}
