package hmda.api.http.model.filing.submissions

import hmda.messages.submission.SubmissionProcessingEvents.HmdaRowParsedError
import hmda.model.filing.submission.{ ParsedWithErrors, SubmissionStatus }

case class ParsingErrorSummary(transmittalSheetErrors: Seq[String] = Nil,
                               larErrors: Seq[HmdaRowParsedError] = Nil,
                               path: String = "",
                               currentPage: Int = 0,
                               total: Int = 0,
                               status: SubmissionStatus = ParsedWithErrors)
    extends PaginatedResponse {
  def isEmpty: Boolean =
    this.transmittalSheetErrors == Nil && this.larErrors == Nil && path == "" && currentPage == 0 && total == 0 && status == ParsedWithErrors
}
