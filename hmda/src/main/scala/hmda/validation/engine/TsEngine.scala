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

}
