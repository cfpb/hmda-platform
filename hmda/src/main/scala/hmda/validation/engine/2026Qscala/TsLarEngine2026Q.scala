package hmda.validation.engine

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.syntactical.S307
import hmda.validation.engine.TsLarEngine2026
// $COVERAGE-OFF$
private[engine] object TsLarEngine2026Q extends ValidationEngine[TransmittalLar] {

  override def syntacticalChecks(ctx: ValidationContext) = TsLarEngine2026.syntacticalChecks(ctx) ++ Vector(S307)

  override def qualityChecks(ctx: ValidationContext) = TsLarEngine2026.qualityChecks(ctx: ValidationContext)

}

// $COVERAGE-ON$