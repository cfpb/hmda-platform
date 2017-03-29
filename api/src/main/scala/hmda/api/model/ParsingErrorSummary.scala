package hmda.api.model

import hmda.model.fi.SubmissionStatus
import hmda.parser.fi.lar.LarParsingError

case class ParsingErrorSummary(
  transmittalSheetErrors: Seq[String],
  larErrors: Seq[LarParsingError],
  path: String,
  currentPage: Int,
  total: Int,
  status: SubmissionStatus
) extends PaginatedResponse
