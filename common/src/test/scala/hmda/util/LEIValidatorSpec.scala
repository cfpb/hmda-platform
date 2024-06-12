package hmda.util

import hmda.util.LEIValidator._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class LEIValidatorSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("correctly validates LEIs") {
    isValidLEIFormat("ABCHD73849ofkdjutB63") mustBe true
    isValidLEIFormat("ABCHD73849ofkdjutB6") mustBe false
    isValidLEIFormat("ABCHD73849ofkdjutB 6") mustBe false
    isValidLEIFormat("ABCHD73849ofkdjutB-6") mustBe false
  }

}
