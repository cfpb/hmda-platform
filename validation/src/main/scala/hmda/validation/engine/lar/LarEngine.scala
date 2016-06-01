package hmda.validation.engine.lar

import hmda.model.fi.lar.LoanApplicationRegister
import hmda.validation.engine.lar.syntactical.LarSyntacticalEngine
import hmda.validation.engine.lar.validity.LarValidityEngine
import hmda.validation.engine.lar.quality.LarQualityEngine
import scalaz._
import Scalaz._

trait LarEngine extends LarSyntacticalEngine with LarValidityEngine with LarQualityEngine {

  def validateLar(lar: LoanApplicationRegister): LarValidation = {
    (
      checkSyntactical(lar) |@|
      checkValidity(lar) |@|
      checkQuality(lar)
    )((_, _, _) => lar)

  }

  def validateLars(lars: Iterable[LoanApplicationRegister]): LarsValidation = {
    checkSyntacticalCollection(lars)
  }

}
