package hmda.parser

object ParserErrorModel {

  def notAnInteger(fieldName: String) = s"$fieldName is not an integer"

  trait ParserValidationError {
    def errorMessage: String
  }

  case class IncorrectNumberOfFields(length: Int)
      extends ParserValidationError {
    override def errorMessage: String =
      s"An incorrect number of data fields were reported: $length data fields were found, when 39 data fields were expected."
  }

}
