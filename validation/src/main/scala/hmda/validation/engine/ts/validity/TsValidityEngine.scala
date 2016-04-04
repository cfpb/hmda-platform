package hmda.validation.engine.ts.validity

import hmda.model.fi.ts.TransmittalSheet
import hmda.validation.api.ValidationApi
import hmda.validation.engine.ValidationError
import hmda.validation.engine.ts.TsCommonEngine
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.validity.{ V105, V140, V155 }

import scalaz._

trait TsValidityEngine extends TsCommonEngine with ValidationApi {

  private def doCheck[T](check: EditCheck[T], input: T): ValidationNel[ValidationError, T] = {
    convertResult(input, check(input), check.name)
  }

  def validate(ts: TransmittalSheet): TsValidation = {
    val checks: List[TsValidation] = List(
      V105,
      V140,
      V155
    ).map(doCheck(_, ts))

    validateAll(checks, ts)
  }
}
