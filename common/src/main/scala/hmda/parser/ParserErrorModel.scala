package hmda.parser

object ParserErrorModel {

  def notNumeric(fieldName: String) = s"$fieldName is not numeric"

  def notStringOrNA(fieldName: String) =
    s"$fieldName is not a non-empty string or NA"

  def notStringOrNAOrExempt(fieldName: String) =
    s"$fieldName is not a non-empty string, NA or Exempt"

  def notEmptyStringOrNaOrExempt(fieldName: String) =
    s"$fieldName is not a valid value, an empty string, NA or Exempt"

  trait ParserValidationError {
    def fieldName: String
    def inputValue: String
    def validValues: String
  }

  case class IncorrectNumberOfFields(length: Int, expectedLength: Int)
      extends ParserValidationError {
    override def fieldName: String = "Number of fields"
    override def inputValue: String = length.toString
    override def validValues: String = expectedLength.toString
  }

}
