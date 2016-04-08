package hmda.validation.rules.lar.syntactical

import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.lar.LarEditCheckSpec

class S040Spec extends LarEditCheckSpec {

  property("Loan/Application number must be unique") {
    forAll(larListGen) { lars =>
      S040(lars) mustBe Success()
      whenever(lars.nonEmpty) {
        val duplicateLars = lars.head :: lars
        S040(duplicateLars) mustBe Failure("Submission contains duplicates")
      }
    }
  }

}
