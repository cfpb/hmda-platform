package hmda.parser

import com.typesafe.config.ConfigFactory

object ParserErrorModel {

  val config = ConfigFactory.load()
  val expectedLegth = config.getInt("hmda.filing.ts.length")

  def notAnInteger(fieldName: String) = s"$fieldName is not an integer"

  trait ParserValidationError {
    def errorMessage: String
  }

  case class IncorrectNumberOfFields(length: Int)
      extends ParserValidationError {
    override def errorMessage: String =
      s"An incorrect number of data fields were reported: $length data fields were found, when $expectedLegth data fields were expected."
  }

}
