package hmda.validation.engine.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.validity._

trait LarValidityEngine extends LarCommonEngine with ValidationApi {
  private def v220(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V220(lar.loan), "V220")
  }

  private def v225(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V225(lar), "V225")
  }

  private def v255(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V255(lar), "V255")
  }

  private def v262(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V262(lar), "V262")
  }

  private def v400(lar: LoanApplicationRegister): LarValidation = {
    convertResult(lar, V400(lar), "V400")
  }

  def validate(lar: LoanApplicationRegister): LarValidation = {
    val checks = List(
      v220(lar),
      v225(lar),
      v255(lar),
      v262(lar),
      v400(lar)
    )

    validateAll(checks, lar)
  }
}