package hmda.validation.dsl

import scala.language.implicitConversions
import scala.util.Try

object PredicateCommon {

  implicit def equalTo[T](that: T): Predicate[T] = (_: T) == that

  def greaterThan[T: Ordering](that: T): Predicate[T] = implicitly[Ordering[T]].gt(_: T, that)

  def greaterThanOrEqual[T: Ordering](that: T): Predicate[T] = implicitly[Ordering[T]].gteq(_: T, that)

  def lessThan[T: Ordering](that: T): Predicate[T] = implicitly[Ordering[T]].lt(_: T, that)

  def lessThanOrEqual[T: Ordering](that: T): Predicate[T] = implicitly[Ordering[T]].lteq(_: T, that)

  def between[T: Ordering](lower: T, upper: T): Predicate[T] = { x: T =>
    val ord = implicitly[Ordering[T]]
    ord.lteq(lower, x) && ord.lteq(x, upper)
  }

  def numericallyBetween(lower: String, upper: String): Predicate[String] = { x: String =>
    Try(between(BigDecimal(lower), BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
  }

  def numericallyLessThan(upper: String): Predicate[String] = { x: String =>
    Try(lessThan(BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
  }

  def numericallyLessThanOrEqual(upper: String): Predicate[String] = { x: String =>
    Try(lessThanOrEqual(BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
  }

  def oneOf[T](domain: T*): Predicate[T] = containedIn(domain)

  def containedIn[T](domain: Seq[T]): Predicate[T] = domain.contains(_: T)

  def containedIn[T](domain: Set[T]): Predicate[T] = domain.contains(_: T)

  def numeric[T]: Predicate[T] = (_: T) match {
    case n: Number => true
    case s: String => Try(s.toDouble).isSuccess
    case _ => throw new NotImplementedError("'numeric' doesn't handle non-number/string values yet")
  }

  def empty[T]: Predicate[T] = (_: T) match {
    case s: String => s.isEmpty
    case _ => throw new NotImplementedError("'empty' doesn't handle non-string values yet")
  }

  def when(condition: Result)(thenTest: => Result): Result = {
    condition.implies(thenTest)
  }
}
