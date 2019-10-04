package hmda.parser.filing.ts

import hmda.parser.ParserErrorModel.{ParserValidationError, notNumeric}

object TsParserErrorModel {

  case class InvalidId(value: String) extends ParserValidationError {
    override def fieldName: String = "Record Identifier"
    override def inputValue: String = value.toString
    override def validValues: String = "1"
  }

  case class InvalidYear(value: String) extends ParserValidationError {
    override def fieldName: String = "Calendar Year"
    override def inputValue: String = value.toString
    override def validValues: String = ""
  }

  case class InvalidQuarter(value: String) extends ParserValidationError {
    override def fieldName: String = "Calendar Quarter"
    override def inputValue: String = value.toString
    override def validValues: String = ""
  }

  case class InvalidTotalLines(value: String) extends ParserValidationError {
    override def fieldName: String =
      "Total Number of Entries Contained in Submission"
    override def inputValue: String = value.toString
    override def validValues: String = ""
  }

  case class InvalidAgencyCode(value: String) extends ParserValidationError {
    override def fieldName: String = "Federal Agency"
    override def inputValue: String = value.toString
    override def validValues: String = ""
  }

}
