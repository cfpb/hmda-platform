package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.{ LarGenerators, LoanApplicationRegister }
import hmda.validation.context.ValidationContext
import hmda.validation.dsl.{ Failure, Success }
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class S270Spec extends PropSpec with PropertyChecks with MustMatchers with LarGenerators {
  property("be named S270") {
    val ctx = ValidationContext(None, Some(2017))

    S270.inContext(ctx).name mustBe "S270"
  }

  def in2017(lar: LoanApplicationRegister): LoanApplicationRegister = {
    val date = lar.actionTakenDate.toString
    val monthAndDay = date.substring(4)
    val newDate = ("2017" + monthAndDay).toInt
    lar.copy(actionTakenDate = newDate)
  }

  property("succeed when the year of the LAR's Action Taken Date matches the filing year") {
    val ctx = ValidationContext(None, Some(2017))

    forAll(larGen) { l =>
      val lar = in2017(l)
      S270.inContext(ctx)(lar) mustBe Success()
    }
  }

  property("fail when TS's activity year does not match the filing year") {
    val ctx = ValidationContext(None, Some(2018))

    forAll(larGen) { l =>
      val lar = in2017(l)
      S270.inContext(ctx)(lar) mustBe Failure()
    }
  }

  property("succeed when there is no filing year") {
    val ctx = ValidationContext(None, None)

    forAll(larGen) { l =>
      val lar = in2017(l)
      S270.inContext(ctx)(lar) mustBe Success()
    }
  }

}
