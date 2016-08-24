package hmda.validation.engine

sealed trait ValidationErrorType
case object Syntactical extends ValidationErrorType
case object Validity extends ValidationErrorType
case object Quality extends ValidationErrorType
case object Macro extends ValidationErrorType

// errorID = Loan ID (LAR) or Agency Code + Respondent ID (TS)
case class ValidationError(errorId: String, name: String, errorType: ValidationErrorType)
case class ValidationErrors(errors: Seq[ValidationError])
case class ValidationErrorsSummary(errors: Seq[String])

