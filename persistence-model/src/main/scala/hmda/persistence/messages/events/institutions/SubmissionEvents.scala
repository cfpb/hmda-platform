package hmda.persistence.messages.events.institutions

import akka.persistence.journal.{ EventAdapter, EventSeq }
import hmda.model.fi.{ Submission, SubmissionId, SubmissionStatus }
import hmda.persistence.messages.CommonMessages.Event
import hmda.persistence.messages.events.institutions.SubmissionEvents.{ SubmissionStatusUpdated, SubmissionStatusUpdatedV2 }

object SubmissionEvents {
  trait SubmissionEvent extends Event
  case class SubmissionCreated(submission: Submission) extends SubmissionEvent
  case class SubmissionStatusUpdated(id: SubmissionId, status: SubmissionStatus) extends SubmissionEvent
  case class SubmissionStatusUpdatedV2(id: SubmissionId, status: SubmissionStatus, time: Long) extends SubmissionEvent
  case class SubmissionFileNameAdded(id: SubmissionId, fileName: String) extends Event
}
