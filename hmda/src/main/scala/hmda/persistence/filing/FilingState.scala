package hmda.persistence.filing

import hmda.messages.filing.FilingEvents.{
  FilingCreated,
  FilingEvent,
  SubmissionAdded
}
import hmda.model.filing.Filing
import hmda.model.filing.submission.Submission

case class FilingState(filing: Filing = Filing(),
                       submissions: List[Submission] = Nil) {
  def update(event: FilingEvent): FilingState = {
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
      case _ => this
    }
  }
}
