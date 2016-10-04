package hmda.persistence.institutions.serialization

import hmda.model.fi._
import hmda.model.submission.{ SubmissionIdMessage, SubmissionMessage, SubmissionStatusMessage }

import scala.language.implicitConversions

object SubmissionConverter {

  implicit def messageToSubmission(m: Option[SubmissionMessage]): Submission = {
    m.map { s =>
      val submissionId = s.submissionId
      val status = s.submissionStatus
      Submission(submissionId, status)
    }
  }.getOrElse(Submission())

  implicit def messageToSubmissionId(m: Option[SubmissionIdMessage]): SubmissionId = {
    m.map { id =>
      val institutionId = id.institutionId
      val period = id.period
      val seqNr = id.sequenceNumber
      SubmissionId(institutionId, period, seqNr)
    }.getOrElse(SubmissionId())
  }

  implicit def messageToSubmissionStatus(m: Option[SubmissionStatusMessage]): SubmissionStatus = {
    m.map { x =>
      val code = x.code
      code match {
        case 1 => Created
        case 2 => Uploading
        case 3 => Uploaded
        case 4 => Parsing
        case 5 => Parsed
        case 6 => ParsedWithErrors
        case 7 => Validating
        case 8 => ValidatedWithErrors
        case 9 => Validated
        case 10 => IRSGenerated
        case 11 => IRSVerified
        case 12 => Signed
        case -1 => Failed
      }
    }.getOrElse(Created).asInstanceOf[SubmissionStatus]
  }

  implicit def submissionToMessage(submission: Submission): Option[SubmissionMessage] = {
    val institutionId = submission.id.institutionId
    val period = submission.id.period
    val seqNr = submission.id.sequenceNumber
    val submissionId = SubmissionIdMessage(institutionId, period, seqNr)
    val status = SubmissionStatusMessage(submission.submissionStatus.code)
    Some(
      SubmissionMessage(Some(submissionId), Some(status))
    )
  }

  implicit def submissionIdToMessage(submissionId: SubmissionId): Option[SubmissionIdMessage] = {
    val institutionId = submissionId.institutionId
    val period = submissionId.period
    val seqNr = submissionId.sequenceNumber
    Some(SubmissionIdMessage(institutionId, period, seqNr))
  }

  implicit def submissionStatusToMessage(submissionStatus: SubmissionStatus): Option[SubmissionStatusMessage] = {
    val code = submissionStatus.code
    Some(SubmissionStatusMessage(code))
  }

}
