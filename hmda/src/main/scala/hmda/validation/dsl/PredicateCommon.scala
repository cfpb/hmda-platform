package hmda.validation.dsl

import scala.util.Try

object PredicateCommon {

  def equalTo[A](that: A): Predicate[A] = (_: A) == that

  def greaterThan[A: Ordering](that: A): Predicate[A] =
    implicitly[Ordering[A]].gt(_: A, that)

  def greaterThanOrEqual[A: Ordering](that: A): Predicate[A] =
    implicitly[Ordering[A]].gteq(_: A, that)

  def lessThan[A: Ordering](that: A): Predicate[A] =
    implicitly[Ordering[A]].lt(_: A, that)

  def lessThan(upper: String): Predicate[String] = { x: String =>
    Try(lessThan(BigDecimal(upper)).check(BigDecimal(x))).getOrElse(false)
  }

  def lessThanOrEqual[A: Ordering](that: A): Predicate[A] =
    implicitly[Ordering[A]].lteq(_: A, that)

  def lessThanOrEqual(upper: String): Predicate[String] = { x: String =>
    Try(lessThanOrEqual(BigDecimal(upper)).check(BigDecimal(x)))
      .getOrElse(false)
  }

  def oneOf[A](domain: A*): Predicate[A] = containedIn(domain)

  def between[A: Ordering](lower: A, upper: A): Predicate[A] = { a: A =>
    val ord = implicitly[Ordering[A]]
    ord.lteq(lower, a) && ord.lteq(a, upper)
  }

  def between(lower: String, upper: String): Predicate[String] = { x: String =>
    Try(lessThanOrEqual(BigDecimal(upper)).check(BigDecimal(x))).getOrElse(false) && Try(greaterThanOrEqual(BigDecimal(lower)).check(BigDecimal(x))).getOrElse(false)
  }

  def containedInSet[A](domain: Set[A]): Predicate[A] = domain.contains(_: A)

  def containedIn[A](domain: Seq[A]): Predicate[A] = domain.contains(_: A)

  def numeric[A]: Predicate[A] = (_: A) match {
    case n: Number => true
    case s: String => Try(s.toDouble).isSuccess
    case _ =>
      throw new NotImplementedError("'numeric' doesn't handle non-number/string values yet")
  }

  def integer[A]: Predicate[A] = (_: A) match {
    case s: String => Try(s.toInt).isSuccess
    case _ =>
      throw new NotImplementedError("'integer' doesn't handle non-number/string values yet")
  }

  def alphaNumeric[A]: Predicate[A] = (_: A) match {
    case s: String => s.matches("^[a-zA-Z0-9]+$")
    case _ =>
      throw new NotImplementedError(
        "'alphanumeric' doesn't handle non-number/string values yet")
  }

  def double[A]: Predicate[A] = (_: A) match {
    case s: String => s.matches("\\d+\\.\\d+")
    case _ =>
      throw new NotImplementedError(
        "'alphanumeric' doesn't handle non-number/string values yet")
  }

  def empty[A]: Predicate[A] = (_: A) match {
    case s: String => s.isEmpty
    case _ =>
      throw new NotImplementedError("'empty doesn't handle non-string values yet'")
  }

  def when(condition: ValidationResult)(thenTest: => ValidationResult): ValidationResult =
    condition.implies(thenTest)

}
