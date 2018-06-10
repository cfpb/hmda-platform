package hmda.validation.dsl

import scala.language.implicitConversions

trait Predicate[A] {
  def check: A => Boolean
}

object Predicate {
  implicit def checkFunction[A](f: A => Boolean): Predicate[A] =
    new Predicate[A] {
      override def check: A => Boolean = f
    }
}
