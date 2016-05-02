package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ RegexDsl, Result, Success }
import hmda.validation.rules.EditCheck

object V300 extends EditCheck[LoanApplicationRegister] with RegexDsl {

  val cbsaTracts = CBSATractLookup.values

  val validCombination = cbsaTracts.map { cbsa =>
    (cbsa.geoIdMsa, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }

  val validMdCombination = cbsaTracts.map { cbsa =>
    (cbsa.metDivFp, cbsa.state, cbsa.county, cbsa.tractDecimal)
  }

  val validStateCountyCombination = cbsaTracts.map { cbsa =>
    (cbsa.state, cbsa.county, cbsa.tractDecimal)
  }

  override def name: String = "V300"

  override def apply(input: LoanApplicationRegister): Result = {

    val msa = input.geography.msa
    val state = input.geography.state
    val county = input.geography.county
    val tract = input.geography.tract

    val combination = (msa, state, county, tract)
    val stateCountyCombination = (state, county, tract)

    val validCensusTractCombination = when(msa not equalTo("NA")) {
      (combination is containedIn(validCombination)) or
        (combination is containedIn(validMdCombination))
    }

    val tractStateCountyCombination = when(msa is equalTo("NA")) {
      (stateCountyCombination is containedIn(validStateCountyCombination)) or (tract is equalTo("NA"))
    }

    val validFormat = (tract is validCensusTractFormat) or (tract is equalTo("NA"))

    val counties = cbsaTracts.filter { c =>
      c.geoIdMsa == msa &&
        c.state == state &&
        c.county == county &&
        c.tractDecimal == tract
    }

    val smallCountyValue =
      if (counties.isEmpty)
        0
      else
        counties.map(c => c.smallCounty).head

    val smallCounty = when(smallCountyValue is equalTo(1)) {
      tract is equalTo("NA")
    }

    validFormat and
      validCensusTractCombination and
      tractStateCountyCombination and
      smallCounty

  }

}
