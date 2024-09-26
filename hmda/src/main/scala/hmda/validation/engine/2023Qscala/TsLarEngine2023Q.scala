package hmda.validation.engine

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.quality.common.Q600
import hmda.validation.rules.lar.syntactical.{S304, S305, S306, S307}
// $COVERAGE-OFF$
private[engine] object TsLarEngine2023Q extends ValidationEngine[TransmittalLar] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S304,
    S305,
    S306,
    S307
  )

  override def qualityChecks(ctx: ValidationContext) = Vector(
    Q600
  )

}

// $COVERAGE-ON$