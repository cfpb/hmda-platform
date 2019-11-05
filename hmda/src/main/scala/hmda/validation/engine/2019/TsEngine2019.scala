package hmda.validation.engine

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.rules.ts.syntactical.{ S300, S302, S303 }
import hmda.validation.rules.ts.validity._

private[engine] object TsEngine2019 extends ValidationEngine[TransmittalSheet] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S300,
    S302.withContext(ctx),
    S303.withContext(ctx)
  )

  override def validityChecks(ctx: ValidationContext) = Vector(
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
