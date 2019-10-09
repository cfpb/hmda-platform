package hmda.parser.filing.ts

import hmda.parser.ParserErrorModel.ParserValidationError

object TsParserErrorModel {

  case class IncorrectNumberOfFieldsTs(value: String)
      extends ParserValidationError {
    override def fieldName: String = "Incorrect Number of TS Fields"
    override def inputValue: String = value.toString
  }

  case class InvalidTsId(value: String) extends ParserValidationError {
    override def fieldName: String = "Transmittal Sheet Record Identifier"
    override def inputValue: String = value.toString
  }

  case class InvalidYear(value: String) extends ParserValidationError {
    override def fieldName: String = "Calendar Year"
    override def inputValue: String = value.toString
  }

  case class InvalidQuarter(value: String) extends ParserValidationError {
    override def fieldName: String = "Calendar Quarter"
    override def inputValue: String = value.toString
  }

  case class InvalidTotalLines(value: String) extends ParserValidationError {
    override def fieldName: String =
      "Total Number of Entries Contained in Submission"
    override def inputValue: String = value.toString
  }

  case class InvalidAgencyCode(value: String) extends ParserValidationError {
    override def fieldName: String = "Federal Agency"
    override def inputValue: String = value.toString
  }

}
