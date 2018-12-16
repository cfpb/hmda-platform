package hmda.validation.engine

import hmda.model.filing.ts.TransmittalLar
import hmda.validation.context.ValidationContext
import hmda.validation.rules.lar.syntactical.S304

object TsLarEngine extends ValidationEngine[TransmittalLar] {

  override def syntacticalChecks(ctx: ValidationContext) = Vector(
    S304
  )

}
