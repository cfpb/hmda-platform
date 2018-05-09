package hmda.model.filing.lar

import org.scalatest.{MustMatchers, PropSpec}
import org.scalatest.prop.PropertyChecks
import LarGenerators._
import com.typesafe.config.ConfigFactory

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
