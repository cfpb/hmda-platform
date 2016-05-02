package hmda.validation.engine.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.validity._

trait LarValidityEngine extends LarCommonEngine with ValidationApi {

  def checkValidity(lar: LoanApplicationRegister): LarValidation = {
    val checks = List(
      V220,
      V225,
      V255,
      V262,
      V290,
      V295,
      V300,
      V310,
      V315,
      V340,
      V347,
      V375,
      V400,
      V410,
      V455,
      V465,
      V470,
      V475,
      V490,
      V520,
      V570,
      V575
    ).map(check(_, lar))

    validateAll(checks, lar)
  }
}