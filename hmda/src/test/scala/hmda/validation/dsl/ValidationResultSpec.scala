package hmda.validation.dsl

import org.scalatest.{MustMatchers, WordSpec}

class ValidationResultSpec extends WordSpec with MustMatchers {

  "Validation Result" must {
    "combine with and operator" in {
      ValidationSuccess and ValidationSuccess mustBe ValidationSuccess
      ValidationSuccess and ValidationFailure mustBe ValidationFailure
      ValidationFailure and ValidationSuccess mustBe ValidationFailure
    }
    "combine with or operator" in {
      ValidationSuccess or ValidationSuccess mustBe ValidationSuccess
      ValidationSuccess or ValidationFailure mustBe ValidationSuccess
      ValidationFailure or ValidationSuccess mustBe ValidationSuccess
      ValidationFailure or ValidationFailure mustBe ValidationFailure
    }
  }

}
