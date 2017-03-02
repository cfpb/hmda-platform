package hmda.parser.fi.lar

case class LarParsingError(lineNumber: Int = 0, errorMessages: List[String] = List.empty)
