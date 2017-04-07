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

/*

When converted to JSON, the ParsingErrorSummary has this format:

ParsingErrorSummary(
  transmittalSheetErrors: Seq[String],
  larErrors: Seq[String],
  count: Int,
  total: Int,
  _links: PaginationLinks
)

This happens in ParserResultsProtocol.scala

 */
