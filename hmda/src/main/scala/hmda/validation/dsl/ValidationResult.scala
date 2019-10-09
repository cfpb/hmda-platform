package hmda.validation.dsl

sealed trait ValidationResult {

  def and(that: ValidationResult): ValidationResult =
    if (this == ValidationSuccess && that == ValidationSuccess)
      ValidationSuccess
    else
      ValidationFailure

  def or(that: ValidationResult): ValidationResult =
    if (this == ValidationSuccess || that == ValidationSuccess)
      ValidationSuccess
    else
      ValidationFailure

  def implies(that: => ValidationResult): ValidationResult =
    this match {
      case ValidationSuccess => that
      case ValidationFailure => ValidationSuccess
    }

}

case object ValidationSuccess extends ValidationResult
case object ValidationFailure extends ValidationResult
