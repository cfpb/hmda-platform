package hmda.validation.rules.lar.validity._2027

import hmda.model.filing.lar.LoanApplicationRegister
import hmda.parser.filing.ts.TsCsvParser.toValidBigInt
import hmda.validation.dsl.PredicateCommon._
import hmda.validation.dsl.PredicateSyntax._
import hmda.validation.dsl.ValidationResult
import hmda.validation.rules.EditCheck

object V695_5 extends EditCheck[LoanApplicationRegister] {
  override def name: String = "V695-5"

  override def parent: String = "V695"

  override def apply(lar: LoanApplicationRegister): ValidationResult = {

    val nmlsrID = lar.larIdentifier.NMLSRIdentifier
    when (nmlsrID not oneOf("Exempt", "NA")  ){
      toValidBigInt(nmlsrID) not oneOf(1111, 7777, 8888, 9999)
    }
  }
}
