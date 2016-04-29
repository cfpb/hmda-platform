package hmda.validation.dsl

object PredicateSyntax {
  implicit class PredicateOps[T](data: T) {
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
}
