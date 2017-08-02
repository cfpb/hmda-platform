package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.`macro`.LarMacroEngine
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import hmda.validation.engine.lar.validity.LarValidityEngine
import hmda.validation.engine.lar.quality.LarQualityEngine

import scalaz._
import Scalaz._

trait LarEngine extends LarSyntacticalEngine with LarValidityEngine with LarQualityEngine with LarMacroEngine {

  def validateLar(lar: LoanApplicationRegister, ctx: ValidationContext): LarValidation = {
    (
      checkSyntactical(lar, ctx) |@|
      checkValidity(lar, ctx) |@|
      checkQuality(lar, ctx)
    )((_, _, _) => lar)

  }

}
