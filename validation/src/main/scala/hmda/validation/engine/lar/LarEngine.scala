package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.context.ValidationContext
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import hmda.validation.engine.lar.validity.LarValidityEngine
import hmda.validation.engine.lar.quality.LarQualityEngine

import scalaz._
import Scalaz._

trait LarEngine extends LarSyntacticalEngine with LarValidityEngine with LarQualityEngine {

  def validateLar(lar: LoanApplicationRegister, ctx: ValidationContext): LarValidation = {
    (
      checkSyntactical(lar, ctx) |@|
      checkValidity(lar, ctx) |@|
      checkQuality(lar, ctx)
    )((_, _, _) => lar)

  }

  def validateLars(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    checkSyntacticalCollection(lars)
  }

}
