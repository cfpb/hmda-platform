package hmda.validation.engine.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.context.ValidationContext
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.quality._

trait TsQualityEngine extends TsCommonEngine with ValidationApi {

  def checkQuality(ts: TransmittalSheet, ctx: ValidationContext): TsValidation = {
    val checks = List(
      Q020,
      Q033.inContext(ctx)
    ).map(check(_, ts))

    validateAll(checks, ts)
  }
}
