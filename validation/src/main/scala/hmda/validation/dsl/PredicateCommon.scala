package hmda.validation.dsl

import scala.language.implicitConversions
import scala.util.Try

object PredicateCommon {
  implicit def equalTo[T](that: T): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _ == that
  }

  implicit def greaterThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.gt(_, that)
  }

  implicit def greaterThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.gteq(_, that)
  }

  implicit def lessThan[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.lt(_, that)
  }

  implicit def lessThanOrEqual[T](that: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = ord.lteq(_, that)
  }

  implicit def between[T](lower: T, upper: T)(implicit ord: Ordering[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = { x => ord.lteq(lower, x) && ord.lteq(x, upper) }
  }

  implicit def numericallyBetween(lower: String, upper: String): Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = { x =>
      Try(between(BigDecimal(lower), BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
    }
  }

  implicit def numericallyLessThan(upper: String): Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = { x =>
      Try(lessThan(BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
    }
  }

  implicit def numericallyLessThanOrEqual(upper: String): Predicate[String] = new Predicate[String] {
    override def validate: (String) => Boolean = { x =>
      Try(lessThanOrEqual(BigDecimal(upper)).validate(BigDecimal(x))).getOrElse(false)
    }
  }

  implicit def oneOf[T](domain: T*): Predicate[T] = containedIn(domain)

  implicit def containedIn[T](domain: Seq[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = domain.contains(_)
  }

  def containedIn[T](domain: Set[T]): Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = domain.contains
  }

  def numeric[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case n: Number => true
      case s: String => Try(s.toDouble).isSuccess
      case _ => throw new NotImplementedError("'numeric' doesn't handle non-number/string values yet")
    }
  }

  implicit def empty[T]: Predicate[T] = new Predicate[T] {
    override def validate: (T) => Boolean = _.asInstanceOf[AnyRef] match {
      case s: String => s.isEmpty
      case _ => throw new NotImplementedError("'empty' doesn't handle non-string values yet")
    }
  }

  implicit def when(condition: Result)(thenTest: => Result): Result = {
    condition.implies(thenTest)
  }
}
