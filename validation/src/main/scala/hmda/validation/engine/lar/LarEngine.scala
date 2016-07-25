package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.model.institution.Institution
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import hmda.validation.engine.lar.validity.LarValidityEngine
import hmda.validation.engine.lar.quality.LarQualityEngine

import scalaz._
import Scalaz._

trait LarEngine extends LarSyntacticalEngine with LarValidityEngine with LarQualityEngine {

  def validateLar(lar: LoanApplicationRegister, institution: Option[Institution]): LarValidation = {
    (
      checkSyntactical(lar, institution) |@|
      checkValidity(lar, institution) |@|
      checkQuality(lar, institution)
    )((_, _, _) => lar)

  }

  // FIXME: This is not actually used anywhere!  These should really be "macro" edits!
  def validateLars(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    checkSyntacticalCollection(lars)
  }

}
