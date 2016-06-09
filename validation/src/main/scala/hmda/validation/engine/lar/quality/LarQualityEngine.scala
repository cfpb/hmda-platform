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
      Q005,
      Q013,
      Q014,
      Q024,
      Q025,
      Q032,
      Q035,
      Q036,
      Q037,
      Q038,
      Q039,
      Q040,
      Q044,
      Q045,
      Q046,
      Q049,
      Q052,
      Q049,
      Q059,
      Q064,
      Q066
    ).map(check(_, lar))

    validateAll(checks, lar)
  }
}
