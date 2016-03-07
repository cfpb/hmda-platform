package hmda.validation.rules.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.dsl.{ Result, CommonDsl }

object S010 extends CommonDsl {

  //Hardcoded for now
  val larId = 2

  def apply(lar: LoanApplicationRegister): Result = {
    lar.id is equalTo(larId)
  }
}
