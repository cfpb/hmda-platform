package hmda.validation.rules.ts

import hmda.model.fi.ts.TransmittalSheet
import hmda.parser.fi.ts.TsGenerators
import hmda.validation.dsl.{ Failure, Success }
import hmda.validation.rules.EditCheck
import org.scalatest.prop.PropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

abstract class TsEditCheckSpec extends PropSpec with PropertyChecks with MustMatchers with TsGenerators {
  implicit val generatorDriverConfig =
    PropertyCheckConfig(minSuccessful = 100, maxDiscarded = 500)

  def check: EditCheck[TransmittalSheet]

  implicit class TsChecker(ts: TransmittalSheet) {
    def mustFail = check(ts) mustBe a[Failure]
    def mustPass = check(ts) mustBe a[Success]
  }
}
