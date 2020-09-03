package hmda.serialization.submission

import hmda.model.filing.submission._
import hmda.persistence.serialization.submission.{ SubmissionIdMessage, SubmissionMessage }
import hmda.utils.YearUtils._

object SubmissionProtobufConverter {

  def submissionToProtobuf(submission: Submission): SubmissionMessage =
    SubmissionMessage(
      submissionIdToProtobuf(submission.id),
      submission.status.code,
      submission.start,
      submission.end,
      submission.fileName,
      submission.receipt,
      submission.signerUsername.getOrElse("")
    )

  def submissionFromProtobuf(submissionMessage: SubmissionMessage): Submission =
    Submission(
      submissionIdFromProtobuf(submissionMessage.submissionId.getOrElse(SubmissionIdMessage())),
      submissionStatusFromProtobuf(submissionMessage.status),
      submissionMessage.start,
      submissionMessage.end,
      submissionMessage.fileName,
      submissionMessage.receipt,
      Option(submissionMessage.signerUsername).filter(_.nonEmpty)
    )

  def submissionIdToProtobuf(submissionId: SubmissionId): Option[SubmissionIdMessage] =
    if (submissionId.isEmpty) None
    else {
      Some(
        SubmissionIdMessage(
          submissionId.lei,
          submissionId.period.toString,
          submissionId.sequenceNumber
        )
      )
    }

  def submissionIdFromProtobuf(submissionIdMessage: SubmissionIdMessage): SubmissionId =
    SubmissionId(
      submissionIdMessage.lei,
      parsePeriod(submissionIdMessage.period).right.get,
      submissionIdMessage.sequenceNumber
    )

  private def submissionStatusFromProtobuf(code: Int): SubmissionStatus =
    code match {
      case 1  => Created
      case 2  => Uploading
      case 3  => Uploaded
      case 4  => Parsing
      case 5  => ParsedWithErrors
      case 6  => Parsed
      case 7  => Validating
      case 8  => SyntacticalOrValidity
      case 9  => SyntacticalOrValidityErrors
      case 10 => Quality
      case 11 => QualityErrors
      case 12 => Macro
      case 13 => MacroErrors
      case 14 => Verified
      case 15 => Signed
      case -1 => Failed
    }

}
