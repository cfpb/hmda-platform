package hmda.validation.engine

import hmda.model.filing.ts.TransmittalSheet
import hmda.validation.context.ValidationContext
import hmda.validation.rules.ts.validity._
import hmda.validation.engine.TsEngine2026

// $COVERAGE-OFF$
private[engine] object TsEngine2026Q extends ValidationEngine[TransmittalSheet] {

  override def syntacticalChecks(ctx: ValidationContext) = TsEngine2026.syntacticalChecks(ctx)

  override def validityChecks(ctx: ValidationContext) = TsEngine2026.validityChecks(ctx).appendedAll(Vector(V718.withContext(ctx)))filter(_ != V602)}
// $COVERAGE-ON$