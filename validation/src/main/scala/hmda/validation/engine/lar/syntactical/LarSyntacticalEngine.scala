package hmda.validation.engine.lar.syntactical

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.syntactical.{ S010, S011, S020, S040 }

trait LarSyntacticalEngine extends LarCommonEngine with ValidationApi {

  def checkSyntactical(lar: LoanApplicationRegister): LarValidation = {
    val checks = List(
      S010,
      S020
    ).map(check(_, lar))

    validateAll(checks, lar)
  }

  def checkSyntacticalIterable(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    val checks = List(
      S011,
      S040
    ).map(check(_, lars))

    validateAll(checks, lars)
  }

}
