package hmda.parser

object ParserErrorModel {

  trait ParserValidationError {
    def fieldName: String
    def inputValue: String
  }

  case class IncorrectNumberOfFields(length: Int)
      extends ParserValidationError {
    override def fieldName: String = "Number of Fields"
    override def inputValue: String = length.toString
  }

}
