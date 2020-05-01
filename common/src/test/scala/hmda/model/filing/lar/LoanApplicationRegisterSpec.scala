package hmda.model.filing.lar

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import org.scalatest.{ MustMatchers, PropSpec }

class LoanApplicationRegisterSpec extends PropSpec with ScalaCheckPropertyChecks with MustMatchers {

  val config         = ConfigFactory.load()
  val currentYear    = config.getString("hmda.filing.current")
  val numberOfFields = config.getInt(s"hmda.filing.$currentYear.lar.length")

  forAll(larGen) { lar =>
    val values = lar.toCSV.split('|')
    values.length mustBe numberOfFields
  }

}