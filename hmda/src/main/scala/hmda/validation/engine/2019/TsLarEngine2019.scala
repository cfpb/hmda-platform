package hmda.validation.engine

import hmda.model.filing.ts._2019.TransmittalLar
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.quality.common.Q600
import hmda.validation.rules.lar.syntactical._2019.{S304, S305}

private[engine] object TsLarEngine2019
    extends ValidationEngine[TransmittalLar] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S304,
    S305
  )

  override val qualityChecks = Vector(
    Q600
  )

}
