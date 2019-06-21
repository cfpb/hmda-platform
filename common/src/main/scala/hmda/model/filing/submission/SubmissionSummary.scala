package hmda.model.filing.submission

import hmda.model.filing.ts._2018.TransmittalSheet

case class SubmissionSummary(
    submission: Option[Submission] = None,
    ts: Option[TransmittalSheet] = None
)
