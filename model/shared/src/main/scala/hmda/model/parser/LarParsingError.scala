package hmda.model.parser

case class LarParsingError(lineNumber: Int = 0, errorMessages: List[String] = List.empty)
