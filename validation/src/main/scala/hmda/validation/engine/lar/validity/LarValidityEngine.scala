package hmda.validation.engine.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.validity.{ V262, V220 }

trait LarValidityEngine extends LarCommonEngine with ValidationApi {
  private def v220(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V220(lar.loan), "V220")
  }

  private def v262(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V262(lar), "V262")
  }

  def validate(lar: LoanApplicationRegister): LarValidation = {
    val checks = List(
      v220(lar),
      v262(lar)
    )

    validateAll(checks, lar)
  }
}