package hmda.persistence.submission

import akka.NotUsed
import akka.actor.ActorSystem
import akka.actor.typed.Logger
import akka.cluster.sharding.typed.scaladsl.{ClusterSharding, EntityRef}
import akka.stream.scaladsl.Source
import akka.util.Timeout
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.messages.submission.SubmissionCommands.{
  GetSubmission,
  ModifySubmission,
  SubmissionCommand
}
import hmda.messages.submission.SubmissionEvents.SubmissionEvent
import hmda.messages.submission.SubmissionManagerCommands.UpdateSubmissionStatus
import hmda.model.filing.submission.{Submission, SubmissionId, SubmissionStatus}
import hmda.query.HmdaQuery._

import scala.concurrent.{ExecutionContext, Future}

object HmdaProcessingUtils {

  def readRawData(submissionId: SubmissionId)(
      implicit system: ActorSystem): Source[LineAdded, NotUsed] = {

    val persistenceId = s"${HmdaRawData.name}-$submissionId"

    eventsByPersistenceId(persistenceId)
      .collect {
        case evt: LineAdded => evt
      }

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
