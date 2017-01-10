package hmda.validation.engine.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.Validity
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.validity._

trait TsValidityEngine extends TsCommonEngine with ValidationApi {

  def checkValidity(ts: TransmittalSheet, ctx: ValidationContext): TsValidation = {
    val tsId = ts.agencyCode + ts.respondent.id
    val checks: List[TsValidation] = List(
      V105,
      V108,
      V110.inContext(ctx),
      V111,
      V112,
      V115,
      V120,
      V125,
      V135,
      V140,
      V145,
      V150,
      V155
    ).map(check(_, ts, tsId, Validity, true))

    validateAll(checks, ts)
  }
}
