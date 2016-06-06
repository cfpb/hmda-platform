package hmda.validation.engine.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.quality._

trait LarQualityEngine extends LarCommonEngine with ValidationApi {

  def checkQuality(lar: LoanApplicationRegister): LarValidation = {
    val checks = List(
      Q001,
      Q002,
      Q003,
      Q004,
      Q005
    ).map(check(_, lar))

    validateAll(checks, lar)
  }
}
