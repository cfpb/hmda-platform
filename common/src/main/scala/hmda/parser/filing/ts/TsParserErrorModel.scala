package hmda.parser.filing.ts

import hmda.parser.ParserErrorModel.{ notNumeric, ParserValidationError }

object TsParserErrorModel {

  case object InvalidId extends ParserValidationError {
    override def errorMessage: String = notNumeric("id")
  }

  case object InvalidYear extends ParserValidationError {
    override def errorMessage: String = notNumeric("year")
  }

  case object InvalidQuarter extends ParserValidationError {
    override def errorMessage: String = notNumeric("quarter")
  }

  case object InvalidTotalLines extends ParserValidationError {
    override def errorMessage: String = notNumeric("total lines")
  }

  case object InvalidAgencyCode extends ParserValidationError {
    override def errorMessage: String = notNumeric("agency code")
  }

}
