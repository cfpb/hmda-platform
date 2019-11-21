package hmda.publication.lar.streams

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{ CommittableMessage, CommittableOffset }
import akka.kafka.scaladsl._
import akka.kafka.{ CommitterSettings, ConsumerSettings, Subscriptions }
import akka.stream.scaladsl._
import akka.{ Done, NotUsed }
import cats.implicits._
import hmda.messages.pubsub.{ HmdaGroups, HmdaTopics }
import hmda.model.filing.submission.SubmissionId
import hmda.publication.lar.database.{ EmailSubmissionMetadata, EmailSubmissionStatusRepository => SubmissionStatusRepo }
import hmda.publication.lar.email.{ Email, EmailService }
import monix.eval.Task
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.{ ConsumerConfig, ConsumerRecord }
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.Future
import scala.concurrent.duration._
import org.slf4j.LoggerFactory

object Stream {
  val log = LoggerFactory.getLogger("hmda")
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

    Consumer
      .committableSource(settings, Subscriptions.topics(HmdaTopics.emailTopic))
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

  def submissionId(s: String): SubmissionId = {
    val lei :: period :: seqNo :: Nil = s.split("-").take(3).toList
    SubmissionId(lei, period, seqNo.toInt)
  }

  def sendEmailAndUpdateStatus(
    emailService: EmailService,
    submissionStatusRepo: SubmissionStatusRepo,
    emailContent: String,
    timesToRetry: Int = 4
  )(
    record: ConsumerRecord[String, String]
  ): Task[Either[Throwable, Unit]] = {
    val rawSubmissionId = record.key()
    val toAddress       = record.value()
    val sId             = submissionId(rawSubmissionId)

    log.info(s"Working on ${rawSubmissionId} and ${toAddress}")

    val process = for {
      optPresent <- submissionStatusRepo.findBySubmissionId(sId)
      // decide whether to send email based on if we've already done it

      sendEmail = optPresent match {
        case None => emailService.send(Email(toAddress, sId.toString, emailContent))

        case Some(status) if status.successful => ().asRight.pure[Task]

        case Some(_) => emailService.send(Email(toAddress, sId.toString, emailContent))
      }
      status   <- sendEmail
      metadata = EmailSubmissionMetadata(sId, toAddress)
      recordSubmission = status match {
        case Left(error) => submissionStatusRepo.recordEmailFailed(metadata, error.getMessage)

        case Right(_) => submissionStatusRepo.recordEmailSent(metadata)
      }
      _ <- recordSubmission
    } yield status

    retryBackoff(process, timesToRetry, 5.seconds)
  }

  def sendEmailsIfNecessary(
    emailService: EmailService,
    submissionStatusRepo: SubmissionStatusRepo,
    emailContent: String,
    parallelism: Int = 2,
    timesToRetry: Int = 4
  )(
    implicit s: Scheduler
  ): FlowWithContext[ConsumerRecord[String, String], CommittableOffset, ConsumerRecord[String, String], CommittableOffset, NotUsed]#Repr[
    Either[Throwable, Unit],
    CommittableOffset
  ] =
    FlowWithContext[ConsumerRecord[String, String], CommittableOffset].mapAsync(parallelism) { record =>
      sendEmailAndUpdateStatus(emailService, submissionStatusRepo, emailContent, timesToRetry)(record).runToFuture
    }

  def commitMessages(commitSettings: CommitterSettings): Sink[CommittableOffset, Future[Done]] =
    Flow[CommittableOffset]
      .via(Committer.flow(commitSettings))
      .toMat(Sink.ignore)(Keep.right)
}
