package hmda.validation.dsl

import scala.util.Try

trait CommonDsl {
  implicit class Subject[T](data: T) {
    private def test(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Success()
        case false => Failure(predicate.failure)
      }
    }

    private def testNot(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Failure(predicate.failure)
        case false => Success()
      }
    }

    def is(predicate: Predicate[T]): Result = {
      test(predicate)
    }

    def not(predicate: Predicate[T]): Result = {
      testNot(predicate)
    }

  }

  def equalTo[T](that: T): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _ == that
    override def failure: String = s"not equal to $that"
  }

  def greaterThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.gt(_, that)
    override def failure: String = s"not greater than $that"
  }

  def greaterThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.gteq(_, that)
    override def failure: String = s"not greater than $that"
  }

  def lessThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.lt(_, that)
    override def failure: String = s"not greater than $that"
  }

  def lessThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.lteq(_, that)
    override def failure: String = s"not greater than $that"
  }

  def between[T](lower: T, upper: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = { x => ord.lteq(lower, x) && ord.lteq(x, upper) }
    override def failure: String = s"not between $lower and $upper (inclusive)"
  }

  def numericallyBetween(lower: String, upper: String): Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = { x =>
      Try(between(BigDecimal(lower), BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
    }
    override def failure: String = s"not between $lower and $upper (inclusive)"
  }

  def containedIn[T](domain: Seq[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = domain.contains(_)
    override def failure: String = s"is not contained in valid values domain"
  }

  def containedIn[T](domain: Set[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = domain.contains
    override def failure: String = s"is not contained in valid values domain"
  }

  def numeric[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case n: Number => true
      case s: String => (s forall Character.isDigit) && !s.isEmpty
      case _ => throw new NotImplementedError("'numeric' doesn't handle non-number/string values yet")
    }
    override def failure: String = s"is not numeric"
  }

  def empty[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case s: String => s.isEmpty
      case _ => throw new NotImplementedError("'empty' doesn't handle non-string values yet")
    }
    override def failure: String = "is not empty"
  }

  def when(condition: Result)(thenTest: => Result): Result = {
    condition.implies(thenTest)
  }
}
