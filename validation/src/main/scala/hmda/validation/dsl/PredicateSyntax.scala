package hmda.validation.dsl

import scala.language.implicitConversions

object PredicateSyntax {
  implicit class PredicateOps[T](data: T) {
    private def test(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Success()
        case false => Failure()
      }
    }

    private def testNot(predicate: Predicate[T]): Result = {
      predicate.validate(data) match {
        case true => Failure()
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
}
