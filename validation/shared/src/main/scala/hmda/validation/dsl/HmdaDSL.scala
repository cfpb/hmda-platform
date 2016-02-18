package hmda.validation.dsl

object HmdaDSL {

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

  }
  case class Success() extends Result
  case class Failure(message: String) extends Result

  sealed trait Predicate[T] {
    def validate: T => Boolean
    def failure: String
  }

  implicit class Rule[T](data: T) {
    private def test(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Success()
        case false => Failure(predicate.failure)
      }
    }

    def is(predicate: Predicate[T]): Result = {
      test(predicate)
    }

  }

  def equalTo[T](that: T): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _ == that
    override def failure: String = s"not equal to $that"
  }

  def containedIn[T](list: List[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = list.contains(_)
    override def failure: String = s"is not contained in valid values domain"
  }

}
