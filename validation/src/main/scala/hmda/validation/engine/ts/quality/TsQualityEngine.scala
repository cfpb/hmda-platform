package hmda.validation.engine.ts.quality

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.quality._

trait TsQualityEngine extends TsCommonEngine with ValidationApi {

  def checkQuality(ts: TransmittalSheet): TsValidation = {
    val checks = List(
      Q020
    ).map(check(_, ts))

    validateAll(checks, ts)
  }
}
