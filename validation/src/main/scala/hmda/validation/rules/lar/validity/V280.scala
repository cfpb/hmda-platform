package hmda.validation.rules.lar.validity

import hmda.model.census.CBSATractLookup
import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.fi.lar.fields.LarTopLevelFields._
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._

object V280 extends EditCheck[LoanApplicationRegister] {

  val cbsaTracts = CBSATractLookup.values

  val validMSAs = cbsaTracts.map(cbsa => cbsa.geoIdMsa).toSet

  val validMDs = cbsaTracts.map(cbsa => cbsa.metDivFp).toSet

  override def name: String = "V280"

  override def description = ""

  override def fields(lar: LoanApplicationRegister) = Map(
    noField -> ""
  )

  override def apply(input: LoanApplicationRegister): Result = {
    val msa = input.geography.msa

    val NA = msa is equalTo("NA")

    val validMSA = msa is containedIn(validMSAs)

    val validMD = msa is containedIn(validMDs)

    validMSA or validMD or NA
  }
}
