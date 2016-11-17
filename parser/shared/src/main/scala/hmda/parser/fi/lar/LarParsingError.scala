package hmda.parser.fi.lar

case class LarParsingError(lineNumber: Int = 0, errorMessages: List[String] = List.empty)
case class ParsingErrorSummary(transmittalSheetErrors: Seq[String] = Seq.empty, larErrors: Seq[LarParsingError] = Seq.empty)
