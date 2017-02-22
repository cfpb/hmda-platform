package hmda.api.model

import hmda.parser.fi.lar.LarParsingError

case class ParsingErrorSummary(
  transmittalSheetErrors: Seq[String],
  larErrors: Seq[LarParsingError],
  path: String,
  currentPage: Int,
  total: Int
) extends WithPagination
