package hmda.parser

object ParserErrorModel {

  trait ParserValidationError {
    def fieldName: String
    def inputValue: String
  }

}
