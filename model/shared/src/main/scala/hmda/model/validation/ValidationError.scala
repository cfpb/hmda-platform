package hmda.model.validation

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

// errorID = Loan ID (LAR) or Agency Code + Respondent ID (TS)
trait ValidationError {
  def errorId: String
  def ruleName: String
  def errorType: ValidationErrorType
  def ts: Boolean
  def toCsv: String = s"$errorType, $ruleName, $publicErrorId"
  def publicErrorId = if (ts) "Transmittal Sheet" else errorId
}
