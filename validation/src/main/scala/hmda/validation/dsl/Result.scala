package hmda.validation.dsl

sealed trait Result {
  def and(that: Result): Result = {
    if (this == Success() && that == Success())
      Success()
    else
      Failure()
  }

  def or(that: Result): Result = {
    if (this == Success() || that == Success())
      Success()
    else
      Failure()
  }

  def implies(that: => Result): Result = {
    this match {
      case Success() => that
      case Failure() => Success()
    }
  }

}
case class Success() extends Result
case class Failure() extends Result

