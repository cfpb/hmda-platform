package hmda.validation.rules.ts

import hmda.model.filing.ts.TransmittalSheet
import org.scalatest.{ MustMatchers, PropSpec }
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.{ ValidationFailure, ValidationSuccess }
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

abstract class TsEditCheckSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  implicit val generatorDriverConfig =
    PropertyCheckConfiguration(minSuccessful = 100, maxDiscardedFactor = 5.0)

  def check: EditCheck[TransmittalSheet]

  implicit class TsChecker(ts: TransmittalSheet) {
    def mustFail = check(ts) mustBe a[ValidationFailure.type]
    def mustPass = check(ts) mustBe a[ValidationSuccess.type]
  }
}