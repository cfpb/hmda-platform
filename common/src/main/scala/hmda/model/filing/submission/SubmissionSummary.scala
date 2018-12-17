package hmda.model.filing.submission

import hmda.model.filing.ts.TransmittalSheet

case class SubmissionSummary(
    submission: Option[Submission] = None,
    ts: Option[TransmittalSheet] = None
)
