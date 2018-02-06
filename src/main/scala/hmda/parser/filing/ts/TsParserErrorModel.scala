package hmda.parser.filing.ts

import hmda.parser.ParserErrorModel._

object TsParserErrorModel {

  case object InvalidId extends ParserValidationError {
    override def errorMessage: String = notAnInteger("id")
  }

  case object InvalidYear extends ParserValidationError {
    override def errorMessage: String = notAnInteger("year")
  }

  case object InvalidQuarter extends ParserValidationError {
    override def errorMessage: String = notAnInteger("quarter")
  }

  case object InvalidTotalLines extends ParserValidationError {
    override def errorMessage: String = notAnInteger("total lines")
  }

  case object InvalidAgencyCode extends ParserValidationError {
    override def errorMessage: String = notAnInteger("agency code")
  }


}
