package hmda.util

import hmda.util.LEIValidator._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LEIValidatorSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("correctly validates LEIs") {
    isValidLEIFormat("ABCHD73849OKDJFIRB63") mustBe true
    isValidLEIFormat("ABCHD73849HDJUSKAB6") mustBe false
    isValidLEIFormat("ABCHD73849JFUDHSNB 6") mustBe false
    isValidLEIFormat("ABCHD73849HFUDJENB-6") mustBe false
  }

}
