package hmda.validation.engine.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.ts.validity.{ V105, V140, V155 }

trait TsValidityEngine extends TsCommonEngine with ValidationApi {

  private def v105(t: TransmittalSheet): TsValidation = {
    convertResult(t, V105(t), "V105")
  }

  private def v140(t: TransmittalSheet): TsValidation = {
    convertResult(t, V140(t), "V140")
  }

  private def v155(t: TransmittalSheet): TsValidation = {
    convertResult(t, V155(t), "V155")
  }

  def validate(ts: TransmittalSheet): TsValidation = {
    val checks = List(
      v105(ts),
      v140(ts),
      v155(ts)
    )

    validateAll(checks, ts)
  }
}
