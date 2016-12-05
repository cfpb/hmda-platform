package hmda.validation.rules.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.Result
import hmda.validation.rules.EditCheck
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.PredicateGeo._

object Q029 extends EditCheck[LoanApplicationRegister] {
  override def apply(lar: LoanApplicationRegister): Result = {
    val geo = lar.geography
    when(geo is stateCountyCombinationInMsaNotMicro) {
      geo.msa not equalTo("NA")
    }
  }

  override def name = "Q029"

  override def description = ""
}
