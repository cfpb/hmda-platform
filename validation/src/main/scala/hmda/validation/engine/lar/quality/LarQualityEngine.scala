package hmda.validation.engine.lar.quality

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.api.ValidationApi
import hmda.validation.engine.lar.LarCommonEngine
import hmda.validation.rules.lar.quality._

trait LarQualityEngine extends LarCommonEngine with ValidationApi {

  def checkQuality(lar: LoanApplicationRegister): LarValidation = {
    val checks = List(
      Q005,
      Q013,
      Q014,
      Q024,
      Q032,
      Q035,
      Q036,
      Q037,
      Q059
    ).map(check(_, lar))

    validateAll(checks, lar)
  }
}
