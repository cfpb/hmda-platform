package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateRegEx._

object V300 extends EditCheck[LoanApplicationRegister] {

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

  val smallCounties = cbsaTracts
    .filter { cbsa => cbsa.smallCounty == 1 }
    .map { cbsa => (cbsa.state, cbsa.county) }

  override def name: String = "V300"

  override def apply(lar: LoanApplicationRegister): Result = {

    val msa = lar.geography.msa
    val state = lar.geography.state
    val county = lar.geography.county
    val tract = lar.geography.tract

    val allCombination = (msa, state, county, tract)
    val stateCountyCombination = (state, county, tract)

    val validCensusTractCombination = when(msa not equalTo("NA")) {
      (allCombination is containedIn(validCombination)) or
        (allCombination is containedIn(validMdCombination))
    }

    val tractStateCountyCombination = when(msa is equalTo("NA")) {
      (stateCountyCombination is containedIn(validStateCountyCombination))
    }

    val validFormat = (tract is validCensusTractFormat)

    val smallCounty = (state, county) not containedIn(smallCounties)

    val counties = cbsaTracts.find { c =>
      c.geoIdMsa == msa &&
        c.state == state &&
        c.county == county &&
        c.tractDecimal == tract
    }

    val smallCountyValue = counties.map(c => c.smallCounty).getOrElse(0)

    val smallCountyOrig = smallCountyValue not equalTo(1)

    when(tract not equalTo("NA")) {
      validFormat and
        validCensusTractCombination and
        tractStateCountyCombination and
        smallCounty
    }

  }

}
