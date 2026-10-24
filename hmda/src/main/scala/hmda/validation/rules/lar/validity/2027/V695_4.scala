package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V695_4 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V695-4"

  override def parent: String = "V695"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {
    val nmlsrID = lar.larIdentifier.NMLSRIdentifier
    when (nmlsrID not oneOf("Exempt", "NA")) {
      val leadChar = nmlsrID.headOption.getOrElse(' ').toString
      leadChar not equalTo("0") and (leadChar is numeric)
    }
  }
}
