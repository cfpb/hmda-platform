package hmda.persistence.submission

import akka.NotUsed
import akka.actor.typed.ActorSystem
import akka.cluster.sharding.typed.scaladsl.{ ClusterSharding, EntityRef }
import akka.stream.scaladsl.Source
import hmda.messages.submission.HmdaRawDataEvents.LineAdded
import hmda.query.HmdaQuery._
import akka.util.Timeout
import hmda.messages.submission.SubmissionCommands.{ GetSubmission, ModifySubmission, SubmissionCommand }
import hmda.messages.submission.SubmissionEvents.SubmissionEvent
import hmda.messages.submission.SubmissionManagerCommands.UpdateSubmissionStatus
import hmda.model.filing.submission.{ Submission, SubmissionId, SubmissionStatus }
import org.slf4j.Logger

import scala.concurrent.{ ExecutionContext, Future }

object HmdaProcessingUtils {

  def readRawData(submissionId: SubmissionId)(implicit system: ActorSystem[_]): Source[LineAdded, NotUsed] = {

    val persistenceId = s"${HmdaRawData.name}-$submissionId"

    eventsByPersistenceId(persistenceId).collect {
      case evt: LineAdded => evt
    }

  }

  def updateSubmissionStatus(sharding: ClusterSharding, submissionId: SubmissionId, modified: SubmissionStatus, log: Logger)(
    implicit ec: ExecutionContext,
    timeout: Timeout
  ): Unit = {
    val submissionPersistence =
      sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-$submissionId")

    val submissionManager =
      sharding.entityRefFor(SubmissionManager.typeKey, s"${SubmissionManager.name}-$submissionId")

    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (ref => GetSubmission(ref))

    for {
      potentialSubmission <- fSubmission
      submission          = potentialSubmission.getOrElse(Submission())
    } yield {
      if (submission.isEmpty) {
        log
          .error(s"Submission $submissionId could not be retrieved")
      } else {
        val modifiedSubmission = submission.copy(status = modified)
        submissionManager ! UpdateSubmissionStatus(modifiedSubmission)
      }
    }
  }

  def updateSubmissionStatusAndReceipt(
                                        sharding: ClusterSharding,
                                        submissionId: SubmissionId,
                                        timestamp: Long,
                                        receipt: String,
                                        modified: SubmissionStatus,
                                        log: Logger,
                                        signerUsername: String
                                      )(
                                        implicit ec: ExecutionContext,
                                        timeout: Timeout
                                      ): Unit = {
    val submissionPersistence: EntityRef[SubmissionCommand] =
      sharding.entityRefFor(SubmissionPersistence.typeKey, s"${SubmissionPersistence.name}-$submissionId")

    val submissionManager =
      sharding.entityRefFor(SubmissionManager.typeKey, s"${SubmissionManager.name}-$submissionId")

    val fSubmission: Future[Option[Submission]] = submissionPersistence ? (ref => GetSubmission(ref))

    for {
      potentialSubmission <- fSubmission
      submission = potentialSubmission
        .map(e => e.copy(receipt = receipt, end = timestamp, signerUsername = Some(signerUsername)))
        .getOrElse(Submission())
    } yield {
      if (potentialSubmission.isEmpty) {
        log
          .error(s"Submission $submissionId could not be retrieved")
      } else {
        val modifiedSubmission = submission.copy(status = modified)
        submissionManager ! UpdateSubmissionStatus(modifiedSubmission)
        val fUpdated: Future[SubmissionEvent] = submissionPersistence ? (ref => ModifySubmission(submission, ref))
        fUpdated.map(e => log.debug(s"Updated receipt for submission $submissionId"))
      }
    }

  }

}