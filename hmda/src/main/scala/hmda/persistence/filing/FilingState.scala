package hmda.persistence.filing

import hmda.messages.filing.FilingEvents.{ FilingCreated, FilingEvent, SubmissionAdded, SubmissionUpdated }
import hmda.model.filing.Filing
import hmda.model.filing.submission.{ Signed, Submission, SubmissionStatus }

case class FilingState(filing: Filing = Filing(), submissions: List[Submission] = Nil) {
  def update(event: FilingEvent): FilingState =
    event match {
      case FilingCreated(f) =>
        if (this.filing.isEmpty) {
          FilingState(f, this.submissions)
        } else {
          this
        }
      case SubmissionAdded(submission) =>
        if (submissions.contains(submission)) {
          this
        } else {
          FilingState(this.filing, submission :: submissions)
        }
      case SubmissionUpdated(updated) =>
        if (submissions.map(_.id).contains(updated.id)
            && !isSigned(updated)
            && (!isSigned(
              submissions
                .filter(_.id == updated.id)
                .headOption
                .getOrElse(Submission())
            ))) {
          val updatedList = updated :: submissions.filterNot(s => s.id == updated.id)
          FilingState(this.filing, updatedList)
        } else if (submissions.map(_.id).contains(updated.id) && isSigned(updated)) {
          val updatedList = updated.copy(status = SubmissionStatus.valueOf(Signed.code), receipt = s"${updated.id}-${updated.end}") :: submissions
            .filterNot(s => s.id == updated.id)
          FilingState(this.filing, updatedList)
        } else {
          this
        }
      case _ => this
    }

  private def isSigned(updated: Submission): Boolean =
    return updated.end != 0 || updated.status == SubmissionStatus
      .valueOf(Signed.code) || !updated.receipt.isEmpty
}
