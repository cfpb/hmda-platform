package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ CommonDsl, Result }
import hmda.validation.rules.EditCheck

object S010 extends EditCheck[LoanApplicationRegister] {

  //Hardcoded for now
  val larId = 2

  def apply(lar: LoanApplicationRegister): Result = {
    lar.id is equalTo(larId)
  }

  def name = "S010"
}
