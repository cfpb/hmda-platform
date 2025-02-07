package hmda.util

import hmda.util.Filer._
import org.scalatest.{MustMatchers, PropSpec}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

class FilerSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  property("correctly get local config") {
    // these values are from common/resource/reference.conf
    getString("currentYear") mustBe "2021"
    getString("yearsAllowed") mustBe "2018,2019,2020,2021,2022,2023,2024,2025"
    getString("quarterlyYearsAllowed") mustBe "2020,2021,2022,2023,2024"
    getString("q2Start") mustBe "July 01"
    getString("q3End") mustBe "December 31"
    getString("actionQ2Start") mustBe "April 01"
    getString("actionQ3End") mustBe "September 30"
    getString("randomKey") mustBe "randomKey"
  }

  property("correctly get config") {
    getConfig("currentYear") mustBe "2021"
    getConfig("yearsAllowed") mustBe "2018,2019,2020,2021,2022,2023,2024,2025"
    getConfig("quarterlyYearsAllowed") mustBe "2020,2021,2022,2023,2024"
    getConfig("q2Start") mustBe "July 01"
    getConfig("q3End") mustBe "December 31"
    getConfig("actionQ2Start") mustBe "April 01"
    getConfig("actionQ3End") mustBe "September 30"
    getConfig("randomKey") mustBe "randomKey"
  }

}
