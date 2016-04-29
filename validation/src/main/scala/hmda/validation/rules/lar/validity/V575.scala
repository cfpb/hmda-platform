package hmda.validation.rules.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Failure, Result }
import hmda.validation.rules.EditCheck

object V575 extends EditCheck[LoanApplicationRegister] {

  import hmda.validation.dsl.PredicateDefaults._
  import hmda.validation.dsl.PredicateSyntax._

  override def name: String = "V575"

  override def apply(lar: LoanApplicationRegister): Result = {
    when(lar.lienStatus is equalTo(2)) {
      lar.rateSpread is equalTo("NA") or {
        try {
          val rateSpread = BigDecimal(lar.rateSpread)
          (rateSpread is greaterThanOrEqual(BigDecimal("3.50"))) and
            (rateSpread is lessThanOrEqual(BigDecimal("99.99")))
        } catch {
          case ex: NumberFormatException => Failure(s"can't parse ${lar.rateSpread} as decimal")
        }
      }
    }
  }
}
