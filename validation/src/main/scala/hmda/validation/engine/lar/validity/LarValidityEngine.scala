package hmda.validation.engine.lar.validity

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.Validity
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.validity._

trait LarValidityEngine extends LarCommonEngine with ValidationApi {

  def checkValidity(lar: LoanApplicationRegister, ctx: ValidationContext): LarValidation = {
    val checks = List(
      V210,
      V215,
      V220,
      V225,
      V230,
      V250,
      V255,
      V260,
      V262,
      V265,
      V275,
      V280,
      V285,
      V290,
      V295,
      V300,
      V310,
      V315,
      V317,
      V320,
      V325,
      V326,
      V330,
      V335,
      V338,
      V340,
      V347,
      V355,
      V360,
      V375,
      V385,
      V400,
      V410,
      V415,
      V425,
      V430,
      V435,
      V440,
      V445,
      V447,
      V450,
      V455,
      V460,
      V463,
      V465,
      V470,
      V475,
      V480,
      V485,
      V490,
      V495,
      V500,
      V505,
      V520,
      V525,
      V535,
      V540,
      V545,
      V550,
      V555,
      V560,
      V565,
      V570,
      V575
    ).map(check(_, lar, lar.loan.id, Validity, false))

    validateAll(checks, lar)
  }
}
