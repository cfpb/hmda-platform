package hmda.validation.dsl

sealed trait Result {
  def and(that: Result): Result = {
    if (this == Success() && that == Success())
      Success()
    else
      Failure(s"$this $that")
  }

  def or(that: Result): Result = {
    if (this == Success() || that == Success())
      Success()
    else
      Failure(s"$this $that")
  }

  def implies(that: => Result): Result = {
    this match {
      case Success() => that
      case Failure(_) => Success()
    }
  }

}
case class Success() extends Result
case class Failure(message: String) extends Result

