package hmda.publication.lar.streams

import java.text.SimpleDateFormat

import akka.actor.ActorSystem
import akka.kafka.ConsumerMessage.{CommittableMessage, CommittableOffset}
import akka.kafka.scaladsl._
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscriptions}
import akka.stream.scaladsl._
import akka.{Done, NotUsed}
import cats.implicits._
import hmda.messages.pubsub.{HmdaGroups, HmdaTopics}
import hmda.model.filing.submission.{Submission, SubmissionId}
import hmda.publication.lar.database.{EmailSubmissionMetadata, EmailSubmissionStatusRepository => SubmissionStatusRepo}
import hmda.publication.lar.email.{Email, EmailService}
import hmda.utils.YearUtils
import hmda.utils.YearUtils.Period
import monix.eval.Task
import monix.execution.Scheduler
import org.apache.kafka.clients.consumer.{ConsumerConfig, ConsumerRecord}
import org.apache.kafka.common.serialization.StringDeserializer
import java.util.TimeZone
import scala.concurrent.Future
import scala.concurrent.duration._
import org.slf4j.LoggerFactory
import hmda.publication.KafkaUtils._

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
        .withProperties(getKafkaConfig)
    
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

  def submissionId(submissionWithReceipt: String): Submission = {
    submissionWithReceipt.split("-").size match {
      case 4 =>
        val lei :: year :: seqNo :: receipt :: Nil = submissionWithReceipt.split("-").take(4).toList
        Submission(id = SubmissionId(lei, YearUtils.parsePeriod(year).right.get, seqNo.toInt), receipt = receipt)
      case 5 =>
        val lei :: year :: quarter :: seqNo :: receipt :: Nil = submissionWithReceipt.split("-").take(5).toList
        Submission(id = SubmissionId(lei, Period(year.toInt, Some(quarter)), seqNo.toInt), receipt = receipt)
    }
  }

  def sendEmailAndUpdateStatus(
    emailService: EmailService,
    submissionStatusRepo: SubmissionStatusRepo,
    emailContent: String,
    emailSubject: String,
    timesToRetry: Int = 4,
    bankFilterList: Array[String]
  )(
    record: ConsumerRecord[String, String]
  ): Task[Either[Throwable, Unit]] = {
    val rawSubmissionId = record.key()
    val toAddress       = record.value()
    val submission             = submissionId(rawSubmissionId)

   if (!bankFilterList.contains(submission.id.lei)) {
     log.info(s"Working on ${rawSubmissionId} and ${toAddress}")

     val df: SimpleDateFormat = new SimpleDateFormat("MMMM dd, YYYY, hh:mm:ss a")
     df.setTimeZone(TimeZone.getTimeZone("EST"))

     val formattedEmail = emailContent.replaceAll("<period>", submission.id.period.toString)
       .replaceAll("<datetime>", df.format(submission.receipt.toLong))
       .replaceAll("<uniq_id>", submission.id.toString + "-" + submission.receipt)

     val process = for {
       optPresent <- submissionStatusRepo.findBySubmissionId(submission.id)
       // decide whether to send email based on if we've already done it

       sendEmail = optPresent match {
         case None => emailService.send(Email(toAddress, emailSubject, formattedEmail))

         case Some(status) if status.successful => ().asRight.pure[Task]

         case Some(_) => emailService.send(Email(toAddress, emailSubject, formattedEmail))
       }
       status <- sendEmail
       metadata = EmailSubmissionMetadata(submission.id, toAddress)
       recordSubmission = status match {
         case Left(error) => submissionStatusRepo.recordEmailFailed(metadata, error.getMessage)

         case Right(_) => submissionStatusRepo.recordEmailSent(metadata)
       }
       _ <- recordSubmission
     } yield status

     retryBackoff(process, timesToRetry, 5.seconds)
   }else {
     log.info(s"Filter sending email for ${rawSubmissionId}")
      ().asRight.pure[Task]
   }
  }

  def sendEmailsIfNecessary(
    emailService: EmailService,
    submissionStatusRepo: SubmissionStatusRepo,
    emailContent: String,
    emailSubject: String,
    parallelism: Int = 2,
    timesToRetry: Int = 4,
    bankFilterList: Array[String]
  )(
    implicit s: Scheduler
  ): FlowWithContext[ConsumerRecord[String, String], CommittableOffset, ConsumerRecord[String, String], CommittableOffset, NotUsed]#Repr[
    Either[Throwable, Unit],
    CommittableOffset
  ] =
    FlowWithContext[ConsumerRecord[String, String], CommittableOffset].mapAsync(parallelism) { record =>
      sendEmailAndUpdateStatus(emailService, submissionStatusRepo, emailContent, emailSubject, timesToRetry,bankFilterList)(record).runToFuture
    }

  def commitMessages(commitSettings: CommitterSettings): Sink[CommittableOffset, Future[Done]] =
    Flow[CommittableOffset]
      .via(Committer.flow(commitSettings))
      .toMat(Sink.ignore)(Keep.right)
}
