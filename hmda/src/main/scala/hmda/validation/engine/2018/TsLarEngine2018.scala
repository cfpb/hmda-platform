package hmda.validation.engine

import hmda.model.filing.ts._2018.TransmittalLar
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.quality.common.Q600
import hmda.validation.rules.lar.syntactical.S305
import hmda.validation.rules.lar.syntactical._2018.{S304, S305}
import hmda.validation.rules.lar.syntactical.common.S305

private[engine] object TsLarEngine2018
    extends ValidationEngine[TransmittalLar] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S304,
    S305
  )

  override val qualityChecks = Vector(
    Q600
  )

}
