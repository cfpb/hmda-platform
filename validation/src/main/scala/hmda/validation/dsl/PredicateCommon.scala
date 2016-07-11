package hmda.validation.dsl

import scala.language.implicitConversions
import scala.util.Try

object PredicateCommon {
  implicit def equalTo[T](that: T): Predicate[T] = (_: T) == that

  implicit def greaterThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = ord.gt(_: T, that)

  implicit def greaterThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = ord.gteq(_: T, that)

  implicit def lessThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = ord.lt(_: T, that)

  implicit def lessThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = ord.lteq(_: T, that)

  implicit def between[T](lower: T, upper: T)(implicit ord: Ordering[T]): Predicate[T] = { x: T =>
    ord.lteq(lower, x) && ord.lteq(x, upper)
  }

  implicit def numericallyBetween(lower: String, upper: String): Predicate[String] = { x: String =>
    Try(between(BigDecimal(lower), BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
  }

  implicit def numericallyLessThan(upper: String): Predicate[String] = { x: String =>
    Try(lessThan(BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
  }

  implicit def numericallyLessThanOrEqual(upper: String): Predicate[String] = { x: String =>
    Try(lessThanOrEqual(BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
  }

  implicit def oneOf[T](domain: T*): Predicate[T] = containedIn(domain)

  implicit def containedIn[T](domain: Seq[T]): Predicate[T] = domain.contains(_: T)

  def containedIn[T](domain: Set[T]): Predicate[T] = domain.contains(_: T)

  def numeric[T]: Predicate[T] = (_: T) match {
    case n: Number => true
    case s: String => Try(s.toDouble).isSuccess
    case _ => throw new NotImplementedError("'numeric' doesn't handle non-number/string values yet")
  }

  implicit def empty[T]: Predicate[T] = (_: T) match {
    case s: String => s.isEmpty
    case _ => throw new NotImplementedError("'empty' doesn't handle non-string values yet")
  }

  implicit def when(condition: Result)(thenTest: => Result): Result = {
    condition.implies(thenTest)
  }

  implicit def func2predicate[T](f: T => Boolean): Predicate[T] = new Predicate[T] {
    override def validate = f
  }
}
