package hmda.validation.engine

import hmda.model.filing.ts.TransmittalSheet
import hmda.model.validation.{Syntactical, ValidationErrorType, Validity}
import hmda.validation.api.ValidationApi
import hmda.validation.rules.EditCheck
import hmda.validation.rules.ts.syntactical.S300
import hmda.validation.rules.ts.validity._

object TsEngine extends ValidationApi[TransmittalSheet] {

  def validateTs(ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = {
    val validations = Vector(
      checkSyntactical(ts),
      checkValidity(ts)
    )

    validations.par.reduceLeft(_ combine _)
  }

  def checkSyntactical(
      ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = {
    val syntacticalChecks = Vector(
      S300
    )

    runChecks(ts, syntacticalChecks, Syntactical)

  }

  def checkValidity(ts: TransmittalSheet): HmdaValidation[TransmittalSheet] = {
    val validityChecks = Vector(
      V600,
      V601,
      V602,
      V603,
      V604,
      V605,
      V606,
      V607
    )

    runChecks(ts, validityChecks, Validity)

  }

  private def runChecks(ts: TransmittalSheet,
                        checksToRun: Vector[EditCheck[TransmittalSheet]],
                        validationErrorType: ValidationErrorType)
    : HmdaValidation[TransmittalSheet] = {
    val checks =
      checksToRun.par.map(check(_, ts, ts.LEI, validationErrorType)).toList

    checks.par.reduceLeft(_ combine _)
  }

}
