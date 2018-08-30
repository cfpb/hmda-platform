package hmda.validation.engine

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.rules.ts.syntactical.S300
import hmda.validation.rules.ts.validity._

object TsEngine extends ValidationEngine[TransmittalSheet] {

  override val syntacticalChecks = Vector(
    S300
  )

  override val validityChecks = Vector(
    V600,
    V601,
    V602,
    V603,
    V604,
    V605,
    V606,
    V607
  )

  override def validateAll(
      ts: TransmittalSheet): TsEngine.HmdaValidation[TransmittalSheet] = {
    val validations = Vector(
      checkSyntactical(ts, ts.LEI),
      checkValidity(ts, ts.LEI)
    )

    validations.par.reduceLeft(_ combine _)
  }

}
