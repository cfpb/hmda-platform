package hmda.validation.dsl

object PredicateSyntax {

  implicit class PredicateOps[A](data: A) {
    private def test(predicate: Predicate[A]): ValidationResult =
      if (predicate.check(data))
        ValidationSuccess
      else
        ValidationFailure

    private def testNot(predicate: Predicate[A]): ValidationResult =
      if (predicate.check(data))
        ValidationFailure
      else
        ValidationSuccess

    def is(predicate: Predicate[A]): ValidationResult =
      test(predicate)

    def not(predicate: Predicate[A]): ValidationResult =
      testNot(predicate)
  }

}
