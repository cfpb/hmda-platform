package hmda.persistence.submission

import akka.actor.typed.{ActorContext, Logger}
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{ConsumerSettings, Subscriptions}
import akka.kafka.scaladsl.Consumer
import akka.stream.scaladsl.Source
import akka.util.Timeout
import hmda.messages.pubsub.KafkaTopics.uploadTopic
import hmda.messages.submission.SubmissionCommands.{
  GetSubmission,
  ModifySubmission,
  SubmissionCommand
}
import hmda.messages.submission.SubmissionEvents.SubmissionEvent
import hmda.messages.submission.SubmissionManagerCommands.UpdateSubmissionStatus
import hmda.model.filing.submission.{Submission, SubmissionId, SubmissionStatus}
import hmda.persistence.submission.HmdaParserError.kafkaHosts
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer

import scala.concurrent.{ExecutionContext, Future}

object HmdaProcessingUtils {

  def uploadConsumer(ctx: ActorContext[_], submissionId: SubmissionId)
    : Source[CommittableMessage[String, String], Consumer.Control] = {

    val kafkaConfig =
      ctx.asScala.system.settings.config.getConfig("akka.kafka.consumer")
    val consumerSettings =
      ConsumerSettings(kafkaConfig,
                       new StringDeserializer,
                       new StringDeserializer)
        .withBootstrapServers(kafkaHosts)
        .withGroupId(submissionId.toString)
        .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

    Consumer
      .committableSource(consumerSettings, Subscriptions.topics(uploadTopic))
      .filter(_.record.key() == submissionId.toString)

  }

  def updateSubmissionStatus(
      sharding: ClusterSharding,
      submissionId: SubmissionId,
      modified: SubmissionStatus,
      log: Logger)(implicit ec: ExecutionContext, timeout: Timeout): Unit = {
    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey,
                            s"${SubmissionPersistence.name}-$submissionId")

    val submissionManager =
      sharding.entityRefFor(SubmissionManager.typeKey,
                            s"${SubmissionManager.name}-$submissionId")

    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (
        ref => GetSubmission(ref))

    for {
      m <- fSubmission
      s = m.getOrElse(Submission())
    } yield {
      if (s.isEmpty) {
        log
          .error(s"Submission $submissionId could not be retrieved")
      } else {
        val modifiedSubmission = s.copy(status = modified)
        submissionManager ! UpdateSubmissionStatus(modifiedSubmission)
      }
    }
  }

  def updateSubmissionReceipt(
      sharding: ClusterSharding,
      submissionId: SubmissionId,
      timestamp: Long,
      receipt: String,
      log: Logger)(implicit ec: ExecutionContext, timeout: Timeout): Unit = {
    val submissionPersistence: EntityRef[SubmissionCommand] =
      sharding.entityRefFor(SubmissionPersistence.typeKey,
                            s"${SubmissionPersistence.name}-$submissionId")

    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (
        ref => GetSubmission(ref))

    for {
      s <- fSubmission
      m = s
        .map(e => e.copy(receipt = receipt, end = timestamp))
        .getOrElse(Submission())
    } yield {
      if (s.isEmpty) {
        log
          .error(s"Submission $submissionId could not be retrieved")
      } else {
        val fUpdated: Future[SubmissionEvent] = submissionPersistence ? (ref =>
          ModifySubmission(m, ref))
        fUpdated.map(e =>
          log.debug(s"Updated receipt for submission $submissionId"))
      }
    }

  }

}
