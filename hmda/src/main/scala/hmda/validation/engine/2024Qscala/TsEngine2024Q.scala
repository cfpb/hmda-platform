package hmda.validation.engine

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.rules.ts.validity._
import hmda.validation.engine.TsEngine2024

// $COVERAGE-OFF$
private[engine] object TsEngine2024Q extends ValidationEngine[TransmittalSheet] {

  override def syntacticalChecks(ctx: ValidationContext) = TsEngine2024.syntacticalChecks(ctx)

  override def validityChecks(ctx: ValidationContext) = TsEngine2024.validityChecks(ctx).appendedAll(Vector(V718.withContext(ctx)))filter(_ != V602)
}
// $COVERAGE-ON$