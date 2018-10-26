package hmda.model.filing.lar

import com.typesafe.config.ConfigFactory
import hmda.model.filing.lar.LarGenerators._
import org.scalatest.prop.PropertyChecks
import org.scalatest.{MustMatchers, PropSpec}

class LoanApplicationRegisterSpec
    extends PropSpec
    with PropertyChecks
    with MustMatchers {

  val config = ConfigFactory.load()
  val numberOfFields = config.getInt("hmda.filing.lar.length")

  forAll(larGen) { lar =>
    val values = lar.toCSV.split('|')
    values.length mustBe numberOfFields
  }

}
