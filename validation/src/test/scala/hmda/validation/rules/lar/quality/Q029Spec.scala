package hmda.validation.rules.lar.quality

import java.io.File

import hmda.model.fi.lar.{ Geography, LoanApplicationRegister }
import hmda.parser.fi.lar.LarCsvParser
import hmda.validation.rules.EditCheck
import hmda.validation.rules.lar.{ BadValueUtils, LarEditCheckSpec }

import scala.io.Source

class Q029Spec extends LarEditCheckSpec with BadValueUtils {

  property("Succeeds for valid combination of MD, state, county and tract") {
    forAll(larGen) { lar =>
      val validGeography = Geography("14454", "25", "025", "0001.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails for valid combination of state, county and tract when MSA == NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "06", "007", "0036.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustFail
    }
  }

  property("Succeeds when MSA present in small county where tract is NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("10100", "46", "045", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds when MSA not present in small county where tract exists") {
    forAll(larGen) { lar =>
      val validGeography = Geography("10100", "46", "045", "9621.00")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Succeeds when MSA not present where tract does not exist but county is not small") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "46", "013", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustPass
    }
  }

  property("Fails when MSA not present in small county where tract is NA") {
    forAll(larGen) { lar =>
      val validGeography = Geography("NA", "46", "045", "NA")
      val validLar = lar.copy(geography = validGeography)
      validLar.mustFail
    }
  }
  

  override def check: EditCheck[LoanApplicationRegister] = Q029
}
