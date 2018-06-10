package hmda.validation.dsl

object PredicateSyntax {

  implicit class PredicateOps[A](data: A) {
    private def test(predicate: Predicate[A]): ValidationResult = {
      predicate.check(data) match {
        case true  => ValidationSuccess
        case false => ValidationFailure
      }
    }

    private def testNot(predicate: Predicate[A]): ValidationResult = {
      predicate.check(data) match {
        case true  => ValidationFailure
        case false => ValidationSuccess
      }
    }

    def is(predicate: Predicate[A]): ValidationResult = {
      test(predicate)
    }

    def not(predicate: Predicate[A]): ValidationResult = {
      testNot(predicate)
    }
  }

}
